package de.verdox.mccreativelab.features.legacy;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.recipe.CustomItemData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class LegacyFoodSystem extends LegacyFeature {
    private static final Map<CustomItemData, Double> healAmounts = new HashMap<>();

    @Override
    protected void onEnable() {
        //TODO: Listeners die Food Damage weglassen
        // - Food Bar per Resource Pack ausblenden
        // - Essens Items lassen sich nur auf 1 stapeln
        // - Remove Hunger Effect
        // - Food heilt direkt
        // - Spieler werden intern auf Food Level 0 gesetzt, sodass sprinten deaktiviert ist.

        setHealAmountWhenEaten(Material.BREAD, 5);
        setHealAmountWhenEaten(Material.COOKED_COD, 5);
        setHealAmountWhenEaten(Material.COOKED_SALMON, 5);
        setHealAmountWhenEaten(Material.COOKED_PORKCHOP, 8);
        setHealAmountWhenEaten(Material.COD, 3);
        setHealAmountWhenEaten(Material.SALMON, 3);
        setHealAmountWhenEaten(Material.PORKCHOP, 3);
        setHealAmountWhenEaten(Material.MUSHROOM_STEW, 10);
        setHealAmountWhenEaten(Material.CAKE, 2);

        setHealAmountWhenEaten(Material.GOLDEN_APPLE, 20);

        setHealAmountWhenEaten(Material.COOKIE, 1);

        CustomResourcePack customResourcePack = MCCreativeLabExtension.getCustomResourcePack();
        Asset<CustomResourcePack> empty = new Asset<>("/empty_block/textures/empty.png");
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/food_empty"), empty, ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/food_empty_hunger"), empty, ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/food_full"), empty, ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/food_full_hunger"), empty, ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/food_half"), empty, ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/food_half_hunger"), empty, ResourcePackAssetTypes.TEXTURES, "png"));

        ItemBehaviour legacyFoodItemBehaviour = new ItemBehaviour() {
            @Override
            public BehaviourResult.Object<Integer> getMaxStackSize(ItemStack stack) {
                return new BehaviourResult.Object<>(1, BehaviourResult.Object.Type.REPLACE_VANILLA);
            }
        };

        for (Material value : Material.values()) {
            if (value.isEdible())
                ItemBehaviour.ITEM_BEHAVIOUR.setBehaviour(new CustomItemData(value, 0), legacyFoodItemBehaviour);
        }
    }

    public void setAllowSprinting(Player player, boolean value) {
        if (value) {
            player.setFoodLevel(10);
            player.setSaturation(10);
            player.setSaturatedRegenRate(10);
            player.sendHealthUpdate();
        } else {
            player.setFoodLevel(0);
            player.setSaturation(0);
            player.setSaturatedRegenRate(0);
            player.sendHealthUpdate();
        }
    }

    public LegacyFoodSystem setHealAmountWhenEaten(CustomItemData customItemData, double healAmount) {
        healAmounts.put(customItemData, healAmount);
        return this;
    }

    public LegacyFoodSystem setHealAmountWhenEaten(Material material, double healAmount) {
        healAmounts.put(new CustomItemData(material, 0), healAmount);
        return this;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
            Bukkit.getScheduler()
                  .runTask(MCCreativeLabExtension.getInstance(), () -> setAllowSprinting(e.getPlayer(), false));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e) {
        setAllowSprinting(e.getPlayer(), false);
    }

    @EventHandler
    public void removeHungerAndSaturationPotionEffects(EntityPotionEffectEvent e) {
        if (e.getNewEffect() == null)
            return;
        if (e.getNewEffect().getType().equals(PotionEffectType.HUNGER) || e.getNewEffect().getType()
                                                                           .equals(PotionEffectType.SATURATION))
            e.setCancelled(true);
    }

    @EventHandler
    public void removeFoodRegeneration(EntityRegainHealthEvent e) {
        if (e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED))
            e.setCancelled(true);
    }

    @EventHandler
    public void removeFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void itemConsumeEvent(PlayerItemConsumeEvent e) {
        CustomItemData customItemData = CustomItemData.fromItemStack(e.getItem());
        double healAmount = healAmounts.getOrDefault(customItemData, 0d);
        double maxHealth = e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = Math.min(maxHealth, e.getPlayer().getHealth() + healAmount);
        e.getPlayer().setHealth(newHealth);
        e.getPlayer().sendHealthUpdate();
    }

    @EventHandler
    public void noDamageWhenNoFood(EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.STARVATION))
            e.setCancelled(true);
    }
}

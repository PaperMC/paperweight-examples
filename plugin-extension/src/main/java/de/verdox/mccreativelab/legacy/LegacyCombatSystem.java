package de.verdox.mccreativelab.legacy;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class LegacyCombatSystem extends LegacyFeature{
    @Override
    protected void onEnable() {
        CustomResourcePack customResourcePack = MCCreativeLabExtension.getInstance().getCustomResourcePack();
        Asset<CustomResourcePack> empty = new Asset<>("/empty_block/textures/empty.png");
        customResourcePack.register(new AssetBasedResourcePackResource(empty, new NamespacedKey("minecraft", "gui/sprites/hud/crosshair_attack_indicator_background"), ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(empty, new NamespacedKey("minecraft", "gui/sprites/hud/crosshair_attack_indicator_full"), ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(empty, new NamespacedKey("minecraft", "gui/sprites/hud/crosshair_attack_indicator_progress"), ResourcePackAssetTypes.TEXTURES, "png"));
        //TODO
        // Paper config -> Disable Player crits

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);
    }
}

package de.verdox.mccreativelab.features.legacy;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class LegacyCombatSystem extends LegacyFeature{
    @Override
    protected void onEnable() {
        CustomResourcePack customResourcePack = MCCreativeLabExtension.getInstance().getCustomResourcePack();
        Asset<CustomResourcePack> empty = new Asset<>("/empty_block/textures/empty.png");
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/crosshair_attack_indicator_background"), empty, ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/crosshair_attack_indicator_full"), empty,ResourcePackAssetTypes.TEXTURES, "png"));
        customResourcePack.register(new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "gui/sprites/hud/crosshair_attack_indicator_progress"), empty,ResourcePackAssetTypes.TEXTURES, "png"));
        //TODO
        // Paper config -> Disable Player crits

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);
    }
}

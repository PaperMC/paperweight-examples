package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.block.display.ReusedBlockStateDisplay;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.item.FakeItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Debug implements Listener {

    public static final FakeBlock DEBUG_BLOCK = MCCreativeLabExtension
        .getFakeBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "test_fake_block"), CustomOreBlock.class)
            .withBlockState(builder ->
                builder
                    .withBlockProperties(fakeBlockProperties ->
                        fakeBlockProperties.isRandomlyTicking(true)
                                           .withBlockHardness(4)
                                           .withExplosionResistance(6)
                                           .requiresCorrectToolForDrops(true)
                    )
                    .withBlockDisplay(
                        new ReusedBlockStateDisplay.Builder()
                            .asFullBlockDisplay(new Asset<>("/debug/textures/block/tin_ore.png"))
                    )
            )
        );

/*    public static final FakeBlock DEBUG_CROP = MCCreativeLabExtension
        .getFakeBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab","debug_crop"), FakeBlock.class)
            .withBlockState(builder ->
                builder.withBlockProperties(fakeBlockProperties ->
                    fakeBlockProperties.fromVanillaBlockData(Bukkit.createBlockData(Material.WHEAT))
                    )
                    .withBlockDisplay(displayBuilder ->
                        displayBuilder.withHitbox(FakeBlock.FakeBlockHitbox.CROP_AGE_0)

                        )
                )
        );*/

    public static final FakeItem DEBUG_ITEM = MCCreativeLabExtension
        .getFakeItemRegistry()
        .register(
            new FakeItem.Builder<>(new NamespacedKey("mccreativelab", "debug_item"), Material.STICK, DebugItem.class)
                .withProperties(new FakeItem.FakeItemProperties().stacksTo(4).fireResistant())
                .withItemMeta(meta -> meta.displayName(Component.text("Debug Item")))
        );
    public static final DebugHud DEBUG_HUD = new DebugHud(new NamespacedKey("mccreativelab", "debug_hud"));

    public static void init(){
        MCCreativeLabExtension.getCustomResourcePack().register(DEBUG_HUD);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e){
        MCCreativeLabExtension.getHudRenderer().getOrStartActiveHud(e.getPlayer() ,DEBUG_HUD).executeOnElement("debugText", SingleHudText.RenderedSingleHudText.class, renderedSingleHudText -> {
            renderedSingleHudText.setRenderedText(Bukkit.getServer().getName()+" "+Bukkit.getServer().getMinecraftVersion());
        });
    }
}

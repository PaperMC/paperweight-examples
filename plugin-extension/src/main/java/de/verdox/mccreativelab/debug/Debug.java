package de.verdox.mccreativelab.debug;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.ReusedBlockStateDisplay;
import de.verdox.mccreativelab.debug.block.CustomOreBlock;
import de.verdox.mccreativelab.entity.UnleashableSugarCow;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cow;
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
    public static final DebugHud DEBUG_HUD = new DebugHud(new NamespacedKey("mccreativelab", "debug_hud"));
    public static final DebugMenu DEBUG_MENU = new DebugMenu(new NamespacedKey("mccreativelab","debug_menu"));

    public static void init() {
        MCCreativeLabExtension.getCustomResourcePack().register(DEBUG_HUD);
        MCCreativeLabExtension.getCustomResourcePack().register(DEBUG_MENU);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        MCCreativeLabExtension.getHudRenderer().getOrStartActiveHud(e.getPlayer(), DEBUG_HUD)
                              .editRenderedElement("debugText", SingleHudText.RenderedSingleHudText.class, renderedSingleHudText -> {
                                  renderedSingleHudText.setRenderedText(Bukkit.getServer().getName() + "" + Bukkit
                                      .getServer().getMinecraftVersion());
                              });
    }

    @EventHandler
    public void onCreatureAddToWorld(EntityAddToWorldEvent e) {
        if (e.getEntity() instanceof Cow cow)
            cow.setCustomEntityBehaviour(Cow.class, new UnleashableSugarCow());
    }
}

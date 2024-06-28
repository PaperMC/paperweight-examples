package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.debug.block.CustomOreBlock;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.ReusedBlockStateDisplay;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;

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
                            .asFullBlockDisplay(new Asset<>("/debug/textures/block/tin_ore.png"), 500)
                    )
            )
        );
    public static final DebugMenu DEBUG_MENU = new DebugMenu(new NamespacedKey("mccreativelab","debug_menu"));

    public static void init() {
        MCCreativeLabExtension.getCustomResourcePack().register(DEBUG_MENU);
    }

/*    @EventHandler
    public void onCreatureAddToWorld(EntityAddToWorldEvent e) {
        if (e.getEntity() instanceof Cow cow)
            cow.setCustomEntityBehaviour(Cow.class, new UnleashableSugarCow());
    }*/
}

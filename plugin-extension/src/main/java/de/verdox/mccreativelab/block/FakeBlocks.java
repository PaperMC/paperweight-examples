package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class FakeBlocks {
    public static final FakeBlock DEBUG_BLOCK = MCCreativeLabExtension
        .getCustomBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "test_fake_block"), FakeBlock.class)
            .withBlockState(builder ->
                builder
                    .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.isRandomlyTicking(true))
                    .withFullBlockDisplay(new Asset<>(() -> MCCreativeLabExtension.class.getResourceAsStream("/debug/textures/block/tin_ore.png")), Bukkit.createBlockData(Material.STONE))
            )
        );

    public static void init() {
    }
}

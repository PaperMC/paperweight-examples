package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.block.impl.CustomOreBlock;
import de.verdox.mccreativelab.generator.Asset;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class FakeBlocks {
    public static final FakeBlock DEBUG_BLOCK = MCCreativeLabExtension
        .getCustomBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "test_fake_block"), CustomOreBlock.class)
            .withBlockState(builder ->
                builder
                    .withBlockProperties(fakeBlockProperties ->
                        fakeBlockProperties.isRandomlyTicking(true)
                                           .withBlockHardness(4)
                                           .withExplosionResistance(6)
                                           .requiresCorrectToolForDrops(true)
                    )
                    .withBlockDisplay(builder1 ->
                        builder1.withFullBlockTexture(new Asset<>("/debug/textures/block/tin_ore.png"))
                                .withDestroyParticles(Bukkit.createBlockData(Material.STONE)))
            )
        );

    public static final FakeBlock DEBUG_BLOCK_USING_NOTE_BLOCK = MCCreativeLabExtension
        .getCustomBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "test_fake_block2"), CustomOreBlock.class)
            .withBlockState(builder ->
                builder
                    .withBlockProperties(fakeBlockProperties ->
                        fakeBlockProperties.isRandomlyTicking(true)
                                           .withBlockHardness(4)
                                           .withExplosionResistance(6)
                                           .requiresCorrectToolForDrops(true)
                    )
                    .withBlockDisplay(builder1 ->
                        builder1.withFullBlockTexture(new Asset<>("/debug/textures/block/tin_ore.png")).useUnusedBlockState(Material.NOTE_BLOCK)
                    )
            )
        );

    public static void init() {
    }
}

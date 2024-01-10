package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.item.FakeItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class Debug {

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
                    .withBlockDisplay(builder1 ->
                        builder1.withFullBlockTexture(new Asset<>("/debug/textures/block/tin_ore.png"))
                                .withDestroyParticles(Bukkit.createBlockData(Material.STONE)))
            )
        );

    public static final FakeBlock DEBUG_BLOCK_USING_NOTE_BLOCK = MCCreativeLabExtension
        .getFakeBlockRegistry()
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

    public static final FakeItem DEBUG_ITEM = MCCreativeLabExtension
        .getFakeItemRegistry()
        .register(
            new FakeItem.Builder<>(new NamespacedKey("mccreativelab", "debug_item"), Material.STICK, DebugItem.class)
                .withProperties(new FakeItem.FakeItemProperties().stacksTo(4).fireResistant())
                .withItemMeta(meta -> meta.displayName(Component.text("Debug Item")))
        );
    public static void init(){

    }
}

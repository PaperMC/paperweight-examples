package de.verdox.mccreativelab.world.block.replaced;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.behaviour.ReplaceVanillaBlockStatesBehaviour;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Ageable;

import java.util.Map;

public class ReplacedBlocks {
    public static void init(){}
    public static final FakeBlock WHEAT = MCCreativeLabExtension
        .getFakeBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "wheat"), ReplacedCrop.class)
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage0"), ReplacedCrop.createCropHitbox(0), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(0))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage1"), ReplacedCrop.createCropHitbox(1), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(1))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage2"), ReplacedCrop.createCropHitbox(2), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(1))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage3"), ReplacedCrop.createCropHitbox(3), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(2))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage4"), ReplacedCrop.createCropHitbox(4), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(2))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage5"), ReplacedCrop.createCropHitbox(5), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(3))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage6"), ReplacedCrop.createCropHitbox(6), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(3))))
            .withBlockState(builder -> ReplacedCrop.createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage7"), ReplacedCrop.createCropHitbox(7), () -> Bukkit.createBlockData(Material.BIRCH_FENCE))
            ));

    public static final FakeBlock SWEET_BERRY = MCCreativeLabExtension
        .getFakeBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab","sweet_berry_bush"), ReplacedBerry.class)
            .withBlockState(builder -> ReplacedBerry.createFakeBerryBlockState(builder, new NamespacedKey("minecraft","block/sweet_berry_bush_stage0"), ReplacedBerry.createBerryHitbox(0), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(0))))
            .withBlockState(builder -> ReplacedBerry.createFakeBerryBlockState(builder, new NamespacedKey("minecraft","block/sweet_berry_bush_stage1"), ReplacedBerry.createBerryHitbox(1), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(1))))
            .withBlockState(builder -> ReplacedBerry.createFakeBerryBlockState(builder, new NamespacedKey("minecraft","block/sweet_berry_bush_stage2"), ReplacedBerry.createBerryHitbox(2), () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(2))))
            .withBlockState(builder -> ReplacedBerry.createFakeBerryBlockState(builder, new NamespacedKey("minecraft","block/sweet_berry_bush_stage3"), ReplacedBerry.createBerryHitbox(3), () -> Bukkit.createBlockData(Material.TORCHFLOWER_CROP, blockData -> ((Ageable) blockData).setAge(1))))
        );
    public static void setup(){
        BlockBehaviour.BLOCK_BEHAVIOUR.setBehaviour(Material.WHEAT, new ReplaceVanillaBlockStatesBehaviour(
            Map.of(
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(0)), ReplacedBlocks.WHEAT.getBlockState(0),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(1)), ReplacedBlocks.WHEAT.getBlockState(1),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(2)), ReplacedBlocks.WHEAT.getBlockState(2),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(3)), ReplacedBlocks.WHEAT.getBlockState(3),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(4)), ReplacedBlocks.WHEAT.getBlockState(4),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(5)), ReplacedBlocks.WHEAT.getBlockState(5),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(6)), ReplacedBlocks.WHEAT.getBlockState(6),
                Bukkit.createBlockData(Material.WHEAT, (data) -> ((Ageable) data).setAge(7)), ReplacedBlocks.WHEAT.getBlockState(7)
            )
        ));

        BlockBehaviour.BLOCK_BEHAVIOUR.setBehaviour(Material.SWEET_BERRY_BUSH, new ReplaceVanillaBlockStatesBehaviour(
            Map.of(
                Bukkit.createBlockData(Material.SWEET_BERRY_BUSH, (data) -> ((Ageable) data).setAge(0)), ReplacedBlocks.SWEET_BERRY.getBlockState(0),
                Bukkit.createBlockData(Material.SWEET_BERRY_BUSH, (data) -> ((Ageable) data).setAge(1)), ReplacedBlocks.SWEET_BERRY.getBlockState(1),
                Bukkit.createBlockData(Material.SWEET_BERRY_BUSH, (data) -> ((Ageable) data).setAge(2)), ReplacedBlocks.SWEET_BERRY.getBlockState(2),
                Bukkit.createBlockData(Material.SWEET_BERRY_BUSH, (data) -> ((Ageable) data).setAge(3)), ReplacedBlocks.SWEET_BERRY.getBlockState(3)
            )
        ));
    }
}

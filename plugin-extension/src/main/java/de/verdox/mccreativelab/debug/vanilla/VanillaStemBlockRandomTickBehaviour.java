package de.verdox.mccreativelab.debug.vanilla;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.concurrent.ThreadLocalRandom;

public class VanillaStemBlockRandomTickBehaviour extends VanillaCropRandomTickBehaviour {
    public static BlockFace[] HORIZONTAL_PLANE = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    public VanillaStemBlockRandomTickBehaviour(int minLightLevel) {
        super(minLightLevel);
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        var lightLevel = block.getLightLevel();
        if (lightLevel < 9)
            return voidResult();

        float growthSpeed = calculateCropGrowthSpeed(block);
        var randomNumber = drawRandomNumber(vanillaRandomSource);

        int modifier;
        Material fruit;
        Material attachedStem;
        if (block.getType().equals(Material.PUMPKIN_STEM)) {
            modifier = getAndValidateGrowth("Pumpkin");
            fruit = Material.PUMPKIN;
            attachedStem = Material.ATTACHED_PUMPKIN_STEM;
        } else {
            modifier = getAndValidateGrowth("Melon");
            fruit = Material.MELON;
            attachedStem = Material.ATTACHED_MELON_STEM;
        }

        if (randomNumber >= (modifier) / (100.0f * (Math.floor((25.0F / growthSpeed) + 1))))
            return voidResult();

        var ageable = (Ageable) block.getBlockData();
        var age = ageable.getAge();
        var maxAge = ageable.getMaximumAge();

        if (age < maxAge) {
            ageUpAndCallBlockGrowEvent(block, ageable, age + 1);
            return voidResult();
        }
        var randomBlockFace = HORIZONTAL_PLANE[ThreadLocalRandom.current().nextInt(HORIZONTAL_PLANE.length)];
        var relativeBlockPos = getRelative(block.getLocation(), randomBlockFace);
        var relativeBlock = relativeBlockPos.getBlock();
        var belowRelativPos = relativeBlock.getRelative(0, -1, 0);

        if (belowRelativPos.getType().isAir() && (relativeBlock.getType().equals(Material.FARMLAND) || relativeBlock
            .getType().equals(Material.DIRT))) {

            if (!handleBlockGrowEvent(relativeBlock, Bukkit.createBlockData(fruit)))
                return voidResult();

            var stemBlockData = Bukkit.createBlockData(attachedStem, blockData -> ((Directional) blockData).setFacing(randomBlockFace));
            block.setBlockData(stemBlockData, true);
        }
        return voidResult();
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return bool(true);
    }
}

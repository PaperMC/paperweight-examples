package de.verdox.mccreativelab.debug.vanilla;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaCropRandomTickBehaviour extends CropRandomTickBehaviour {
    public VanillaCropRandomTickBehaviour(int minLightLevel) {
        super(minLightLevel);
    }

    @Override
    protected boolean validate(Block block) {
        return block.getBlockData() instanceof Ageable;
    }

    @Override
    protected int getAge(Block block) {
        return ((Ageable) block.getBlockData()).getAge();
    }

    @Override
    protected int getMaxAge(Block block) {
        return ((Ageable) block.getBlockData()).getMaximumAge();
    }

    @Override
    protected void ageUpCrop(Block block, int newAge) {
        ageUpAndCallBlockGrowEvent(block, (Ageable) block.getBlockData(), newAge);
    }

    @Override
    protected boolean canGrow(Block block, VanillaRandomSource vanillaRandomSource) {
        float growthSpeed = calculateCropGrowthSpeed(block);

        int modifier;
        if (block.getType().equals(Material.BEETROOT))
            modifier = getAndValidateGrowth("Beetroot");
        else if (block.getType().equals(Material.CARROT))
            modifier = getAndValidateGrowth("Carrot");
        else if (block.getType().equals(Material.POTATO))
            modifier = getAndValidateGrowth("Potato");
        else if (block.getType().equals(Material.TORCHFLOWER_CROP))
            modifier = getAndValidateGrowth("TorchFlower");
        else
            modifier = getAndValidateGrowth("Wheat");

        var randomNumber = drawRandomNumber(vanillaRandomSource);

        return randomNumber < (modifier / (100.0f * (Math.floor((25.0F / growthSpeed) + 1))));
    }

    @Override
    protected int getBoneMealAgeIncrease(Block block, Random random) {
        return random.nextInt(2, 5);
    }

    @Override
    protected boolean isSameCrop(Block block, Location relativePos) {
        return block.getWorld().getBlockState(relativePos).getType().equals(block.getType());
    }

    protected void ageUpAndCallBlockGrowEvent(Block block, Ageable ageable, int newAge) {
        var stateSnapshot = block.getState();
        ageable.setAge(Math.min(ageable.getMaximumAge(), newAge));
        stateSnapshot.setBlockData(ageable);
        handleBlockGrowEvent(block, stateSnapshot);
    }

    protected boolean handleBlockGrowEvent(Block block, BlockState newBlockState) {
        BlockGrowEvent event = new BlockGrowEvent(block, newBlockState);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            block.setBlockData(newBlockState.getBlockData());
        }
        return !event.isCancelled();
    }

    protected boolean handleBlockGrowEvent(Block block, BlockData blockData) {
        var blockState = block.getState(true);
        blockState.setBlockData(blockData);
        return handleBlockGrowEvent(block, blockState);
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        return isBlockDataRandomlyTicking(blockData);
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        if (!(blockData instanceof Ageable ageable))
            return bool(false);
        return bool(ageable.getAge() < ageable.getMaximumAge());
    }
}

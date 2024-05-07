package de.verdox.mccreativelab.debug.vanilla;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public abstract class CropRandomTickBehaviour implements BlockBehaviour {

    private final int minLightLevel;

    public CropRandomTickBehaviour(int minLightLevel){
        this.minLightLevel = minLightLevel;
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        var lightLevel = block.getLightLevel();
        if (lightLevel < minLightLevel)
            return voidResult();

        int age = getAge(block);
        int maxAge = getMaxAge(block);

        if (age < maxAge) {
            if(canGrow(block, vanillaRandomSource)){
                ageUpCrop(block, age + 1);
            }
        }
        return voidResult();
    }

    protected abstract boolean validate(Block block);
    protected abstract int getAge(Block block);
    protected abstract int getMaxAge(Block block);
    protected abstract boolean isSameCrop(Block block, Location relativePos);
    protected abstract void ageUpCrop(Block block, int newAge);
    protected abstract boolean canGrow(Block block, VanillaRandomSource vanillaRandomSource);
    protected abstract int getBoneMealAgeIncrease(Block block, Random random);

    protected float drawRandomNumber(VanillaRandomSource vanillaRandomSource){
        return vanillaRandomSource.nextFloat();
    }

    @Override
    public BehaviourResult.Bool fertilizeAction(Block block, ItemStack stack) {
        System.out.println("Fertilizing");
        return BlockBehaviour.super.fertilizeAction(block, stack);
    }



    protected float calculateCropGrowthSpeed(Block block) {
        float growthSpeed = 1.0F;
        var pos = block.getLocation();
        var world = block.getWorld();
        var farmLandPos = pos.getBlock().getRelative(BlockFace.DOWN).getLocation();

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float tempGrowthSpeedValueThisBlock = 0.0F;
                var blockState = world.getBlockState(farmLandPos.clone().add(i, 0, j));

                if (blockState.getType().equals(Material.FARMLAND)) {
                    var farmLand = ((Farmland) blockState.getBlockData());
                    tempGrowthSpeedValueThisBlock = calculateGrowthSpeedValueBasedOnFarmland(farmLand);
                }

                if (i != 0 || j != 0)
                    tempGrowthSpeedValueThisBlock /= 4.0F;


                growthSpeed += tempGrowthSpeedValueThisBlock;
            }
        }

        var northBlockPos = getRelative(pos, BlockFace.NORTH);
        var southBlockPos = getRelative(pos, BlockFace.SOUTH);
        var westBlockPos = getRelative(pos, BlockFace.WEST);
        var eastBlockPos = getRelative(pos, BlockFace.EAST);
        boolean westOrEastSameCrop = isSameCrop(block, westBlockPos) || isSameCrop(block, eastBlockPos);
        boolean northOrWestSameCrop = isSameCrop(block, northBlockPos) || isSameCrop(block, southBlockPos);

        if (westOrEastSameCrop && northOrWestSameCrop)
            growthSpeed /= 2.0F;
        else {
            boolean sameCropDiagonal =
                isSameCrop(block, getRelative(westBlockPos, BlockFace.NORTH))
                    || isSameCrop(block, getRelative(eastBlockPos, BlockFace.NORTH))
                    || isSameCrop(block, getRelative(eastBlockPos, BlockFace.SOUTH))
                    || isSameCrop(block, getRelative(westBlockPos, BlockFace.SOUTH));

            if (sameCropDiagonal)
                growthSpeed /= 2.0F;
        }

        return growthSpeed;
    }

    protected float calculateGrowthSpeedValueBasedOnFarmland(Farmland farmLand) {
        float tempGrowthSpeedValueThisBlock;
        tempGrowthSpeedValueThisBlock = 1.0F;
        if (farmLand.getMoisture() > 0) {
            tempGrowthSpeedValueThisBlock = 3.0F;
        }
        return tempGrowthSpeedValueThisBlock;
    }

    protected final Location getRelative(Location location, BlockFace face) {
        return location.getBlock().getRelative(face).getLocation();
    }

    protected final int getAndValidateGrowth(String crop) {
        var config = Bukkit.spigot().getSpigotConfig();
        int modifier = config.getInt("growth." + crop.toLowerCase(java.util.Locale.ENGLISH) + "-modifier", 100);
        if (modifier == 0)
            modifier = 100;
        return modifier;
    }
}

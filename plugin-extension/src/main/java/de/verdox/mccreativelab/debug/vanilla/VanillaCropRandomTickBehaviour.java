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

import java.util.concurrent.ThreadLocalRandom;

public class VanillaCropRandomTickBehaviour implements BlockBehaviour {
    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        var lightLevel = block.getLightLevel();
        if (lightLevel < 9)
            return voidResult();

        Ageable ageable = (Ageable) block.getBlockData();
        var age = ageable.getAge();

        if (age < ageable.getMaximumAge()) {
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

            var randomNumber = drawRandomNumber();

            if(canGrow(block, growthSpeed, modifier, randomNumber)){
                ageUpAndCallBlockGrowEvent(block, ageable);
            }
        }
        return voidResult();
    }


    protected void ageUpAndCallBlockGrowEvent(Block block, Ageable ageable) {
        var stateSnapshot = block.getState();
        ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + 1));
        stateSnapshot.setBlockData(ageable);
        handleBlockGrowEvent(block, stateSnapshot);
    }

    protected boolean handleBlockGrowEvent(Block block, BlockState newBlockState){
        BlockGrowEvent event = new BlockGrowEvent(block, newBlockState);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            block.setBlockData(newBlockState.getBlockData());
        }
        return !event.isCancelled();
    }

    protected boolean handleBlockGrowEvent(Block block, BlockData blockData){
        var blockState = block.getState(true);
        blockState.setBlockData(blockData);
        return handleBlockGrowEvent(block, blockState);
    }

    protected float drawRandomNumber(){
        return ThreadLocalRandom.current().nextFloat();
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        return isBlockDataRandomlyTicking(blockData);
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        if(!(blockData instanceof Ageable ageable))
            return bool(false);
        return bool(ageable.getAge() < ageable.getMaximumAge());
    }

    protected boolean canGrow(Block block, float growthSpeed, float spigotConfigModifier, float minecraftRandomNumber){
        return minecraftRandomNumber < (spigotConfigModifier / (100.0f * (Math.floor((25.0F / growthSpeed) + 1))));
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

    protected boolean isSameCrop(Block block, Location relativePos) {
        return block.getWorld().getBlockState(relativePos).getType().equals(block.getType());
    }
}

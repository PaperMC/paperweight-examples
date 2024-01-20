package de.verdox.mccreativelab.debug.vanilla;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.util.BlockVector;

public class VanillaFarmBlockRandomTickBehaviour implements BlockBehaviour {
    private final int farmlandWaterRadius;

    public VanillaFarmBlockRandomTickBehaviour(){
        this(4);
    }

    public VanillaFarmBlockRandomTickBehaviour(int farmlandWaterRadius){
        this.farmlandWaterRadius = farmlandWaterRadius;
    }
    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        Farmland farmlandData = (Farmland) block.getBlockData();
        int moisture = farmlandData.getMoisture();

        if (isNearWater(block, farmlandWaterRadius) || block.getWorld()
                                                              .isRainingAt(above(block).getLocation())) {
            if (moisture >= 7)
                return voidResult();
            handleMoistureChangeEvent(block, 7);
            return voidResult();
        }

        if (moisture > 0)
            handleMoistureChangeEvent(block, moisture - 1);
        else if (!shouldMaintainFarmland(block))
            turnToDirt(block);
        return voidResult();
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return bool(true);
    }

    private static boolean shouldMaintainFarmland(Block block) {
        return Tag.MAINTAINS_FARMLAND.isTagged(above(block).getType());
    }

    private static Block above(Block block) {
        return block.getRelative(0, 1, 0);
    }

    private static boolean isNearWater(Block block, int horizontalRadius) {
        int xOff = block.getX();
        int yOff = block.getY();
        int zOff = block.getZ();

        for (int dz = -horizontalRadius; dz <= horizontalRadius; ++dz) {
            int z = dz + zOff;
            for (int dx = -horizontalRadius; dx <= horizontalRadius; ++dx) {
                int x = xOff + dx;
                for (int dy = 0; dy <= 1; ++dy) {
                    int y = dy + yOff;
                    Block nearBlock = block.getWorld().getBlockAt(x, y, z);
                    if (nearBlock.getType().equals(Material.WATER))
                        return true;
                }
            }
        }

        return false;
    }

    private static boolean handleMoistureChangeEvent(Block block, int newMoistureLevel) {
        Farmland farmland = (Farmland) block.getBlockData();
        if (newMoistureLevel == farmland.getMoisture() || newMoistureLevel > farmland.getMaximumMoisture())
            return false;

        farmland.setMoisture(newMoistureLevel);
        var newState = block.getState();
        newState.setBlockData(farmland);

        MoistureChangeEvent event = new MoistureChangeEvent(block, newState);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            block.setBlockData(newState.getBlockData());
        }
        return !event.isCancelled();
    }

    private static void turnToDirt(Block block){
        BlockState newState = block.getState();
        newState.setType(Material.DIRT);

        BlockFadeEvent event = new BlockFadeEvent(block, newState);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        block.setType(Material.DIRT, true);
        block.getWorld().sendGameEvent(null, GameEvent.BLOCK_CHANGE, new BlockVector(block.getX(), block.getY(), block.getZ()));
    }
}

package de.verdox.mccreativelab.util;

import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkUtil {

    public static int getMinBuildHeight(World world){
        return world.getMinHeight();
    }

    public static int getMaxBuildHeight(World world){
        return world.getMaxHeight();
    }

    public static int getBlockHeightOfSectionIndex(World world, int sectionIndex){
        return SectionPos.sectionToBlockCoord(getSectionYFromSectionIndex(world, sectionIndex));
    }

    public static int getSectionIndexFromSectionY(World world, int coord) {
        return coord - getMinSection(world);
    }

    public static int getSectionsCount(World world) {
        return getMaxSection(world) - getMinSection(world);
    }

    public static int getSectionYFromSectionIndex(World world, int index) {
        return index + getMinSection(world);
    }

    public static int getSectionIndex(World world, int y) {
        return getSectionIndexFromSectionY(world, SectionPos.blockToSectionCoord(y));
    }

    public static int getMinSection(World world){
        return SectionPos.blockToSectionCoord(getMinBuildHeight(world));
    }

    public static int getMaxSection(World world){
        return SectionPos.blockToSectionCoord(getMaxBuildHeight(world) - 1) + 1;
    }

    public static int approximateChunkSurfaceSection(Chunk chunk){
        if(!chunk.isLoaded())
            return Integer.MIN_VALUE;
        int count = 0;
        int sum = 0;
        for(int x = 0; x < 16; x++){
            for(int z = 0; z < 16; z++){
                Location worldLocation = chunk.getBlock(x,0,z).getLocation();
                sum += chunk.getWorld().getHighestBlockYAt(worldLocation, HeightMap.WORLD_SURFACE);
                count++;
            }
        }
        int averageBlockHeight = sum / count;
        return getSectionIndex(chunk.getWorld(), averageBlockHeight);
    }

}

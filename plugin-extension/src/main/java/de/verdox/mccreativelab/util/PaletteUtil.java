package de.verdox.mccreativelab.util;

import org.bukkit.World;

public class PaletteUtil {

    public static int worldXToPaletteXCoordinate(int x){
        return Math.abs(x % 16);
    }

    public static int worldZToPaletteXCoordinate(int z){
        return Math.abs(z % 16);
    }

    public static int worldYCoordinateToPaletteCoordinate(int worldMinHeight, int worldMaxHeight, int y){
        if(y < worldMinHeight)
            throw new IllegalArgumentException(y+" can't be smaller than min height of world "+worldMinHeight);
        if(y > worldMaxHeight)
            throw new IllegalArgumentException(y+" can't be greater than max height of world "+worldMaxHeight);
        if(worldMinHeight > worldMaxHeight)
            throw new IllegalArgumentException("worldMinHeight is greater than worldMaxHeight");
        return y + Math.abs(worldMinHeight);
    }
    public static int worldYCoordinateToPaletteCoordinate(World world, int y){
        return worldYCoordinateToPaletteCoordinate(world.getMinHeight(), world.getMaxHeight(), y);
    }

    public static int getMaxYPaletteFromWorldLimits(int worldMinHeight, int worldMaxHeight){
        return Math.abs(worldMinHeight) + Math.abs(worldMaxHeight);
    }

    public static int getMaxYPaletteFromWorldLimits(World world){
        return getMaxYPaletteFromWorldLimits(world.getMinHeight(), world.getMaxHeight());
    }
}

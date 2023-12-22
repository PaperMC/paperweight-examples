package de.verdox.mccreativelab.blockbreak;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakSpeedSettings {
    private static final Map<Material, Float> customHardness = new HashMap<>();
    public static boolean hasCustomBlockHardness(Material material){
        return customHardness.containsKey(material);
    }

    static {
        customHardness.put(Material.TINTED_GLASS, 10f);
    }
    public static float getCustomBlockHardness(Material material){
        return customHardness.getOrDefault(material, material.getHardness());
    }
}

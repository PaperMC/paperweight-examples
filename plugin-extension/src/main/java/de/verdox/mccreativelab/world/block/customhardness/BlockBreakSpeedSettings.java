package de.verdox.mccreativelab.world.block.customhardness;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakSpeedSettings {
    private final Map<Material, Float> customHardness = new HashMap<>();
    public boolean hasCustomBlockHardness(Material material){
        return customHardness.containsKey(material);
    }
    public float getCustomBlockHardness(Material material){
        return customHardness.getOrDefault(material, material.getHardness());
    }

    public void registerCustomBlockHardness(Material material, float hardness){
        customHardness.put(material, hardness);
    }
}

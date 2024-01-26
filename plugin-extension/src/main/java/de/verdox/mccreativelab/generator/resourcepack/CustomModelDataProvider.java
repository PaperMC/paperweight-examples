package de.verdox.mccreativelab.generator.resourcepack;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class CustomModelDataProvider {
    private static final Map<Material, Integer> drawnModelData = new HashMap<>();
    public static int drawCustomModelData(Material material){
        int modelData = drawnModelData.computeIfAbsent(material, material1 -> 5000);
        drawnModelData.put(material, modelData+1);
        return modelData;
    }
}

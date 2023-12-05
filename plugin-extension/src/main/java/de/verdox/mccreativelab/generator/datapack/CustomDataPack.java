package de.verdox.mccreativelab.generator.datapack;

import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.CustomPack;

public class CustomDataPack extends CustomPack<CustomDataPack> {
    public CustomDataPack(String packName, int packFormat, String description, AssetPath savePath) {
        super(packName, packFormat, description, savePath);
    }

    @Override
    public void createDescriptionFile() {

    }

    @Override
    public String mainFolder() {
        return "data";
    }
}

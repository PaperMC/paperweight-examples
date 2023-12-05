package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.CustomPack;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public class CustomResourcePack extends CustomPack<CustomResourcePack> {
    public static final AssetPath resourcePacksFolder = AssetPath.buildPath("resourcePacks");
    public CustomResourcePack(String packName, int packFormat, String description, AssetPath savePath) {
        super(packName, packFormat, description, savePath);
    }

    @Override
    protected void includeThirdPartyFiles() {
        Asset<CustomResourcePack> spaceFont = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/font/default.json"));
        Asset<CustomResourcePack> spaceLanguage = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/lang/en_us.json"));
        Asset<CustomResourcePack> spaceSplitterTexture = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/textures/font/splitter.png"));
        Asset<CustomResourcePack> minecraftFontWithSpaceChars = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/minecraft/font/default.json"));

        try {
            minecraftFontWithSpaceChars.installAsset(this, new NamespacedKey("minecraft","default"), ResourcePackAssetTypes.FONT, "json");
            spaceFont.installAsset(this, new NamespacedKey("space","default"), ResourcePackAssetTypes.FONT, "json");
            spaceLanguage.installAsset(this, new NamespacedKey("space","en_us"), ResourcePackAssetTypes.LANG, "json");
            spaceSplitterTexture.installAsset(this, new NamespacedKey("space","font/splitter"), ResourcePackAssetTypes.TEXTURES, "png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createDescriptionFile() {
        var languagesJson = JsonObjectBuilder.create();

        //TODO Languages

        var mcMetaPreset = JsonObjectBuilder.create().add("language", languagesJson).build();

        JsonObjectBuilder.create(mcMetaPreset).add("pack",
            JsonObjectBuilder.create()
                             .add("pack_format", packFormat)
                             .add("description", description)
        );
        JsonUtil.writeJsonObjectToFile(mcMetaPreset, pathToSavePackDataTo.concatPath("pack.mcmeta").toPath().toFile());
    }

    @Override
    public String mainFolder() {
        return "assets";
    }
}

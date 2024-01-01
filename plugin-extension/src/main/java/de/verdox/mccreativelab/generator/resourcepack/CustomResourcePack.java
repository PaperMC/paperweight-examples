package de.verdox.mccreativelab.generator.resourcepack;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.CustomPack;
import de.verdox.mccreativelab.generator.Resource;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import de.verdox.mccreativelab.util.io.AssetUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomResourcePack extends CustomPack<CustomResourcePack> {
    public static final AssetPath resourcePacksFolder = AssetPath.buildPath("resourcePacks");
    private final Map<String, SoundFile> soundFilesPerNamespace = new HashMap<>();
    private final Map<Material, Set<ItemTextureData>> itemTextureDataPerMaterial = new HashMap<>();
    private final Map<Material, Set<AlternateBlockStateModel>> alternateBlockStateModels = new HashMap<>();


    public CustomResourcePack(String packName, int packFormat, String description, AssetPath savePath) {
        super(packName, packFormat, description, savePath);
    }

    @Override
    protected void includeThirdPartyFiles() {
        Asset<CustomResourcePack> spaceFont = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/font/default.json"));
        Asset<CustomResourcePack> spaceLanguage = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/lang/en_us.json"));
        Asset<CustomResourcePack> spaceSplitterTexture = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/textures/font/splitter.png"));
        Asset<CustomResourcePack> minecraftFontWithSpaceChars = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/font/default.json"));

        try {
            minecraftFontWithSpaceChars.installAsset(this, new NamespacedKey("minecraft", "default"), ResourcePackAssetTypes.FONT, "json");
            spaceFont.installAsset(this, new NamespacedKey("space", "default"), ResourcePackAssetTypes.FONT, "json");
            spaceLanguage.installAsset(this, new NamespacedKey("space", "en_us"), ResourcePackAssetTypes.LANG, "json");
            spaceSplitterTexture.installAsset(this, new NamespacedKey("space", "font/splitter"), ResourcePackAssetTypes.TEXTURES, "png");

            for(int i = 0; i < 10; i++){
                new Asset<CustomResourcePack>("/blockbreak/destroy_stage_"+i+".png").installAsset(this, new NamespacedKey("minecraft","item/destroy_stage_"+i), ResourcePackAssetTypes.TEXTURES, "png");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearResources() {
        super.clearResources();
        soundFilesPerNamespace.clear();
        itemTextureDataPerMaterial.clear();
    }

    @Override
    public File installPack() throws IOException {
        File file = super.installPack();
        for (Map.Entry<Material, Set<ItemTextureData>> materialSetEntry : itemTextureDataPerMaterial.entrySet()) {
            Material material = materialSetEntry.getKey();
            Set<ItemTextureData> itemTextureDataSet = materialSetEntry.getValue();
            ItemTextureData.createVanillaModelFile(material, itemTextureDataSet, this);
        }
        for (Map.Entry<Material, Set<AlternateBlockStateModel>> materialSetEntry : alternateBlockStateModels.entrySet()) {

            Material material = materialSetEntry.getKey();
            Set<AlternateBlockStateModel> alternateBlockStateModels = materialSetEntry.getValue();

            JsonObject jsonObject = AlternateBlockStateModel.createBlockStateJson(alternateBlockStateModels);

            AssetUtil.createJsonAssetAndInstall(jsonObject, this, material.getKey(), ResourcePackAssetTypes.BLOCK_STATES);
        }
        return file;
    }

    @Override
    public void register(Resource<CustomResourcePack> resource) {
        super.register(resource);
        if (resource instanceof SoundData soundData)
            soundFilesPerNamespace.computeIfAbsent(soundData.key().namespace(), namespace -> {
                SoundFile soundFile = new SoundFile(new NamespacedKey(namespace, "sounds"));
                register(soundFile);
                return soundFile;
            }).addSoundData(soundData);
        if(resource instanceof ItemTextureData itemTextureData)
            itemTextureDataPerMaterial.computeIfAbsent(itemTextureData.getMaterial(), material -> new HashSet<>()).add(itemTextureData);
        if(resource instanceof AlternateBlockStateModel alternateBlockStateModel){
            alternateBlockStateModels.computeIfAbsent(alternateBlockStateModel.getBlockData().getMaterial(), material -> new HashSet<>()).add(alternateBlockStateModel);
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

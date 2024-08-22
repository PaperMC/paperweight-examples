package de.verdox.mccreativelab.generator.resourcepack;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.CustomPack;
import de.verdox.mccreativelab.generator.Resource;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translation;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ShaderRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageFile;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.Resolution;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import de.verdox.mccreativelab.util.io.AssetUtil;
import de.verdox.mccreativelab.util.io.ZipUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class CustomResourcePack extends CustomPack<CustomResourcePack> {
    public static final AssetPath resourcePacksFolder = AssetPath.buildPath("resourcePacks");
    private final Map<String, SoundFile> soundFilesPerNamespace = new HashMap<>();
    private final Map<Material, Set<ItemTextureData>> itemTextureDataPerMaterial = new HashMap<>();
    private final Map<Material, Set<AlternateBlockStateModel>> alternateBlockStateModels = new HashMap<>();
    private final LanguageStorage languageStorage = new LanguageStorage(this);
    private final ResourcePackMapper resourcePackMapper = new ResourcePackMapper();
    private final ItemTextureData emptyItem;
    private final List<File> includedResourcePacks = new LinkedList<>();

    public CustomResourcePack(String packName, int packFormat, String description, AssetPath savePath) {
        super(packName, packFormat, description, savePath);
        emptyItem = new ItemTextureData(new NamespacedKey("fixedminecraft", "item/empty_item"), Material.GRAY_STAINED_GLASS_PANE, CustomModelDataProvider.drawCustomModelData(Material.GRAY_STAINED_GLASS_PANE), new Asset<>("/empty_block/textures/empty.png"), null);
        register(emptyItem);
    }

    @Override
    public void onShutdown() throws IOException {
        for (File includedResourcePack : includedResourcePacks) {
            FileUtils.deleteDirectory(includedResourcePack);
        }
    }

    public void includeThirdPartyResourcePack(Asset<CustomResourcePack> zipFile){
        File includedResourcePack = ZipUtil.extractFilesFromZipFileResource(zipFile.assetInputStream(), CustomResourcePack.resourcePacksFolder.toPath().toString());
        includedResourcePacks.add(includedResourcePack);
    }

    public ItemTextureData getEmptyItem() {
        return emptyItem;
    }

    public ResourcePackMapper getResourcePackMapper() {
        return resourcePackMapper;
    }

    public LanguageStorage getLanguageStorage() {
        return languageStorage;
    }

    public Translation addTranslation(Translation translation) {
        languageStorage.addTranslation(translation);
        return translation;
    }

    public Translatable addTranslation(Translatable translation) {
        languageStorage.addTranslation(translation);
        return translation;
    }

    public List<Translatable> addTranslations(List<Translatable> translations) {
        for (Translatable translation : translations)
            languageStorage.addTranslation(translation);
        return translations;
    }

    @Override
    protected void includeThirdPartyFiles() {
        Asset<CustomResourcePack> spaceFont = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/font/default.json"));
        Asset<CustomResourcePack> spaceLanguage = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/lang/en_us.json"));
        Asset<CustomResourcePack> spaceSplitterTexture = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/space/textures/font/splitter.png"));
        Asset<CustomResourcePack> minecraftFontWithSpaceChars = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/font/default.json"));

        AssetBasedResourcePackResource spaceFontResource = new AssetBasedResourcePackResource(new NamespacedKey("space", "default"), spaceFont, ResourcePackAssetTypes.FONT, "json");
        register(spaceFontResource);
        AssetBasedResourcePackResource spaceLanguageResource = new AssetBasedResourcePackResource(new NamespacedKey("space", "en_us"), spaceLanguage, ResourcePackAssetTypes.LANG, "json");
        register(spaceLanguageResource);
        AssetBasedResourcePackResource spaceSplitterTextureResource = new AssetBasedResourcePackResource(new NamespacedKey("space", "font/splitter"), spaceSplitterTexture, ResourcePackAssetTypes.TEXTURES, "png");
        register(spaceSplitterTextureResource);
        AssetBasedResourcePackResource minecraftFontWithSpaceCharsResource = new AssetBasedResourcePackResource(new NamespacedKey("minecraft", "default"), minecraftFontWithSpaceChars, ResourcePackAssetTypes.FONT, "json");
        register(minecraftFontWithSpaceCharsResource);

        for (Resolution value : Resolution.values())
            register(value.getResolutionItemModel());
    }

/*    private void shaderCompat() throws IOException {
        includeShaderSetupFile("rendertype_entity_cutout.json");
        includeShaderSetupFile("position_color.json");
        includeShaderSetupFile("rendertype_eyes.json");
        includeShaderSetupFile("rendertype_armor_entity_glint.json");
        includeShaderSetupFile("rendertype_entity_smooth_cutout.json");
        includeShaderSetupFile("rendertype_entity_translucent.json");
        includeShaderSetupFile("rendertype_entity_solid.json");
        includeShaderSetupFile("rendertype_energy_swirl.json");
        includeShaderSetupFile("rendertype_tripwire.json");
        includeShaderSetupFile("rendertype_text_intensity_see_through.json");
        includeShaderSetupFile("rendertype_text_intensity.json");
        includeShaderSetupFile("rendertype_outline.json");
        includeShaderSetupFile("position_tex.json");
        includeShaderSetupFile("rendertype_lines.json");
        includeShaderSetupFile("position_color_tex_lightmap.json");
        includeShaderSetupFile("rendertype_entity_no_outline.json");
        includeShaderSetupFile("rendertype_crumbling.json");
        includeShaderSetupFile("rendertype_text_background.json");
        includeShaderSetupFile("rendertype_entity_decal.json");
        includeShaderSetupFile("rendertype_armor_cutout_no_cull.json");
        includeShaderSetupFile("rendertype_entity_glint_direct.json");
        includeShaderSetupFile("rendertype_solid.json");
        includeShaderSetupFile("rendertype_gui_ghost_recipe_overlay.json");
        includeShaderSetupFile("rendertype_entity_cutout_no_cull_z_offset.json");
        includeShaderSetupFile("rendertype_entity_alpha.json");
        includeShaderSetupFile("rendertype_entity_glint.json");
        includeShaderSetupFile("particle.json");
        includeShaderSetupFile("rendertype_translucent.json");
        includeShaderSetupFile("position_tex_color.json");
        includeShaderSetupFile("blit_screen.json");
        includeShaderSetupFile("rendertype_item_entity_translucent_cull.json");
        includeShaderSetupFile("rendertype_entity_translucent_cull.json");
        includeShaderSetupFile("position.json");
        includeShaderSetupFile("rendertype_lightning.json");
        includeShaderSetupFile("rendertype_beacon_beam.json");
        includeShaderSetupFile("rendertype_text_see_through.json");
        includeShaderSetupFile("position_tex_color_normal.json");
        includeShaderSetupFile("rendertype_gui_text_highlight.json");
        includeShaderSetupFile("position_color_normal.json");
        includeShaderSetupFile("rendertype_entity_shadow.json");
        includeShaderSetupFile("rendertype_water_mask.json");
        includeShaderSetupFile("rendertype_glint_direct.json");
        includeShaderSetupFile("rendertype_leash.json");
        includeShaderSetupFile("rendertype_glint.json");
        includeShaderSetupFile("rendertype_cutout_mipped.json");
        includeShaderSetupFile("rendertype_translucent_no_crumbling.json");
        includeShaderSetupFile("rendertype_end_portal.json");
        includeShaderSetupFile("position_color_tex.json");
        includeShaderSetupFile("rendertype_end_gateway.json");
        includeShaderSetupFile("rendertype_translucent_moving_block.json");
        includeShaderSetupFile("rendertype_entity_translucent_emissive.json");
        includeShaderSetupFile("position_color_lightmap.json");
        includeShaderSetupFile("rendertype_gui.json");
        includeShaderSetupFile("rendertype_armor_glint.json");
        includeShaderSetupFile("rendertype_cutout.json");
        includeShaderSetupFile("position_tex_lightmap_color.json");
        includeShaderSetupFile("rendertype_gui_overlay.json");
        includeShaderSetupFile("rendertype_entity_cutout_no_cull.json");
        includeShaderSetupFile("rendertype_text_background_see_through.json");
        includeShaderSetupFile("rendertype_glint_translucent.json");
    }

    private void includeShaderSetupFile(String fileName) throws IOException {
        String key = FileUtils.filename(fileName);
        NamespacedKey namespacedKey = new NamespacedKey("minecraft", "core/" + key);
        Asset<CustomResourcePack> asset = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/resourcepack/shader/" + fileName));
        asset.installAsset(this, namespacedKey, ResourcePackAssetTypes.SHADERS, "json");

    }*/

    @Override
    public void clearResources() {
        super.clearResources();
        soundFilesPerNamespace.clear();
        itemTextureDataPerMaterial.clear();
        resourcePackMapper.clear();
    }

    @Override
    public File installPack(boolean reload) throws IOException {
        File file = super.installPack(reload);
        globalAssetInstallation();
        return file;
    }

    private void globalAssetInstallation() throws IOException {
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
        Bukkit.getLogger().info("Installing shader files");
        ShaderRendered.installShaderFileToPack(this);
        this.languageStorage.installLanguages();
    }

    @Override
    protected void onRegister(Resource<CustomResourcePack> resource) {
        if (resource instanceof SoundData soundData)
            soundFilesPerNamespace.computeIfAbsent(soundData.key().namespace(), namespace -> {
                SoundFile soundFile = new SoundFile(new NamespacedKey(namespace, "sounds"));
                register(soundFile);
                return soundFile;
            }).addSoundData(soundData);
        if (resource instanceof ItemTextureData itemTextureData)
            itemTextureDataPerMaterial.computeIfAbsent(itemTextureData.getMaterial(), material -> new HashSet<>())
                                      .add(itemTextureData);
        if (resource instanceof AlternateBlockStateModel alternateBlockStateModel) {
            alternateBlockStateModels
                .computeIfAbsent(alternateBlockStateModel.getBlockData().getMaterial(), material -> new HashSet<>())
                .add(alternateBlockStateModel);
        }
        if (resource instanceof LanguageFile languageFile) {

        }
        resourcePackMapper.register(resource);
    }

    @Override
    public void createDescriptionFile() throws IOException {
        JsonObjectBuilder languagesJson = JsonObjectBuilder.create();
        languageStorage
            .getCustomTranslations()
            .stream().map(Translation::languageInfo).forEach(languageInfo -> {
                languagesJson.add(languageInfo.identifier(),
                    JsonObjectBuilder.create().add("name", languageInfo.name())
                                     .add("region", languageInfo.region())
                                     .add("bidirectional", languageInfo.bidirectional()));
            });

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

package de.verdox.mccreativelab.generator.resourcepack.types;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.AssetUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Function;

public class ItemTextureData extends ResourcePackResource {
    private final Material material;
    private final int customModelData;
    private final Asset<CustomResourcePack> pngFile;
    private final @Nullable ModelType modelType;

    public ItemTextureData(@NotNull NamespacedKey namespacedKey,
                           @NotNull Material material,
                           int customModelData,
                           @Nullable Asset<CustomResourcePack> pngFile,
                           @Nullable ModelType modelType) {
        super(namespacedKey);
        this.material = material;
        this.customModelData = customModelData;
        this.pngFile = pngFile;
        this.modelType = modelType;
    }

    public ItemStack createItem(){
        return new CustomItemData(material, customModelData).createStack();
    }

    @Override
    public void installToDataPack(CustomResourcePack customPack) throws IOException {
        var hasCustomTexture = pngFile != null;

        if (hasCustomTexture)
            pngFile.installAsset(customPack, key(), ResourcePackAssetTypes.TEXTURES, "png");

        createModelFile(customPack);
        createVanillaModelFile(customPack);
    }

    private void createModelFile(CustomResourcePack customPack) {
        JsonObject jsonToWriteToFile = createModelJson(key(), modelType);

        AssetUtil.createJsonAssetAndInstall(jsonToWriteToFile, customPack, key(), ResourcePackAssetTypes.MODELS);
    }

    private void createVanillaModelFile(CustomResourcePack customPack) {
        if (customModelData == 0)
            return;
        NamespacedKey vanillaKey = new NamespacedKey(material.getKey().namespace(), "item/" + material.getKey()
                                                                                                      .getKey());
        JsonObject jsonToWriteToFile = createModelJson(vanillaKey, null);

        addCustomModelDataListToVanillaModelFile(jsonToWriteToFile);

        AssetUtil.createJsonAssetAndInstall(jsonToWriteToFile, customPack, vanillaKey, ResourcePackAssetTypes.MODELS);
    }

    private void addCustomModelDataListToVanillaModelFile(JsonObject jsonToWriteToFile) {
        var list = new LinkedList<JsonObject>();
        var builder = JsonObjectBuilder.create(jsonToWriteToFile)
                                       .getOrCreateArray("overrides", jsonArrayBuilder -> {
                                           jsonArrayBuilder.add(
                                               JsonObjectBuilder
                                                   .create()
                                                   .add("predicate",
                                                       JsonObjectBuilder
                                                           .create()
                                                           .add("custom_model_data", customModelData))
                                                   .add("model", key().toString()));
                                           jsonArrayBuilder.build()
                                                           .forEach(jsonElement -> list.add(jsonElement.getAsJsonObject()));
                                       });

        list.sort(Comparator.comparing(jsonElement -> jsonElement.getAsJsonObject().getAsJsonObject("predicate")
                                                                 .get("custom_model_data").getAsJsonPrimitive()
                                                                 .getAsInt()));

        var sortedArray = JsonArrayBuilder.create();
        list.forEach(jsonElement -> {
            sortedArray.add(JsonObjectBuilder.create(jsonElement));
        });
        builder.add("overrides", sortedArray);
    }

    private JsonObject createModelJson(NamespacedKey key, @Nullable ModelType modelType) {
        JsonObject jsonToWriteToFile;
        if (modelType != null)
            jsonToWriteToFile = modelType.modelCreator().apply(key);
        else {
            if (isHandheldItem(material))
                jsonToWriteToFile = ModelType.HAND_HELD.modelCreator.apply(key);
            else
                jsonToWriteToFile = ModelType.GENERATED_ITEM.modelCreator.apply(key);
        }
        return jsonToWriteToFile;
    }

    private boolean isHandheldItem(Material material) {
        return material.name().contains("SWORD") || material.name().contains("AXE") || material.name()
                                                                                               .contains("HOE") || material
            .name().contains("SHOVEL") || material.equals(Material.FISHING_ROD);
    }

    public record ModelType(String modelName, Function<NamespacedKey, JsonObject> modelCreator) {
        public static final ModelType GENERATED_ITEM = new ModelType("item/generated", namespacedKey ->
            JsonObjectBuilder.create().add("parent", "item/generated")
                             .add("textures", JsonObjectBuilder.create().add("layer0", namespacedKey.toString()))
                             .build());
        public static final ModelType HAND_HELD = new ModelType("item/handheld", namespacedKey ->
            JsonObjectBuilder.create().add("parent", "item/handheld")
                             .add("textures", JsonObjectBuilder.create().add("layer0", namespacedKey.toString()))
                             .build());
        public static final ModelType FAKE_CROP = new ModelType("minecraft:block/crop", namespacedKey ->
            JsonObjectBuilder.create().add("parent", "minecraft:block/crop")
                             .add("textures", JsonObjectBuilder.create().add("crop", namespacedKey.toString()))
                             .build());

        public static final ModelType CUBE_ALL = new ModelType("minecraft:block/cube_all", namespacedKey ->
            JsonObjectBuilder.create().add("parent", "minecraft:block/cube_all")
                             .add("textures", JsonObjectBuilder.create().add("all", namespacedKey.toString()))
                             .build());

        public static final ModelType CLICKABLE_ITEM = new ModelType("clickable_item", namespacedKey ->
            JsonObjectBuilder.create().add("parent", "item/generated")
                             .add("textures", JsonObjectBuilder.create().add("layer0", namespacedKey.toString()))
                             .add("display",
                                 JsonObjectBuilder.create().add("gui",
                                     JsonObjectBuilder.create()
                                                      .add("scale", JsonArrayBuilder
                                                          .create()
                                                          .add(1.2f)
                                                          .add(1)
                                                          .add(1))))
                             .build());
    }
}

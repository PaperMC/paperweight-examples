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
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
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

    public ItemStack createItem() {
        return new CustomItemData(material, customModelData).createStack();
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        var hasCustomTexture = pngFile != null;

        if (hasCustomTexture)
            pngFile.installAsset(customPack, key(), ResourcePackAssetTypes.TEXTURES, "png");

        createModelFile(customPack);
    }

    public static void createVanillaModelFile(Material material, Set<ItemTextureData> installedItems, CustomResourcePack customPack) {
        //TODO: Funktioniert noch nicht!
        NamespacedKey vanillaKey = new NamespacedKey(material.getKey().namespace(), "item/" + material.getKey()
                                                                                                      .getKey());
        JsonObject jsonToWriteToFile = createModelJson(material, vanillaKey, null);

        addCustomModelDataListToVanillaModelFile(installedItems, jsonToWriteToFile);

        AssetUtil.createJsonAssetAndInstall(jsonToWriteToFile, customPack, vanillaKey, ResourcePackAssetTypes.MODELS);
        Bukkit.getLogger()
              .info("Installing modified vanilla item model for " + vanillaKey + " with " + installedItems.size() + " entries");
    }

    private static void addCustomModelDataListToVanillaModelFile(Set<ItemTextureData> installedItems, JsonObject jsonToWriteToFile) {
        var list = new LinkedList<JsonObject>();
        var builder = JsonObjectBuilder.create(jsonToWriteToFile)
                                       .getOrCreateArray("overrides", jsonArrayBuilder -> {

                                           for (ItemTextureData installedItem : installedItems) {
                                               jsonArrayBuilder.add(
                                                   JsonObjectBuilder
                                                       .create()
                                                       .add("predicate",
                                                           JsonObjectBuilder
                                                               .create()
                                                               .add("custom_model_data", installedItem.customModelData))
                                                       .add("model", installedItem.key().toString()));
                                           }
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

    private void createModelFile(CustomResourcePack customPack) {
        JsonObject jsonToWriteToFile = createModelJson(this.material, key(), modelType);

        AssetUtil.createJsonAssetAndInstall(jsonToWriteToFile, customPack, key(), ResourcePackAssetTypes.MODELS);
    }

    private static JsonObject createModelJson(Material material, NamespacedKey key, @Nullable ModelType modelType) {
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

    private static boolean isHandheldItem(Material material) {
        return material.name().contains("SWORD") || material.name().contains("AXE") || material.name()
                                                                                               .contains("HOE") || material
            .name().contains("SHOVEL") || material.equals(Material.FISHING_ROD);
    }

    public record ModelType(String modelName, Function<NamespacedKey, JsonObject> modelCreator) {
        public static ModelType createModelForBlockItem(String modelName, NamespacedKey blockModel) {
            return new ModelType(modelName, namespacedKey ->
                JsonObjectBuilder.create().add("parent", blockModel.toString())
                                 .build());
        }

        public static ModelType createFullCubeWithSingleTexture(Keyed textureKey) {
            return createFullCubeWithSeparateTextures(Map.of(BlockFace.UP, textureKey, BlockFace.DOWN, textureKey, BlockFace.NORTH, textureKey, BlockFace.EAST, textureKey, BlockFace.SOUTH, textureKey, BlockFace.WEST, textureKey));
        }

        public static ModelType createFullCubeWithSeparateTextures(Map<BlockFace, ? extends Keyed> texturesPerBlockFace) {
            var textures = JsonObjectBuilder.create();
            texturesPerBlockFace.forEach((blockFace, itemTextureData) -> {
                textures.add(blockFace.name().toLowerCase(Locale.ROOT), itemTextureData.key().toString());
            });

            return new ModelType("", namespacedKey ->
                JsonObjectBuilder.create().add("parent", "block/cube")
                                 .add("textures", textures)
                                 .build());

        }

        public static ModelType createOnlyOneSideTextureOfCube(BlockFace face) {

            String faceName;
            if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN))
                faceName = face.name().toLowerCase(Locale.ROOT);
            else
                faceName = face.getOppositeFace().name().toLowerCase(Locale.ROOT);
            int posX = 0;
            int posY = 0;
            int posZ = 0;

            int sizeX = 0;
            int sizeY = 0;
            int sizeZ = 0;

            if (face == BlockFace.DOWN) {
                posY = 8;
                sizeY = 8;

                sizeX = 16;
                sizeZ = 16;
            } else if (face == BlockFace.EAST) {
                posX = 8;
                sizeX = 8;

                sizeY = 16;
                sizeZ = 16;
            } else if (face == BlockFace.NORTH) {
                posZ = 8;
                sizeZ = 8;

                sizeX = 16;
                sizeY = 16;
            } else if (face == BlockFace.SOUTH) {
                posZ = 8;
                sizeZ = 8;

                sizeX = 16;
                sizeY = 16;
            } else if (face == BlockFace.UP) {
                posY = 8;
                sizeY = 8;

                sizeX = 16;
                sizeZ = 16;
            } else if (face == BlockFace.WEST) {
                posX = 8;
                sizeX = 8;

                sizeY = 16;
                sizeZ = 16;
            }

            var element = JsonObjectBuilder.create()
                                           .add("from", JsonArrayBuilder.create().add(posX).add(posY).add(posZ))
                                           .add("to", JsonArrayBuilder.create().add(sizeX).add(sizeY).add(sizeZ))
                                           .add("faces",
                                               JsonObjectBuilder.create()
                                                                .add(faceName,
                                                                    JsonObjectBuilder.create()
                                                                                     .add("texture", "#" + faceName)
                                                                                     .add("cullface", faceName)
                                                                )
                                           );


            return new ModelType("", namespacedKey ->
                JsonObjectBuilder.create().add("parent", "block/block")
                                 .add("elements", JsonArrayBuilder.create().add(element))
                                 .add("textures",
                                     JsonObjectBuilder.create()
                                                      .add("particle", namespacedKey.toString())
/*                                                      .add("down", emptyTexture.toString())
                                                      .add("up", emptyTexture.toString())
                                                      .add("north", emptyTexture.toString())
                                                      .add("east", emptyTexture.toString())
                                                      .add("south", emptyTexture.toString())
                                                      .add("west", emptyTexture.toString())*/
                                                      .add(faceName, namespacedKey.toString())
                                 )
                                 .build());
        }

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

        public static final ModelType CUBE_ONLY_FACE_UP = new ModelType("minecraft:block/cube_all", namespacedKey ->
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

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Asset<CustomResourcePack> getPngFile() {
        return pngFile;
    }

    public ModelType getModelType() {
        return modelType;
    }
}

package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import org.bukkit.NamespacedKey;

public enum Resolution {
    FULL_HD(1920, 1080, new AssetBasedResourcePackResource(new NamespacedKey("mccreativelab", "item/menu/fullhd"), new Asset<>("/resolution/fullhdmenu.json"), ResourcePackAssetTypes.MODELS, "json")),
    HD(1280, 720, new AssetBasedResourcePackResource(new NamespacedKey("mccreativelab", "item/menu/hdmenu"), new Asset<>("/resolution/hdmenu.json"), ResourcePackAssetTypes.MODELS, "json")),
    ;
    private final int width;
    private final int height;
    private final AssetBasedResourcePackResource resolutionItemModel;

    Resolution(int width, int height, AssetBasedResourcePackResource resolutionItemModel) {
        this.width = width;
        this.height = height;
        this.resolutionItemModel = resolutionItemModel;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public static ItemTextureData.ModelType createModel(Resolution resolution, NamespacedKey textureKey) {
        return new ItemTextureData.ModelType(null, (namespacedKey, jsonObject) ->
            JsonObjectBuilder
                .create(jsonObject)
                .add("parent", resolution.getResolutionItemModel().getKey().asString())
                .add("textures",
                    JsonObjectBuilder.create().add("texture", textureKey.asString())
                )
        );
    }

    public AssetBasedResourcePackResource getResolutionItemModel() {
        return resolutionItemModel;
    }
}

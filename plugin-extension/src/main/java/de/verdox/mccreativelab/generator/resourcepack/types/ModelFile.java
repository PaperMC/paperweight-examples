package de.verdox.mccreativelab.generator.resourcepack.types;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.util.io.AssetUtil;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public class ModelFile extends ResourcePackResource {
    private final ItemTextureData.ModelType modelType;

    public ModelFile(NamespacedKey namespacedKey, ItemTextureData.ModelType modelType) {
        super(namespacedKey);
        this.modelType = modelType;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        JsonObject jsonToWriteToFile = new JsonObject();
        this.modelType.modelCreator().accept(getKey(), jsonToWriteToFile);
        AssetUtil.createJsonAssetAndInstall(jsonToWriteToFile, customPack, key(), ResourcePackAssetTypes.MODELS);
    }
}

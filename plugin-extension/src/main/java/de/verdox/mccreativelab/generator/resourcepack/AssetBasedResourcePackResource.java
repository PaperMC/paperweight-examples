package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetType;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public class AssetBasedResourcePackResource extends ResourcePackResource{
    private final Asset<CustomResourcePack> asset;
    private final AssetType<CustomResourcePack> assetType;
    private final String fileEnding;

    public AssetBasedResourcePackResource(NamespacedKey namespacedKey, Asset<CustomResourcePack> asset, AssetType<CustomResourcePack> assetType, String fileEnding) {
        super(namespacedKey);
        this.asset = asset;
        this.assetType = assetType;
        this.fileEnding = fileEnding;
    }

    public AssetType<CustomResourcePack> getAssetType() {
        return assetType;
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public Asset<CustomResourcePack> getAsset() {
        return asset;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        asset.installAsset(customPack, getKey(), assetType, fileEnding);
    }

    @Override
    public String toString() {
        return "AssetBasedResourcePackResource{" +
            "asset=" + asset +
            ", assetType=" + assetType +
            ", fileEnding='" + fileEnding + '\'' +
            '}';
    }
}

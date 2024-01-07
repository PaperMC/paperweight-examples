package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetType;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public class AssetBasedResourcePackResource extends ResourcePackResource{
    private final Asset<CustomResourcePack> asset;
    private final AssetType<CustomResourcePack> assetType;
    private final String fileEnding;

    public AssetBasedResourcePackResource(Asset<CustomResourcePack> asset, NamespacedKey namespacedKey, AssetType<CustomResourcePack> assetType, String fileEnding) {
        super(namespacedKey);
        this.asset = asset;
        this.assetType = assetType;
        this.fileEnding = fileEnding;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        asset.installAsset(customPack, getKey(), assetType, fileEnding);
    }
}

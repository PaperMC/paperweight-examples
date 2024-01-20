package de.verdox.mccreativelab.world.block.display;

import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.strategy.DummyBlockVisualStrategy;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.UnusedBlockStates;
import de.verdox.mccreativelab.generator.resourcepack.AlternateBlockStateModel;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReusedBlockStateDisplay extends FakeBlockDisplay {
    private final Map<String, Asset<CustomResourcePack>> textures;
    private final ItemTextureData.ModelType modelType;

    private ReusedBlockStateDisplay(NamespacedKey namespacedKey, FakeBlock.FakeBlockHitbox hitBox, Map<String, Asset<CustomResourcePack>> textures, ItemTextureData.ModelType modelType) {
        super(namespacedKey, DummyBlockVisualStrategy.INSTANCE, hitBox);
        this.textures = textures;
        this.modelType = modelType;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {

    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) {
        Map<String, NamespacedKey> textureNamesToKeyMapping = new HashMap<>();
        textures.forEach((s, customResourcePackAsset) -> {
            NamespacedKey assetKey = new NamespacedKey(getKey().namespace(), "block/" + getKey().getKey() + "/" + s);
            customPack.register(new AssetBasedResourcePackResource(assetKey, customResourcePackAsset, ResourcePackAssetTypes.TEXTURES, "png"));
            textureNamesToKeyMapping.put(s, assetKey);
        });
        ItemTextureData.ModelType modifiedModelType = ItemTextureData.ModelType.modifyExistingModelType(modelType, (namespacedKey, jsonObject) -> {
            JsonObjectBuilder.create(jsonObject).getOrCreateJsonObject("textures", jsonObjectBuilder -> {
                textureNamesToKeyMapping.forEach((s, namespacedKey1) -> jsonObjectBuilder.add(s, namespacedKey1.toString()));
            });
        });
        ItemTextureData itemTextureData = new ItemTextureData(getKeyOfFullDisplay(getKey()), getModelMaterial(), drawNewModelID(), null, modifiedModelType);
        customPack.register(itemTextureData);
        customPack.register(new AlternateBlockStateModel(hitBox.getBlockData(), getKeyOfFullDisplay(getKey())));
    }

    private NamespacedKey getKeyOfFullDisplay(NamespacedKey namespacedKey) {
        return new NamespacedKey(namespacedKey.namespace(), "block/" + namespacedKey.getKey() + "/display");
    }

    @Override
    public final boolean simulateDiggingParticles() {
        return false;
    }

    @Override
    public BlockData getDestroyParticleData() {
        return hitBox.getBlockData();
    }

    public static class Builder implements FakeBlockDisplay.Builder<ReusedBlockStateDisplay> {
        private final Map<String, Asset<CustomResourcePack>> textures = new HashMap<>();
        private FakeBlock.FakeBlockHitbox fakeBlockHitbox;
        private ItemTextureData.ModelType modelType;

        public Builder asFullBlockDisplay(Asset<CustomResourcePack> textureAsset) {
            withFakeHitbox(UnusedBlockStates.getUnusedBlockState(Material.NOTE_BLOCK));
            withTexture("all", textureAsset);
            withModel(ItemTextureData.ModelType.CUBE_ALL);
            return this;
        }

        public Builder withFakeHitbox(FakeBlock.FakeBlockHitbox fakeBlockHitbox) {
            this.fakeBlockHitbox = fakeBlockHitbox;
            return this;
        }

        public Builder withFakeHitbox(BlockData blockData) {
            return withFakeHitbox(FakeBlock.FakeBlockHitbox.createFakeBlockHitbox(blockData));
        }

        public Builder withTexture(String textureName, Asset<CustomResourcePack> textureAsset) {
            textures.put(textureName, textureAsset);
            return this;
        }

        public Builder withModel(ItemTextureData.ModelType modelType) {
            this.modelType = modelType;
            return this;
        }

        @Override
        public ReusedBlockStateDisplay build(NamespacedKey namespacedKey) {
            Objects.requireNonNull(modelType);
            Objects.requireNonNull(fakeBlockHitbox);
            return new ReusedBlockStateDisplay(namespacedKey, fakeBlockHitbox, textures, modelType);
        }
    }
}

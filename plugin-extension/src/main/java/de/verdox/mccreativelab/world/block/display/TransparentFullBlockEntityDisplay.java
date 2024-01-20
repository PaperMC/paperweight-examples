package de.verdox.mccreativelab.world.block.display;

import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.strategy.TransparentBlockVisualStrategy;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransparentFullBlockEntityDisplay extends FakeBlockDisplay{
    private static boolean IS_USED = false;
    public static boolean isUsed() {
        return IS_USED;
    }
    private final Map<String, Asset<CustomResourcePack>> textures;
    private final ItemTextureData.ModelType modelType;
    private final BlockData destroyParticleData;
    private ItemTextureData fullBlockFakeItem;
    private TransparentFullBlockEntityDisplay(NamespacedKey namespacedKey, Map<String, Asset<CustomResourcePack>> textures, ItemTextureData.ModelType modelType, BlockData destroyParticleData) {
        this(namespacedKey, textures, FakeBlock.FakeBlockHitbox.TRANSPARENT_BLOCK, modelType, destroyParticleData);
    }

    private TransparentFullBlockEntityDisplay(NamespacedKey namespacedKey, Map<String, Asset<CustomResourcePack>> textures, FakeBlock.FakeBlockHitbox fakeBlockHitbox, ItemTextureData.ModelType modelType, BlockData destroyParticleData) {
        super(namespacedKey, TransparentBlockVisualStrategy.INSTANCE, fakeBlockHitbox);
        this.textures = textures;
        this.modelType = modelType;
        this.destroyParticleData = destroyParticleData;
        IS_USED = true;
    }

    @Override
    public boolean simulateDiggingParticles() {
        return true;
    }

    @Override
    public BlockData getDestroyParticleData() {
        return destroyParticleData;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        textures.forEach((s, customResourcePackAsset) -> {
            NamespacedKey assetKey = new NamespacedKey(getKey().namespace(), getKey().getKey() + "_" + s);
            customPack.register(new AssetBasedResourcePackResource(assetKey, customResourcePackAsset, ResourcePackAssetTypes.TEXTURES, "png"));
        });
        fullBlockFakeItem = new ItemTextureData(getKey(), getModelMaterial(), drawNewModelID(), null, modelType);
        customPack.register(fullBlockFakeItem);
    }

    public ItemTextureData getFullBlockFakeItem() {
        return fullBlockFakeItem;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {

    }

    public static class Builder implements FakeBlockDisplay.Builder<TransparentFullBlockEntityDisplay>{
        private static final List<BlockFace> validFaces = List.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
        private final Map<String, Asset<CustomResourcePack>> textures = new HashMap<>();
        private ItemTextureData.ModelType modelType;
        private BlockData destroyParticleData = Bukkit.createBlockData(Material.STONE);
        private FakeBlock.FakeBlockHitbox fakeBlockHitbox;

        public Builder withTexture(String name, Asset<CustomResourcePack> blockTexture) {
            textures.put(name, blockTexture);
            return this;
        }

        public Builder withHitbox(FakeBlock.FakeBlockHitbox fakeBlockHitbox){
            this.fakeBlockHitbox = fakeBlockHitbox;
            return this;
        }

        public Builder withModel(ItemTextureData.ModelType modelType){
            this.modelType = modelType;
            return this;
        }

        public Builder withTopAndBottomTexture(Asset<CustomResourcePack> blockTexture) {
            withTexture("up", blockTexture);
            withTexture("down", blockTexture);
            return this;
        }

        public Builder withFullBlockTexture(Asset<CustomResourcePack> blockTexture) {
            for (BlockFace validFace : validFaces)
                withTexture(validFace.name().toLowerCase(), blockTexture);
            return this;
        }

        public Builder withSideTexture(Asset<CustomResourcePack> blockTexture) {
            withTexture("north", blockTexture);
            withTexture("east", blockTexture);
            withTexture("south", blockTexture);
            withTexture("west", blockTexture);
            return this;
        }

        public Builder withDestroyParticleData(BlockData destroyParticleData) {
            this.destroyParticleData = destroyParticleData;
            return this;
        }

        @Override
        public TransparentFullBlockEntityDisplay build(NamespacedKey namespacedKey) {
            Objects.requireNonNull(modelType);
            return new TransparentFullBlockEntityDisplay(namespacedKey, textures, fakeBlockHitbox, modelType, destroyParticleData);
        }
    }
}

package de.verdox.mccreativelab.world.block.replaced;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.world.block.display.TransparentFullBlockEntityDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.List;
import java.util.function.Supplier;

public class ReplacedCrop extends FakeBlock {
    protected ReplacedCrop(List<FakeBlockState> fakeBlockStates) {
        super(fakeBlockStates);
    }

    public static ItemTextureData.ModelType createFakeCropModel(NamespacedKey cropTexture){
        return new ItemTextureData.ModelType("minecraft:block/cube_all", (namespacedKey, jsonObject) ->
            JsonObjectBuilder.create(jsonObject)
                             .add("parent", "minecraft:block/crop")
                             .add("textures",
                                 JsonObjectBuilder.create().add("crop", cropTexture.asString()))
                             .build());
    }

    public static void createFakeCropBlockState(FakeBlock.FakeBlockState.Builder builder, NamespacedKey namespacedKey, FakeBlock.FakeBlockHitbox fakeBlockHitbox, Supplier<BlockData> blockDataFunction) {
        builder
            .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.fromVanillaBlockData(fakeBlockHitbox.getBlockData()))
            .withBlockDisplay(new TransparentFullBlockEntityDisplay.Builder()
                .withModel(ReplacedCrop.createFakeCropModel(namespacedKey))
                .withDestroyParticleData(blockDataFunction.get())
                .withHitbox(fakeBlockHitbox)
            );
    }

    public static FakeBlockHitbox createCropHitbox(int age) {
        if (age < 0 || age > 7)
            throw new IllegalStateException("Age must be between 0 and 7");
        return new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(age))) {
            @Override
            protected void makeInvisible(CustomResourcePack customResourcePack, Asset<CustomResourcePack> emptyBlockModelAsset, Asset<CustomResourcePack> emptyBlockStatesFile) {
                makeModelEmpty(customResourcePack, new NamespacedKey("minecraft", "block/wheat_stage" + age));
            }
        };
    }
}

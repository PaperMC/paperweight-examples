package de.verdox.mccreativelab.world.block.replaced;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.TransparentFullBlockEntityDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.List;
import java.util.function.Supplier;

public class ReplacedBerry extends FakeBlock {
    protected ReplacedBerry(List<FakeBlockState> fakeBlockStates) {
        super(fakeBlockStates);
    }

    public static ItemTextureData.ModelType createFakeBerryModel(NamespacedKey berryTexture){
        return new ItemTextureData.ModelType("minecraft:block/cube_all", (namespacedKey, jsonObject) ->
            JsonObjectBuilder.create(jsonObject)
                             .add("parent", "minecraft:block/cross")
                             .add("textures",
                                 JsonObjectBuilder.create().add("cross", berryTexture.asString()))
                             .build());
    }

    public static void createFakeBerryBlockState(FakeBlock.FakeBlockState.Builder builder, NamespacedKey namespacedKey, FakeBlock.FakeBlockHitbox fakeBlockHitbox, Supplier<BlockData> blockDataFunction) {
        builder
            .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.fromVanillaBlockData(fakeBlockHitbox.getBlockData()))
            .withBlockDisplay(new TransparentFullBlockEntityDisplay.Builder()
                .withModel(createFakeBerryModel(namespacedKey))
                .withDestroyParticleData(blockDataFunction.get())
                .withHitbox(fakeBlockHitbox)
            );
    }

    public static FakeBlockHitbox createBerryHitbox(int age) {
        if (age < 0 || age > 3)
            throw new IllegalStateException("Age must be between 0 and 3");
        return new FakeBlockHitbox(Bukkit.createBlockData(Material.SWEET_BERRY_BUSH, blockData -> ((Ageable) blockData).setAge(age))) {
            @Override
            protected void makeInvisible(CustomResourcePack customResourcePack, Asset<CustomResourcePack> emptyBlockModelAsset, Asset<CustomResourcePack> emptyBlockStatesFile) {
                makeModelEmpty(customResourcePack, new NamespacedKey("minecraft", "block/sweet_berry_bush_stage" + age));
            }
        };
    }
}

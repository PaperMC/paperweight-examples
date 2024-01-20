package de.verdox.mccreativelab.debug.block;

import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import org.bukkit.NamespacedKey;

import java.util.List;

public class CustomCrop extends FakeBlock {
    protected CustomCrop(List<FakeBlockState> fakeBlockStates) {
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
}

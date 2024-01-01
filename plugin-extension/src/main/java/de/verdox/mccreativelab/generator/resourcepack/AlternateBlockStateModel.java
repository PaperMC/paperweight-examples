package de.verdox.mccreativelab.generator.resourcepack;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import io.vertx.core.json.Json;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlternateBlockStateModel extends ResourcePackResource {
    private final BlockData blockData;
    private final NamespacedKey alternativeModelKey;

    public AlternateBlockStateModel(BlockData blockData, NamespacedKey alternativeModelKey) {
        super(blockData.getMaterial().getKey());
        this.blockData = blockData;
        this.alternativeModelKey = alternativeModelKey;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {}

    String getVariantName() {
        String regex = "\\[(.*?)\\]";
        String input = blockData.getAsString();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return matcher.group(1);
        else return "";
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public static JsonObject createBlockStateJson(Set<AlternateBlockStateModel> alternateBlockStateModels){
        var variants = JsonObjectBuilder.create();
        for (AlternateBlockStateModel alternateBlockStateModel : alternateBlockStateModels) {
            variants.add(alternateBlockStateModel.getVariantName(), JsonObjectBuilder.create().add("model", alternateBlockStateModel.alternativeModelKey.asString()));
        }
        return JsonObjectBuilder.create().add("variants",variants).build();
    }
}

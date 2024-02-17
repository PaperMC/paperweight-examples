package de.verdox.mccreativelab.generator.datapack.wrapper.elements;

import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import net.kyori.adventure.key.Key;

public class Tag implements RecipeIngredient {
    private final Key tagKey;

    public Tag(Key tagKey) {

        this.tagKey = tagKey;
    }

    @Override
    public JsonObjectBuilder toJson() {
        return JsonObjectBuilder.create().add("tag", tagKey.asString());
    }
}

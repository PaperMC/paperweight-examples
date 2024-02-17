package de.verdox.mccreativelab.generator.datapack.wrapper.elements;

import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import net.kyori.adventure.key.Key;

public class Item implements RecipeIngredient, RecipeResult {
    private final Key key;
    public Item(Key key){
        this.key = key;
    }
    @Override
    public JsonObjectBuilder toJson() {
        return JsonObjectBuilder.create().add("item", key.asString());
    }
}

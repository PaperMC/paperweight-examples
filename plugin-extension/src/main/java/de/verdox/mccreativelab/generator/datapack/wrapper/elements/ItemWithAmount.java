package de.verdox.mccreativelab.generator.datapack.wrapper.elements;

import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;

public class ItemWithAmount implements RecipeResult {
    private final Item item;
    private final int amount;

    public ItemWithAmount(Item item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    @Override
    public JsonObjectBuilder toJson() {
        return item.toJson().add("count", amount);
    }
}

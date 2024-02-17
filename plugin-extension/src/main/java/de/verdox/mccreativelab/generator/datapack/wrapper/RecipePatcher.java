package de.verdox.mccreativelab.generator.datapack.wrapper;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.datapack.wrapper.elements.Item;
import de.verdox.mccreativelab.generator.datapack.wrapper.elements.ItemWithAmount;
import de.verdox.mccreativelab.generator.datapack.wrapper.elements.RecipeIngredient;
import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;

public abstract class RecipePatcher extends JsonMinecraftWrapper {
    public RecipePatcher(JsonObject jsonObject) {
        super(jsonObject);
    }

    public RecipePatcher(JsonObjectBuilder jsonObjectBuilder) {
        super(jsonObjectBuilder);
    }

    public void setResult(Item item) {
        jsonObjectBuilder.add("result", item.toJson());
    }

    public interface RecipeWithItemAndAmountResult extends JsonBase {
        default void setResult(ItemWithAmount item) {
            getJsonBuilder().add("result", item.toJson());
        }
    }

    public interface SingleIngredientRecipe extends JsonBase {
        default void setIngredient(RecipeIngredient item) {
            getJsonBuilder().add("ingredient", item.toJson());
        }
    }

    public static class Shapeless extends RecipePatcher implements RecipeWithItemAndAmountResult {
        public Shapeless(JsonObject jsonObject) {
            super(jsonObject);
        }

        public Shapeless(JsonObjectBuilder jsonObjectBuilder) {
            super(jsonObjectBuilder);
        }

        public void clearIngredients() {
            jsonObjectBuilder.add("ingredients", JsonArrayBuilder.create());
        }

        public void add(RecipeIngredient... items) {
            jsonObjectBuilder.getAsJsonArray("ingredients", jsonArrayBuilder -> {
                if (jsonArrayBuilder.length() + items.length >= 9)
                    throw new IllegalStateException("There cant be more ingredients than 9");

                for (RecipeIngredient item : items)
                    jsonArrayBuilder.add(item.toJson());
            });
        }
    }

    public static class Shaped extends RecipePatcher implements RecipeWithItemAndAmountResult {
        public Shaped(JsonObject jsonObject) {
            super(jsonObject);
        }

        public Shaped(JsonObjectBuilder jsonObjectBuilder) {
            super(jsonObjectBuilder);
        }

        public void clearKeys() {
            jsonObjectBuilder.add("key", JsonObjectBuilder.create());
        }

        public void addKey(char character, RecipeIngredient item) {
            jsonObjectBuilder.getOrCreateJsonObject("key", keys -> keys.add(String.valueOf(character), item.toJson()));
        }

        public void setPattern(String... pattern) {
            if (pattern.length < 1 || pattern.length > 3)
                throw new IllegalArgumentException("You must provide 1-3 lines in a pattern.");
            var array = JsonArrayBuilder.create();
            for (String s : pattern) {
                if (s.length() != 3)
                    throw new IllegalArgumentException("A pattern line must contain 3 characters!");
                array.add(s);
            }
            jsonObjectBuilder.add("pattern", array);
        }
    }

    public static class Smelting extends RecipePatcher implements SingleIngredientRecipe {
        public Smelting(JsonObject jsonObject) {
            super(jsonObject);
        }

        public Smelting(JsonObjectBuilder jsonObjectBuilder) {
            super(jsonObjectBuilder);
        }

        public void setCookingTime(int ticks) {
            jsonObjectBuilder.add("cookingtime", ticks);
        }

        public void setExperience(float experience) {
            jsonObjectBuilder.add("experience", experience);
        }
    }

    public static class SmithingTrim extends RecipePatcher implements RecipeWithItemAndAmountResult {

        public SmithingTrim(JsonObject jsonObject) {
            super(jsonObject);
        }

        public SmithingTrim(JsonObjectBuilder jsonObjectBuilder) {
            super(jsonObjectBuilder);
        }

        public void setTemplate(RecipeIngredient ingredient) {
            jsonObjectBuilder.add("template", ingredient.toJson());
        }

        public void setBase(RecipeIngredient ingredient) {
            jsonObjectBuilder.add("base", ingredient.toJson());
        }

        public void setAddition(RecipeIngredient ingredient) {
            jsonObjectBuilder.add("addition", ingredient.toJson());
        }
    }

    public static class SmithingTransform extends SmithingTrim {
        public SmithingTransform(JsonObject jsonObject) {
            super(jsonObject);
        }

        public SmithingTransform(JsonObjectBuilder jsonObjectBuilder) {
            super(jsonObjectBuilder);
        }
    }

    public static class Stonecutting extends RecipePatcher implements SingleIngredientRecipe {
        public Stonecutting(JsonObject jsonObject) {
            super(jsonObject);
        }

        public Stonecutting(JsonObjectBuilder jsonObjectBuilder) {
            super(jsonObjectBuilder);
        }

        public void setCount(int count) {
            getJsonBuilder().add("count", count);
        }
    }
}

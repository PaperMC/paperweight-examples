package de.verdox.mccreativelab.util.gson;

import com.google.gson.JsonPrimitive;

public class JsonPrimitiveBuilder extends JsonElementBuilder<JsonPrimitive> {
    protected JsonPrimitiveBuilder(JsonPrimitive element) {
        super(element);
    }

    public static JsonPrimitiveBuilder create(Character character) {
        return new JsonPrimitiveBuilder(new JsonPrimitive(character));
    }

    public static JsonPrimitiveBuilder create(Number number) {
        return new JsonPrimitiveBuilder(new JsonPrimitive(number));
    }

    public static JsonPrimitiveBuilder create(String value) {
        return new JsonPrimitiveBuilder(new JsonPrimitive(value));
    }

    public static JsonPrimitiveBuilder create(Boolean value) {
        return new JsonPrimitiveBuilder(new JsonPrimitive(value));
    }
}

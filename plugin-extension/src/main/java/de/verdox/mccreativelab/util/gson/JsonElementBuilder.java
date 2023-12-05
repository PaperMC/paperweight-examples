package de.verdox.mccreativelab.util.gson;

import com.google.gson.JsonElement;

public class JsonElementBuilder<T extends JsonElement> {
    protected final T element;

    protected JsonElementBuilder(T element) {
        this.element = element;
    }

    public JsonObjectBuilder getAsJsonObject() {
        return new JsonObjectBuilder(element.getAsJsonObject());
    }

    public JsonArrayBuilder getAsJsonArray() {
        return new JsonArrayBuilder(element.getAsJsonArray());
    }

    public JsonPrimitiveBuilder getAsJsonPrimitive() {
        return new JsonPrimitiveBuilder(element.getAsJsonPrimitive());
    }
}

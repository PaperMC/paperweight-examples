package de.verdox.mccreativelab.util.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class JsonObjectBuilder extends JsonElementBuilder<JsonObject> {
    public JsonObjectBuilder(JsonObject element) {
        super(element);
    }

    public JsonObjectBuilder add(String property, JsonElementBuilder<?> builder) {
        element.add(property, builder.element);
        return this;
    }

    public JsonObjectBuilder add(String property, JsonElement jsonElement) {
        element.add(property, jsonElement);
        return this;
    }

    public JsonObjectBuilder addProperty(String property, String value) {
        element.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder add(String property, String value) {
        return addProperty(property, value);
    }

    public JsonObjectBuilder addProperty(String property, Boolean value) {
        element.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder add(String property, Boolean value) {
        return addProperty(property, value);
    }

    public JsonObjectBuilder addProperty(String property, Number value) {
        element.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder add(String property, Number value) {
        return addProperty(property, value);
    }

    public JsonObjectBuilder addProperty(String property, Character value) {
        element.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder add(String property, Character value) {
        return addProperty(property, value);
    }

    public JsonObjectBuilder remove(String property) {
        element.remove(property);
        return this;
    }

    public JsonElementBuilder<?> get(String property, Consumer<JsonElementBuilder<?>> consumer) {
        consumer.accept(new JsonElementBuilder<>(element.get(property)));
        return this;
    }

    public <T extends JsonElementBuilder<?>> T getOrCreate(String property, Supplier<T> supplier, Consumer<T> consumer) {
        if (!element.has(property))
            add(property, supplier.get());
        return (T) get(property, (Consumer<JsonElementBuilder<?>>) consumer);
    }

    public JsonObjectBuilder getOrCreateArray(String property, Consumer<JsonArrayBuilder> consumer) {
        if (!element.has(property))
            add(property, JsonArrayBuilder.create());
        return getAsJsonArray(property, consumer);
    }

    public JsonObjectBuilder getAsJsonPrimitive(String property, Consumer<JsonPrimitiveBuilder> consumer) {
        consumer.accept(new JsonPrimitiveBuilder(element.get(property).getAsJsonPrimitive()));
        return this;
    }

    public JsonObjectBuilder getAsJsonArray(String property, Consumer<JsonArrayBuilder> consumer) {
        consumer.accept(new JsonArrayBuilder(element.get(property).getAsJsonArray()));
        return this;
    }

    public JsonObjectBuilder getAsJsonObject(String property, Consumer<JsonObjectBuilder> consumer) {
        consumer.accept(new JsonObjectBuilder(element.get(property).getAsJsonObject()));
        return this;
    }

    public JsonObjectBuilder getOrCreateJsonObject(String property, Consumer<JsonObjectBuilder> consumer) {
        if (!element.has(property))
            add(property, JsonObjectBuilder.create());
        return getAsJsonObject(property, consumer);
    }

    public JsonObject build() {
        return element;
    }

    public static JsonObjectBuilder create() {
        return new JsonObjectBuilder(new JsonObject());
    }

    public static JsonObjectBuilder create(JsonObject jsonObject) {
        return new JsonObjectBuilder(jsonObject);
    }
}

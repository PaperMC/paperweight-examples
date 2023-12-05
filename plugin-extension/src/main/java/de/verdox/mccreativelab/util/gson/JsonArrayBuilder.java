package de.verdox.mccreativelab.util.gson;

import com.google.gson.JsonArray;

import java.util.function.Consumer;

public class JsonArrayBuilder extends JsonElementBuilder<JsonArray> {
    protected JsonArrayBuilder(JsonArray element) {
        super(element);
    }

    public JsonArrayBuilder add(Boolean value) {
        element.add(value);
        return this;
    }

    public JsonArrayBuilder add(Character value) {
        element.add(value);
        return this;
    }

    public JsonArrayBuilder add(Number value) {
        element.add(value);
        return this;
    }

    public JsonArrayBuilder add(String value) {
        element.add(value);
        return this;
    }

    public int length() {
        return element.size();
    }

    public JsonArrayBuilder add(JsonElementBuilder<?> value) {
        element.add(value.element);
        return this;
    }

    public JsonArrayBuilder set(int index, JsonElementBuilder<?> value) {
        element.set(index, value.element);
        return this;
    }

    public JsonArrayBuilder remove(int index) {
        element.remove(index);
        return this;
    }

    public JsonArrayBuilder remove(JsonElementBuilder<?> value) {
        element.remove(value.element);
        return this;
    }

    public JsonElementBuilder<?> get(int index, Consumer<JsonElementBuilder<?>> consumer) {
        consumer.accept(new JsonElementBuilder<>(element.get(index)));
        return this;
    }

    public JsonArray build() {
        return element;
    }

    public static JsonArrayBuilder create() {
        return new JsonArrayBuilder(new JsonArray());
    }

    public static JsonArrayBuilder create(int capacity) {
        return new JsonArrayBuilder(new JsonArray(capacity));
    }

    public static JsonArrayBuilder create(JsonArray jsonArray) {
        return new JsonArrayBuilder(jsonArray);
    }
}

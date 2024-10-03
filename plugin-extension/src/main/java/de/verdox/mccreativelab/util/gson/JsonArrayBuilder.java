package de.verdox.mccreativelab.util.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

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

    public JsonArrayBuilder add(JsonElement value) {
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

    public JsonArrayBuilder remove(String primitive) {
        return remove(JsonPrimitiveBuilder.create(primitive));
    }

    public JsonArrayBuilder remove(boolean primitive) {
        return remove(JsonPrimitiveBuilder.create(primitive));
    }

    public JsonArrayBuilder remove(Number primitive) {
        return remove(JsonPrimitiveBuilder.create(primitive));
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

    public static JsonArrayBuilder of(byte[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (byte j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }
    public static JsonArrayBuilder of(short[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (short j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }
    public static JsonArrayBuilder of(int[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (int j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }
    public static JsonArrayBuilder of(long[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (long j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }

    public static JsonArrayBuilder of(float[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (float j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }

    public static JsonArrayBuilder of(double[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (double j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }

    public static JsonArrayBuilder of(char[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (char j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }

    public static JsonArrayBuilder of(String[] array){
        JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
        for (String j : array)
            jsonArrayBuilder.add(j);
        return jsonArrayBuilder;
    }
}

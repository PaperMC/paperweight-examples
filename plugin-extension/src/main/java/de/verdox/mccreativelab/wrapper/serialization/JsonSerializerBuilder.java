package de.verdox.mccreativelab.wrapper.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.wrapper.JsonSerializer;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonSerializerBuilder<T> {
    private final String id;

    public static <T> JsonSerializerBuilder<T> create(String id, Class<? extends T> type) {
        return new JsonSerializerBuilder<>(id);
    }

    private Map<String, SerializableField<T, ?>> fields = new HashMap<>();
    private JsonSerializer<T> constructorSerializer;

    private JsonSerializerBuilder(String id) {
        this.id = id;
    }

    public <R> JsonSerializerBuilder<T> withField(String name, JsonSerializer<R> serializer, Function<T, R> getter, BiConsumer<T, R> setter) {
        fields.put(name, new SerializableField<>(name, serializer, getter, (t, o) -> setter.accept(t, (R) o)));
        return this;
    }

    public JsonSerializer<T> build() {
        final String id = this.id;
        if (this.constructorSerializer == null)
            throw new IllegalStateException("No constructor specified for json serializer");
        return new JsonSerializer<T>() {
            @Override
            public JsonElement toJson(T wrapped) {
                JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create(constructorSerializer.toJson(wrapped).getAsJsonObject());
                fields.forEach((s, serializableField) -> serializableField.write(jsonObjectBuilder, wrapped));
                return jsonObjectBuilder.build();
            }

            @Override
            public @Nullable T fromJson(JsonElement jsonElement) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                T wrapped = constructorSerializer.fromJson(jsonElement);
                fields.forEach((s, serializableField) -> {
                    Object value = serializableField.read(jsonObject);
                    if (serializableField.setter() != null)
                        serializableField.setter().accept(wrapped, value);
                });
                return wrapped;
            }

            @Override
            public String id() {
                return id;
            }
        };
    }

    public JsonSerializerBuilder<T> constructor(Supplier<T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> constructor.get());
        return this;
    }

    public <R1> JsonSerializerBuilder<T> constructor(
        SerializableField<T, R1> field1,
        Function<R1, T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            R1 r1 = field1.read(jsonObject);
            return constructor.apply(r1);
        }, field1);
        return this;
    }

    public <R1, R2> JsonSerializerBuilder<T> constructor(
        SerializableField<T, R1> field1,
        SerializableField<T, R2> field2,
        BiFunction<R1, R2, T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            R1 r1 = field1.read(jsonObject);
            R2 r2 = field2.read(jsonObject);
            return constructor.apply(r1, r2);
        }, field1, field2);
        return this;
    }

    public <R1, R2, R3> JsonSerializerBuilder<T> constructor(
        SerializableField<T, R1> field1,
        SerializableField<T, R2> field2,
        SerializableField<T, R3> field3,
        TriFunction<R1, R2, R3, T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            R1 r1 = field1.read(jsonObject);
            R2 r2 = field2.read(jsonObject);
            R3 r3 = field3.read(jsonObject);
            return constructor.apply(r1, r2, r3);
        }, field1, field2, field3);
        return this;
    }

    public <R1, R2, R3, R4> JsonSerializerBuilder<T> constructor(
        SerializableField<T, R1> field1,
        SerializableField<T, R2> field2,
        SerializableField<T, R3> field3,
        SerializableField<T, R4> field4,
        Function4<R1, R2, R3, R4, T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            R1 r1 = field1.read(jsonObject);
            R2 r2 = field2.read(jsonObject);
            R3 r3 = field3.read(jsonObject);
            R4 r4 = field4.read(jsonObject);
            return constructor.apply(r1, r2, r3, r4);
        }, field1, field2, field3, field4);
        return this;
    }

    public <R1, R2, R3, R4, R5> JsonSerializerBuilder<T> constructor(
        SerializableField<T, R1> field1,
        SerializableField<T, R2> field2,
        SerializableField<T, R3> field3,
        SerializableField<T, R4> field4,
        SerializableField<T, R5> field5,
        Function5<R1, R2, R3, R4, R5, T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            R1 r1 = field1.read(jsonObject);
            R2 r2 = field2.read(jsonObject);
            R3 r3 = field3.read(jsonObject);
            R4 r4 = field4.read(jsonObject);
            R5 r5 = field5.read(jsonObject);
            return constructor.apply(r1, r2, r3, r4, r5);
        }, field1, field2, field3, field4, field5);
        return this;
    }

    public <R1, R2, R3, R4, R5, R6> JsonSerializerBuilder<T> constructor(
        SerializableField<T, R1> field1,
        SerializableField<T, R2> field2,
        SerializableField<T, R3> field3,
        SerializableField<T, R4> field4,
        SerializableField<T, R5> field5,
        SerializableField<T, R6> field6,
        Function6<R1, R2, R3, R4, R5, R6, T> constructor) {
        this.constructorSerializer = new ConstructorSerializer(this.id, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            R1 r1 = field1.read(jsonObject);
            R2 r2 = field2.read(jsonObject);
            R3 r3 = field3.read(jsonObject);
            R4 r4 = field4.read(jsonObject);
            R5 r5 = field5.read(jsonObject);
            R6 r6 = field6.read(jsonObject);
            return constructor.apply(r1, r2, r3, r4, r5, r6);
        }, field1, field2, field3, field4, field5, field6);
        return this;
    }

    private class ConstructorSerializer implements JsonSerializer<T> {
        private final String id;
        private final Function<JsonElement, T> deserializer;
        private final SerializableField<T, ?>[] fields;

        @SafeVarargs
        private ConstructorSerializer(String id, Function<JsonElement, T> deserializer, SerializableField<T, ?>... fields) {
            this.id = id;
            this.deserializer = deserializer;
            this.fields = fields;
        }

        @Override
        public JsonElement toJson(T wrapped) {
            JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();
            for (SerializableField<T, ?> field : this.fields)
                field.write(jsonObjectBuilder, wrapped);
            return jsonObjectBuilder.build();
        }

        @Override
        public @Nullable T fromJson(JsonElement jsonElement) {
            return deserializer.apply(jsonElement);
        }

        @Override
        public String id() {
            return id;
        }
    }

}

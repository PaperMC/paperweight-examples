package de.verdox.mccreativelab.wrapper.serialization;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.wrapper.JsonSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record SerializableField<T, R>(@Nullable String fieldName, JsonSerializer<R> serializer, Function<T, R> getter, @Nullable BiConsumer<T, Object> setter) {
    public SerializableField(@Nullable String fieldName, JsonSerializer<R> serializer, Function<T, R> getter){
        this(fieldName, serializer, getter, null);
    }

    public SerializableField(JsonSerializer<R> serializer, Function<T, R> getter){
        this(null, serializer, getter, null);
    }
    JsonObjectBuilder write(JsonObjectBuilder jsonObjectBuilder, T wrapped) {
        jsonObjectBuilder.add(fieldName == null ? serializer.id() : fieldName, serializer.toJson(getter.apply(wrapped))).build();
        return jsonObjectBuilder;
    }

    R read(JsonObject jsonObject) {
        return serializer.fromJson(jsonObject.get(fieldName == null ? serializer.id() : fieldName));
    }
}

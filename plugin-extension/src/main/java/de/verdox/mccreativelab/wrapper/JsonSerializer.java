package de.verdox.mccreativelab.wrapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.verdox.itemformat.BasicItemFormat;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetFileStorage;
import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.AssetType;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translation;
import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.wrapper.serialization.JsonSerializerBuilder;
import de.verdox.mccreativelab.wrapper.serialization.SerializableField;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface JsonSerializer<T> {
    JsonElement toJson(T wrapped);

    @Nullable
    T fromJson(JsonElement jsonElement);

    String id();

    //TODO

    class PrimitiveSerializer<T> implements JsonSerializer<T> {
        public static final PrimitiveSerializer<Boolean> BOOLEAN = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsBoolean);
        public static final PrimitiveSerializer<String> STRING = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsString);
        public static final PrimitiveSerializer<Character> CHARACTER = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsCharacter);
        public static final PrimitiveSerializer<Number> NUMBER = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsNumber);
        public static final PrimitiveSerializer<Double> DOUBLE = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsDouble);
        public static final PrimitiveSerializer<Float> FLOAT = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsFloat);
        public static final PrimitiveSerializer<Long> LONG = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsLong);
        public static final PrimitiveSerializer<Integer> INTEGER = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsInt);
        public static final PrimitiveSerializer<Short> SHORT = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsShort);
        public static final PrimitiveSerializer<Byte> BYTE = new PrimitiveSerializer<>(JsonPrimitive::new, JsonElement::getAsByte);

        private final Function<T, JsonPrimitive> toFunction;
        private final Function<JsonElement, T> from;

        private PrimitiveSerializer(Function<T, JsonPrimitive> to, Function<JsonElement, T> from) {

            toFunction = to;
            this.from = from;
        }

        @Override
        public JsonElement toJson(T wrapped) {
            return toFunction.apply(wrapped);
        }

        @Override
        public @Nullable T fromJson(JsonElement jsonElement) {
            return from.apply(jsonElement);
        }

        @Override
        public String id() {
            return "primitive";
        }
    }

    class EnumSerializer<E extends Enum<E>> implements JsonSerializer<E> {
        private final String id;
        private final Class<? extends E> type;

        public static <E extends Enum<E>> EnumSerializer<E> create(String id, Class<? extends E> type) {
            return new EnumSerializer<>(id, type);
        }

        private EnumSerializer(String id, Class<? extends E> type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public JsonElement toJson(E wrapped) {
            return new JsonPrimitive(wrapped.name());
        }

        @Override
        public @Nullable E fromJson(JsonElement jsonElement) {
            return Enum.valueOf((Class<E>) type, jsonElement.getAsString());
        }

        @Override
        public String id() {
            return id;
        }
    }

    class ItemStackSerializer implements JsonSerializer<ItemStack> {
        public static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

        @Override
        public JsonElement toJson(ItemStack wrapped) {
            ItemStack toSerialize = wrapped.clone();
            if (MCCreativeLabExtension.isServerSoftware())
                toSerialize.editMeta(meta -> meta.getPersistentDataContainer().remove(BasicItemFormat.sessionIDKey));
            JsonObject jsonObject = Bukkit.getUnsafe().serializeItemAsJson(toSerialize);
            // We remove values that are only needed internally

            jsonObject.remove("DataVersion");
            return jsonObject;
        }

        @Override
        public String id() {
            return "item_stack";
        }

        @Override
        public @Nullable ItemStack fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = JsonObjectBuilder.create(jsonElement.getAsJsonObject())
                .add("DataVersion", Bukkit.getUnsafe().getDataVersion())
                .build();
            ItemStack itemStack = Bukkit.getUnsafe().deserializeItemFromJson(jsonObject);
            if (MCCreativeLabExtension.isServerSoftware())
                BasicItemFormat.applyConversionTag(itemStack);
            return itemStack;
        }
    }

    class NameSpacedAssetSerializer implements JsonSerializer<AssetBasedResourcePackResource> {
        private Plugin javaPlugin;

        public static NameSpacedAssetSerializer get(JavaPlugin javaPlugin) {
            return new NameSpacedAssetSerializer(javaPlugin);
        }

        private NameSpacedAssetSerializer(Plugin javaPlugin) {
            this.javaPlugin = javaPlugin;
        }

        @Override
        public JsonElement toJson(AssetBasedResourcePackResource wrapped) {
            AssetType<CustomResourcePack> assetType = wrapped.getAssetType();
            String fileEnding = wrapped.getFileEnding();
            NamespacedKey key = wrapped.getKey();
            return JsonObjectBuilder.create()
                .add("plugin", javaPlugin.getName())
                .add("assetType", assetType.resourceTypePath().toPath().toString())
                .add("key", key.asString())
                .add("fileEnding", fileEnding)
                .build();
        }

        @Override
        public String id() {
            return "name_spaced_asset";
        }

        @Override
        public @Nullable AssetBasedResourcePackResource fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String pluginName = javaPlugin.getName();
            NamespacedKey key = null;
            AssetType<CustomResourcePack> assetType = null;
            String fileEnding = null;

            if (jsonObject.has("plugin")) {
                pluginName = jsonObject.get("plugin").getAsString();
            }
            if (jsonObject.has("assetType"))
                assetType = new AssetType<>(AssetPath.buildPath(jsonObject.get("assetType").getAsString()));
            if (jsonObject.has("key"))
                key = NamespacedKey.fromString(jsonObject.get("key").getAsString());
            if (jsonObject.has("fileEnding"))
                fileEnding = jsonObject.get("fileEnding").getAsString();

            if (key == null)
                throw new IllegalStateException("No key specified for asset");
            if (assetType == null)
                throw new IllegalStateException("No asset type specified for asset " + key);
            if (fileEnding == null)
                throw new IllegalStateException("No file ending specified for asset " + key);

            return AssetFileStorage.get(pluginName).loadAsset(key, assetType, fileEnding);
        }
    }

    class TranslatableSerializer implements JsonSerializer<Translatable> {

        public static final TranslatableSerializer INSTANCE = new TranslatableSerializer();

        @Override
        public JsonElement toJson(Translatable wrapped) {
            return JsonObjectBuilder.create(toJsonWithoutKey(wrapped)).add("key", wrapped.key()).build();
        }

        @Override
        public String id() {
            return "translatable";
        }

        public JsonObject toJsonWithoutKey(Translatable wrapped) {
            JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
            wrapped.getCache().forEach((languageInfo, translation) ->
                jsonArrayBuilder.add(JsonObjectBuilder.create()
                    .add("language", languageInfo.identifier())
                    .add("translation", translation.content())));
            return JsonObjectBuilder.create()
                .add("translations", jsonArrayBuilder)
                .build();
        }

        @Override
        public @Nullable Translatable fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("key") || !jsonObject.has("translations"))
                return null;

            String key = jsonObject.get("key").getAsString();
            return fromJsonWithProvidedKey(jsonElement, key);
        }

        public @Nullable Translatable fromJsonWithProvidedKey(JsonElement jsonElement, String key) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("translations"))
                return null;
            JsonArray array = jsonObject.get("translations").getAsJsonArray();

            Translatable translatable = new Translatable(key);

            for (JsonElement element : array) {
                JsonObject translationJson = element.getAsJsonObject();
                String languageID = translationJson.get("language").getAsString();
                String translation = translationJson.get("translation").getAsString();
                translatable.withAdditionalTranslation(new LanguageInfo(languageID, "", "", true), translation);
            }
            return translatable;
        }
    }

    abstract class CollectionSerializer<T, C extends Collection<T>> implements JsonSerializer<C> {
        protected final JsonSerializer<T> elementSerializer;

        private CollectionSerializer(JsonSerializer<T> elementSerializer) {
            this.elementSerializer = elementSerializer;
        }

        public static <T, C extends Collection<T>> CollectionSerializer<T, C> create(@NotNull JsonSerializer<T> serializer, @NotNull Supplier<C> collectionSupplier) {
            return new CollectionSerializer<>(serializer) {
                @Override
                protected C supplyCollection() {
                    return collectionSupplier.get();
                }

                @Override
                public String id() {
                    return "collection";
                }
            };
        }

        @Override
        public final JsonElement toJson(C wrapped) {
            JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();
            for (T t : wrapped)
                jsonArrayBuilder.add(elementSerializer.toJson(t));
            return jsonArrayBuilder.build();
        }

        @Nullable
        @Override
        public final C fromJson(JsonElement jsonElement) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            C list = supplyCollection();
            for (JsonElement element : jsonArray)
                list.add(elementSerializer.fromJson(element));
            return list;
        }

        protected abstract C supplyCollection();
    }

    abstract class MapSerializer<K, V, M extends Map<K, V>> implements JsonSerializer<M> {
        public static <K, V, M extends Map<K, V>> MapSerializer<K, V, M> create(@NotNull JsonSerializer<K> key, @NotNull JsonSerializer<V> value, @NotNull Supplier<M> mapSupplier) {
            return new MapSerializer<>(key, value) {

                @Override
                protected M supplyMap() {
                    return mapSupplier.get();
                }
            };
        }

        private final JsonSerializer<K> key;
        private final JsonSerializer<V> value;

        private MapSerializer(JsonSerializer<K> key, JsonSerializer<V> value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public JsonElement toJson(M wrapped) {
            boolean isStringKeys = wrapped.keySet().stream().anyMatch(k -> k instanceof String);
            if (isStringKeys) {
                JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();

                wrapped.forEach((k, v) -> {
                    JsonElement keyElement = key.toJson(k);
                    JsonElement valueElement = value.toJson(v);
                    jsonObjectBuilder.add(keyElement.getAsString(), valueElement);
                });
                return jsonObjectBuilder.build();
            } else {
                JsonArrayBuilder jsonArrayBuilder = JsonArrayBuilder.create();

                wrapped.forEach((k, v) -> {
                    JsonElement keyElement = key.toJson(k);
                    JsonElement valueElement = value.toJson(v);
                    jsonArrayBuilder.add(JsonObjectBuilder.create().add("key", keyElement).add("value", valueElement));
                });
                return jsonArrayBuilder.build();
            }
        }

        @Override
        public @Nullable M fromJson(JsonElement jsonElement) {
            M map = supplyMap();
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    JsonElement keyElement = element.getAsJsonObject().get("key").getAsJsonObject();
                    JsonElement valueElement = element.getAsJsonObject().get("value").getAsJsonObject();

                    K key = this.key.fromJson(keyElement);
                    V value = this.value.fromJson(valueElement);
                    map.put(key, value);
                }
            } else {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                for (String jsonKey : jsonObject.keySet()) {
                    K key = (K) jsonKey;
                    V value = this.value.fromJson(jsonObject.get(jsonKey));
                    map.put(key, value);
                }
            }
            return map;
        }

        protected abstract M supplyMap();

        @Override
        public String id() {
            return "mapping";
        }
    }

    JsonSerializer<NamespacedKey> NAMESPACED_KEY_SERIALIZER = JsonSerializerBuilder
        .create("key", NamespacedKey.class)
        .constructor(
            new SerializableField<>("namespace", PrimitiveSerializer.STRING, NamespacedKey::namespace),
            new SerializableField<>("key", PrimitiveSerializer.STRING, NamespacedKey::getKey),
            NamespacedKey::new
        )
        .build();
}

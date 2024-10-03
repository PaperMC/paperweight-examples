package de.verdox.mccreativelab.wrapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public interface WrapperSerializer<W extends MCCWrapped> extends JsonSerializer<W> {

    void writeToYaml(W wrapped, ConfigurationSection configurationSection);

    @Nullable
    W readFromYaml(ConfigurationSection configurationSection);


    class ItemTypeSerializer implements WrapperSerializer<MCCItemType> {
        public static final ItemTypeSerializer INSTANCE = new ItemTypeSerializer();

        @Override
        public JsonElement toJson(MCCItemType wrapped) {
            return JsonObjectBuilder.create()
                .add("type", getType(wrapped))
                .add("key", wrapped.getKey().asString())
                .build();
        }

        @Override
        public MCCItemType fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("type") || !jsonObject.has("key"))
                return null;
            NamespacedKey key = NamespacedKey.fromString(jsonElement.getAsJsonObject().get("key").getAsString());
            String type = jsonElement.getAsJsonObject().get("type").getAsString();
            return reconstruct(key, type);
        }

        @Override
        public String id() {
            return "item_type";
        }

        @Override
        public void writeToYaml(MCCItemType wrapped, ConfigurationSection configurationSection) {
            configurationSection.set("type", getType(wrapped));
            configurationSection.set("key", wrapped.getKey().toString());
        }

        @Override
        public MCCItemType readFromYaml(ConfigurationSection configurationSection) {
            String type = configurationSection.getString("type", "unknown");
            NamespacedKey key = NamespacedKey.fromString(configurationSection.getString("key", "minecraft:empty"));
            return reconstruct(key, type);
        }

        private String getType(MCCItemType wrapped) {
            if (wrapped instanceof MCCItemType.Vanilla) {
                return "vanilla";
            } else if (wrapped instanceof MCCItemType.FakeItemType) {
                return "mcc";
            } else
                return "unknown";
        }

        private MCCItemType reconstruct(NamespacedKey key, String type) {
            switch (type) {
                case "vanilla" -> {
                    return MCCItemType.of(RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).get(key).createItemStack());
                }
                case "fakeItemType" -> {
                    return MCCItemType.of(MCCreativeLabExtension.getFakeItemRegistry().get(key));
                }
                default -> {
                    return null;
                }
            }
        }
    }
}

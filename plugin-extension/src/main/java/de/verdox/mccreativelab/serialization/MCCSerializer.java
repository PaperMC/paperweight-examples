package de.verdox.mccreativelab.serialization;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.wrapper.block.MCCBlockType;
import de.verdox.mccreativelab.wrapper.block.MCCBlockData;
import de.verdox.mccreativelab.wrapper.entity.MCCEntityType;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MCCSerializer {
    private static final MCCSerializer INSTANCE = new MCCSerializer();

    public static MCCSerializer getInstance() {
        return INSTANCE;
    }

    public static void init() {
        MCCSerializer instance = getInstance();

        instance.registerSerializer("vanilla_block", MCCBlockType.Vanilla.class, MCCBlockType.Vanilla.SERIALIZER);
        instance.registerSerializer("mcc_block", MCCBlockType.FakeBlockType.class, MCCBlockType.FakeBlockType.SERIALIZER);

        instance.registerSerializer("vanilla_blockdata", MCCBlockData.Vanilla.class, MCCBlockData.Vanilla.INSTANCE);
        instance.registerSerializer("mcc_blockdata", MCCBlockData.FakeBlockState.class, MCCBlockData.FakeBlockState.INSTANCE);

        instance.registerSerializer("vanilla_entity_type", MCCEntityType.Vanilla.class, MCCEntityType.Vanilla.INSTANCE);

        if(MCCreativeLabExtension.isServerSoftware()){
            instance.registerSerializer("item_type", CustomItemData.class, new NBTSerializer<>() {
                @Override
                public void serialize(CustomItemData data, NBTContainer nbtContainer) {
                    nbtContainer.set("type", data.material().name());
                    nbtContainer.set("custom_model_data", data.customModelData());
                }

                @Override
                public CustomItemData deserialize(NBTContainer nbtContainer) {
                    if(!nbtContainer.has("type") || !nbtContainer.has("custom_model_data"))
                        return null;
                    return new CustomItemData(Material.valueOf(nbtContainer.getString("type")), nbtContainer.getInt("custom_model_data"));
                }
            });
        }
    }

    private final Map<Class<?>, NBTSerializer<?>> storedSerializers = new HashMap<>();
    private final Map<String, Class<?>> keyToClassMapping = new HashMap<>();
    private final Map<Class<?>, String> classToKeyMapping = new HashMap<>();

    public <T> void registerSerializer(String id, Class<? extends T> type, NBTSerializer<T> serializer) {
        if (keyToClassMapping.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already registered in MCCSerializer");
        if (classToKeyMapping.containsKey(type))
            throw new IllegalArgumentException("Type " + type.getName() + " already registered in MCCSerializer");
        storedSerializers.put(type, serializer);
        keyToClassMapping.put(id, type);
        classToKeyMapping.put(type, id);
    }

    public void serializeOrThrow(Object data, NBTContainer nbtContainer) {
        try {
            serialize(data, nbtContainer);
        } catch (SerializerNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void serialize(Object data, NBTContainer nbtContainer) throws SerializerNotFoundException {
        Objects.requireNonNull(nbtContainer, "NBTContainer cannot be null");
        Objects.requireNonNull(data, "Null data cannot be serialized");
        Class<?> type = data.getClass();
        if (!storedSerializers.containsKey(type))
            throw new SerializerNotFoundException("Serializer not found for type " + type.getName());

        NBTSerializer<Object> nbtSerializer = (NBTSerializer<Object>) storedSerializers.get(type);

        NBTContainer serialized = nbtContainer.createNBTContainer();
        try {
            nbtSerializer.serialize(data, serialized);
        } catch (Throwable e) {
            System.err.println("An error occurred while serializing data to an nbt container.");
            e.printStackTrace();
        }
        if (!serialized.getKeys().isEmpty()) {
            nbtContainer.set("id", classToKeyMapping.get(type));
            nbtContainer.set("serialized", serialized);
        }
    }

    @Nullable
    public <T> T deserializeOrThrow(Class<? extends T> type, NBTContainer nbtContainer) {
        try {
            return deserialize(type, nbtContainer);
        } catch (SerializerNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public <T> T deserialize(Class<? extends T> type, NBTContainer nbtContainer) throws SerializerNotFoundException {
        if (!nbtContainer.has("id"))
            return null;
        String serializerID = nbtContainer.getString("id");
        if (!keyToClassMapping.containsKey(serializerID))
            throw new SerializerNotFoundException("Serializer not found for type " + type.getName());
        NBTSerializer<?> nbtSerializer = storedSerializers.getOrDefault(keyToClassMapping.get(serializerID), null);
        try {
            Object deserialized = nbtSerializer.deserialize(nbtContainer);
            return type.cast(deserialized);
        } catch (Throwable e) {
            System.err.println("An error occurred while deserializing an nbt container.");
            e.printStackTrace();
            return null;
        }
    }
}

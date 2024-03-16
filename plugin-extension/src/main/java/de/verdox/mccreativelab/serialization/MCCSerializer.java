package de.verdox.mccreativelab.serialization;

import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.wrapper.MCCWrapped;
import de.verdox.mccreativelab.wrapper.block.MCCBlock;
import de.verdox.mccreativelab.wrapper.block.MCCBlockData;
import de.verdox.mccreativelab.wrapper.entity.MCCEntity;
import de.verdox.mccreativelab.wrapper.entity.MCCEntityType;
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

        instance.registerSerializer("vanilla_block", MCCBlock.Vanilla.class, MCCBlock.Vanilla.SERIALIZER);
        instance.registerSerializer("mcc_block", MCCBlock.FakeBlock.class, MCCBlock.FakeBlock.SERIALIZER);

        instance.registerSerializer("vanilla_blockdata", MCCBlockData.Vanilla.class, MCCBlockData.Vanilla.INSTANCE);
        instance.registerSerializer("mcc_blockdata", MCCBlockData.FakeBlockState.class, MCCBlockData.FakeBlockState.INSTANCE);

        instance.registerSerializer("vanilla_entity_type", MCCEntityType.Vanilla.class, MCCEntityType.Vanilla.INSTANCE);
    }

    private final Map<Class<?>, NBTSerializer<?>> storedSerializers = new HashMap<>();
    private final Map<String, Class<?>> keyToClassMapping = new HashMap<>();
    private final Map<Class<?>, String> classToKeyMapping = new HashMap<>();

    public <T extends MCCWrapped> void registerSerializer(String id, Class<? extends T> type, NBTSerializer<T> serializer) {
        if (keyToClassMapping.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already registered in MCCSerializer");
        if (classToKeyMapping.containsKey(type))
            throw new IllegalArgumentException("Type " + type.getName() + " already registered in MCCSerializer");
        storedSerializers.put(type, serializer);
        keyToClassMapping.put(id, type);
        classToKeyMapping.put(type, id);
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
    public <T> T deserialize(Class<? extends T> type, NBTContainer nbtContainer) {
        if (!nbtContainer.has("id"))
            return null;
        String serializerID = nbtContainer.getString("id");
        if (!keyToClassMapping.containsKey(serializerID))
            return null;
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

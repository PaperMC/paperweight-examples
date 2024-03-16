package de.verdox.mccreativelab.util.nbt;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.world.block.FakeBlock;
import net.md_5.bungee.api.chat.ItemTag;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NBTContainer {
    private final String namespace;
    private final PersistentDataContainer persistentDataContainer;

    protected NBTContainer(String namespace, PersistentDataContainer persistentDataContainer) {
        this.namespace = namespace;
        this.persistentDataContainer = persistentDataContainer;
    }

    public static NBTContainer of(String namespace, PersistentDataContainer persistentDataContainer) {
        return new NBTContainer(namespace, persistentDataContainer);
    }

    public NBTContainer createNBTContainer() {
        return new NBTContainer(namespace, persistentDataContainer.getAdapterContext().newPersistentDataContainer());
    }

    public boolean has(String key) {
        return persistentDataContainer.has(createNameSpacedKey(key));
    }

    public void set(String key, int value) {
        persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.INTEGER, value);
    }

    public int getInt(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return 0;
        return persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER);
    }

    public void set(String key, boolean value) {
        persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.BOOLEAN, value);
    }

    public boolean getBoolean(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return false;
        return persistentDataContainer.get(namespacedKey, PersistentDataType.BOOLEAN);
    }

    public void set(String key, long value) {
        persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.LONG, value);
    }

    public long getLong(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return 0;
        return persistentDataContainer.get(namespacedKey, PersistentDataType.LONG);
    }

    public void set(String key, String value) {
        if (value == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.STRING, value);
    }

    public String getString(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return "";
        return persistentDataContainer.get(namespacedKey, PersistentDataType.STRING);
    }

    public void set(String key, float value) {
        persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.FLOAT, value);
    }

    public float getFloat(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return 0;
        return persistentDataContainer.get(namespacedKey, PersistentDataType.FLOAT);
    }

    public void set(String key, byte value) {
        persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.BYTE, value);
    }

    public byte getByte(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return 0;
        return persistentDataContainer.get(namespacedKey, PersistentDataType.BYTE);
    }

    public void set(String key, double value) {
        persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.DOUBLE, value);
    }


    public double getDouble(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return 0;
        return persistentDataContainer.get(namespacedKey, PersistentDataType.DOUBLE);
    }

    public void set(String key, byte[] value) {
        if (value == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.BYTE_ARRAY, value);
    }

    public byte[] getByteArray(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return new byte[0];
        return persistentDataContainer.get(namespacedKey, PersistentDataType.BYTE_ARRAY);
    }

    public void set(String key, int[] value) {
        if (value == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.INTEGER_ARRAY, value);
    }

    public int[] getIntArray(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return new int[0];
        return persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER_ARRAY);
    }

    public void set(String key, long[] value) {
        if (value == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.LONG_ARRAY, value);
    }

    public void setDoubleList(String key, List<Double> list) {
        if (list == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.LIST.doubles(), list);
    }

    public List<Double> getDoubleList(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return new LinkedList<>();
        return persistentDataContainer.get(namespacedKey, PersistentDataType.LIST.doubles());
    }

    public long[] getLongArray(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return new long[0];
        return persistentDataContainer.get(namespacedKey, PersistentDataType.LONG_ARRAY);
    }

    public void set(String key, Collection<NBTContainer> value) {
        if (value == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.TAG_CONTAINER_ARRAY, value.stream()
                                                                                                               .map(nbtContainer -> nbtContainer.persistentDataContainer)
                                                                                                               .toArray(PersistentDataContainer[]::new));
    }

    public List<NBTContainer> getNBTContainerList(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return new LinkedList<>();
        return Arrays.stream(persistentDataContainer.get(namespacedKey, PersistentDataType.TAG_CONTAINER_ARRAY))
                     .map(persistentDataContainer1 -> new NBTContainer(namespace, persistentDataContainer1)).toList();
    }

    public void set(String key, NBTContainer value) {
        if (value == null)
            remove(key);
        else
            persistentDataContainer.set(createNameSpacedKey(key), PersistentDataType.TAG_CONTAINER, value.persistentDataContainer);
    }

    @NotNull
    private NamespacedKey createNameSpacedKey(String key) {
        return new NamespacedKey(namespace.toLowerCase(Locale.ROOT), key.toLowerCase(Locale.ROOT));
    }

    /**
     * If you intend to edit the NBTContainer you must save it to the parent container after it.
     *
     * @param key The key
     * @return - The NBTContainer saved at the key
     */
    @Nullable
    public NBTContainer getNBTContainer(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return null;
        return new NBTContainer(namespace, persistentDataContainer.get(namespacedKey, PersistentDataType.TAG_CONTAINER));
    }

    public void editNBTContainer(String key, Consumer<NBTContainer> editor) {
        NBTContainer container = getNBTContainer(key);
        if (container == null || editor == null)
            return;
        editor.accept(container);
        set(key, container);
    }

    public void set(String key, UUID uuid) {
        if (uuid == null) {
            remove(key);
            return;
        }
        set(key, uuid.toString());
    }

    @Nullable
    public UUID getUUID(String key) {
        String uuidString = getString(key);
        if (uuidString.isEmpty())
            return null;
        return UUID.fromString(uuidString);
    }

    public void addNBTContainer(String key) {
        addNBTContainer(key, null);
    }

    public void addNBTContainer(String key, Consumer<NBTContainer> consumer) {
        NBTContainer nbtContainer = createNBTContainer();
        if (consumer != null)
            consumer.accept(nbtContainer);
        set(key, nbtContainer);
    }

    public Set<String> getKeys() {
        return persistentDataContainer.getKeys().stream().map(NamespacedKey::getKey).collect(Collectors.toSet());
    }

    public void remove(String key) {
        persistentDataContainer.remove(createNameSpacedKey(key));
    }

    public void set(String key, boolean[] array) {
        if (array == null) {
            remove(key);
            return;
        }
        byte[] bArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i])
                bArray[i] = 1;
            else
                bArray[i] = 0;
        }
        set(key, bArray);
    }

    public boolean[] getBooleanArray(String key) {
        byte[] bArray = getByteArray(key);
        var array = new boolean[bArray.length];

        for (int i = 0; i < bArray.length; i++)
            array[i] = bArray[i] == 1;
        return array;
    }

    public void setStringList(String key, Collection<String> stringList) {
        if (stringList == null) {
            remove(key);
            return;
        }
        set(key, stringList.stream().map(s -> {
            NBTContainer nbtContainer = createNBTContainer();
            nbtContainer.set("value", s);
            return nbtContainer;
        }).toList());
    }

    public List<String> getStringList(String key) {
        return getNBTContainerList(key).stream().map(nbtContainer -> nbtContainer.getString("value")).toList();
    }

    public void setUUIDList(String key, Collection<UUID> uuidList) {
        if (uuidList == null) {
            remove(key);
            return;
        }
        setStringList(key, uuidList.stream().map(UUID::toString).toList());
    }

    public List<UUID> getUUIDList(String key) {
        return getStringList(key).stream().map(UUID::fromString).toList();
    }

    public void set(String key, FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState == null) {
            remove(key);
            return;
        }
        NBTContainer nbtContainer = createNBTContainer();
        nbtContainer.set("id", fakeBlockState.getFakeBlock().getKey().asString());
        nbtContainer.set("state", fakeBlockState.getFakeBlock().getBlockStateID(fakeBlockState));
        set(key, nbtContainer);
    }

    public FakeBlock.FakeBlockState getFakeBlockState(String key) {
        NBTContainer nbtContainer = getNBTContainer(key);
        if (nbtContainer == null)
            return null;

        NamespacedKey blockKey = NamespacedKey.fromString(nbtContainer.getString("id"));
        int stateID = nbtContainer.getInt("state");
        FakeBlock fakeBlock = MCCreativeLabExtension.getFakeBlockRegistry().get(blockKey);
        if (fakeBlock == null)
            return null;
        FakeBlock.FakeBlockState fakeBlockState = fakeBlock.getBlockState(stateID);
        if (fakeBlockState == null)
            return fakeBlock.getDefaultBlockState();
        return fakeBlockState;
    }

    public void set(String key, BlockData blockData) {
        if (blockData == null) {
            remove(key);
            return;
        }
        set(key, blockData.getAsString());
    }

    public void set(String key, Enum<?> enumValue) {
        if (enumValue == null) {
            remove(key);
            return;
        }
        set(key, enumValue.name());
    }

    public void set(String key, ItemStack stack) {
        if (stack == null) {
            remove(key);
            return;
        }
        set(key, stack.serializeAsBytes());
    }

    public ItemStack getItemStack(String key) {
        byte[] serialized = getByteArray(key);
        return ItemStack.deserializeBytes(serialized);
    }

    public void set(String key, ItemStack[] itemStacks) {
        if (itemStacks == null) {
            remove(key);
            return;
        }
        setItemStackList(key, Arrays.stream(itemStacks).toList());
    }

    public ItemStack[] getItemArray(String key) {
        return getItemStacks(key).toArray(ItemStack[]::new);
    }

    public void setItemStackList(String key, Collection<ItemStack> itemStacks) {
        if (itemStacks == null) {
            remove(key);
            return;
        }
        List<NBTContainer> serializedItems = new LinkedList<>();
        for (ItemStack itemStack : itemStacks) {
            NBTContainer nbt = createNBTContainer();
            if (itemStack == null)
                nbt.set("item", new byte[0]);
            else
                nbt.set("item", itemStack.serializeAsBytes());
            serializedItems.add(nbt);
        }
        set(key, serializedItems);
    }

    public List<ItemStack> getItemStacks(String key) {
        return getNBTContainerList(key)
            .stream()
            .map(nbtContainer -> nbtContainer.getByteArray("item"))
            .map(bytes -> bytes.length == 0 ? new ItemStack(Material.AIR) : ItemStack.deserializeBytes(bytes))
            .toList();
    }

    public <T extends Enum<T>> T getEnum(String key, Class<? extends T> type) {
        String enumID = getString(key);
        return Arrays.stream(type.getEnumConstants()).filter(t -> t.name().equals(enumID)).findAny().orElse(null);
    }

    @Nullable
    public BlockData getBlockData(String key) {
        return Bukkit.createBlockData(getString(key));
    }

    public void set(String key, Location location) {
        if (location == null) {
            remove(key);
            return;
        }
        NBTContainer nbtContainer = createNBTContainer();
        nbtContainer.set("level", location.getWorld().getName());
        nbtContainer.set("pos", location.toVector());
        nbtContainer.set("yaw", location.getYaw());
        nbtContainer.set("pitch", location.getPitch());
        set(key, nbtContainer);
    }

    @Nullable
    public Location getLocation(String key) {
        NBTContainer nbtContainer = getNBTContainer(key);
        if (nbtContainer == null)
            return null;
        String worldName = nbtContainer.getString("level");
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;
        Vector pos = nbtContainer.getVector("pos");
        if (pos == null)
            return null;
        float yaw = nbtContainer.getFloat("yaw");
        float pitch = nbtContainer.getFloat("pitch");
        return pos.toLocation(world, yaw, pitch);
    }

    public void set(String key, Vector vector) {
        if (vector == null) {
            remove(key);
            return;
        }
        setDoubleList(key, List.of(vector.getX(), vector.getY(), vector.getZ()));
    }

    @Nullable
    public Vector getVector(String key) {
        List<Double> components = getDoubleList(key);
        if (components.size() != 3)
            return null;
        return new Vector(components.get(0), components.get(1), components.get(2));
    }

    public void setVectorList(String key, Collection<Vector> list) {
        if (list == null) {
            remove(key);
            return;
        }
        set(key, list.stream().map(vector -> {
            NBTContainer nbtContainer = createNBTContainer();
            nbtContainer.set("v", vector);
            return nbtContainer;
        }).toList());
    }

    public List<Vector> getVectorList(String key) {
        return getNBTContainerList(key).stream().map(nbtContainer -> nbtContainer.getVector("v"))
                                       .filter(Objects::nonNull).toList();
    }

    @Override
    public String toString() {
        return "NBTContainer{" +
            "namespace='" + namespace + '\'' +
            ", persistentDataContainer=" + persistentDataContainer +
            '}';
    }
}

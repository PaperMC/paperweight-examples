package de.verdox.mccreativelab.util.nbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    public long[] getLongArray(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return new long[0];
        return persistentDataContainer.get(namespacedKey, PersistentDataType.LONG_ARRAY);
    }

    public void set(String key, List<NBTContainer> value) {
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

    @Nullable
    public NBTContainer getNBTContainer(String key) {
        NamespacedKey namespacedKey = createNameSpacedKey(key);
        if (!persistentDataContainer.has(namespacedKey))
            return null;
        return new NBTContainer(namespace, persistentDataContainer.get(namespacedKey, PersistentDataType.TAG_CONTAINER));
    }

    public NBTContainer getOrCreateNBTContainer(String key) {
        if (!has(key))
            addNBTContainer(key);
        return getNBTContainer(key);
    }

    public void set(String key, UUID uuid) {
        if(uuid == null){
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

    public NBTContainer addNBTContainer(String key) {
        NBTContainer nbtContainer = createNBTContainer();
        set(key, nbtContainer);
        return nbtContainer;
    }

    public Set<String> getKeys() {
        return persistentDataContainer.getKeys().stream().map(NamespacedKey::getKey).collect(Collectors.toSet());
    }

    public void remove(String key) {
        persistentDataContainer.remove(createNameSpacedKey(key));
    }

    public void set(String key, boolean[] array) {
        if(array == null){
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

    public void setStringList(String key, List<String> stringList) {
        if(stringList == null){
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

    public void setUUIDList(String key, List<UUID> uuidList) {
        if(uuidList == null){
            remove(key);
            return;
        }
        setStringList(key, uuidList.stream().map(UUID::toString).toList());
    }

    public List<UUID> getUUIDList(String key) {
        return getStringList(key).stream().map(UUID::fromString).toList();
    }

    public void set(String key, Location location) {
        if(location == null){
            remove(key);
            return;
        }
        NBTContainer nbtContainer = createNBTContainer();
        nbtContainer.set("level", location.getWorld().getName());
        nbtContainer.set("x", location.x());
        nbtContainer.set("y", location.y());
        nbtContainer.set("z", location.z());
        nbtContainer.set("yaw", location.getYaw());
        nbtContainer.set("pitch", location.getPitch());
        set(key, nbtContainer);
    }

    @Nullable
    public Location getLocation(String key) {
        NBTContainer nbtContainer = getNBTContainer(key);
        String worldName = nbtContainer.getString("level");
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;
        double x = nbtContainer.getDouble("x");
        double y = nbtContainer.getDouble("y");
        double z = nbtContainer.getDouble("z");
        float yaw = nbtContainer.getFloat("yaw");
        float pitch = nbtContainer.getFloat("pitch");
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}

package de.verdox.mccreativelab.util.storage.palette;

import de.verdox.mccreativelab.registry.exception.PaletteValueUnknownException;
import de.verdox.mccreativelab.util.storage.HashedThreeDimensionalStorage;
import de.verdox.mccreativelab.util.storage.NBTThreeDimensionalStorageSerializer;
import de.verdox.mccreativelab.util.storage.ThreeDimensionalStorage;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.*;

public class NBTPalettedContainer<T> {
    private static final NamespacedKey PALETTE_DATA_ARRAY_KEY = new NamespacedKey("mccreativelab", "data");
    private static final NamespacedKey PALETTE_UNIQUE_DATA_LIST_KEY = new NamespacedKey("mccreativelab", "unique_data");
    private static final NamespacedKey SERIALIZATION_ID = new NamespacedKey("mccreativelab", "serialization_id");
    private final IdMap<T> idMap;
    private final ThreeDimensionalStorage<Short, T> storage;

    public NBTPalettedContainer(IdMap<T> idMap, int xSize, int ySize, int zSize) {
        this.storage = new HashedThreeDimensionalStorage<>(idMap, new ThreeDimensionalStorage.IndexingStrategy.Short(xSize, ySize, zSize));
        this.idMap = idMap;
        if (xSize <= 0 || ySize <= 0 || zSize <= 0)
            throw new IllegalArgumentException("Values must be greater than 0");
    }

    public final void setData(@Nullable T data, int x, int y, int z) {
        this.storage.setData(data, x, y, z);
    }

    public final void removeData(int x, int y, int z) {
        setData(null, x, y, z);
    }

    public final @Nullable T getData(int x, int y, int z) {
        return storage.getData(x,y,z);
    }

    public final void serialize(NamespacedKey namespacedKey, PersistentDataContainer persistentDataContainer) {
        PersistentDataContainer serializedStorage = NBTThreeDimensionalStorageSerializer.serialize(storage, persistentDataContainer.getAdapterContext());
        persistentDataContainer.set(namespacedKey, PersistentDataType.TAG_CONTAINER, serializedStorage);
    }

    public final void deSerialize(NamespacedKey namespacedKey, PersistentDataContainer persistentDataContainer) {
        if(!persistentDataContainer.has(namespacedKey, PersistentDataType.TAG_CONTAINER))
            return;
        PersistentDataContainer serializedStorage = persistentDataContainer.getOrDefault(namespacedKey, PersistentDataType.TAG_CONTAINER, persistentDataContainer.getAdapterContext().newPersistentDataContainer());
        NBTThreeDimensionalStorageSerializer.deSerialize(storage, serializedStorage);
    }
}

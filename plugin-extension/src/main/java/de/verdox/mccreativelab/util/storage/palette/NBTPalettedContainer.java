package de.verdox.mccreativelab.util.storage.palette;

import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.NBTPersistent;
import de.verdox.mccreativelab.util.storage.HashedThreeDimensionalStorage;
import de.verdox.mccreativelab.util.storage.NBTThreeDimensionalStorageSerializer;
import de.verdox.mccreativelab.util.storage.ThreeDimensionalStorage;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Objects;

public class NBTPalettedContainer<T> implements NBTPersistent {
    private final ThreeDimensionalStorage<Short, T> storage;

    public NBTPalettedContainer(IdMap<T> idMap, int xSize, int ySize, int zSize) {
        this.storage = new HashedThreeDimensionalStorage<>(idMap, new ThreeDimensionalStorage.IndexingStrategy.Short(xSize, ySize, zSize));
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

    public boolean isEmpty(){
        return storage.isEmpty();
    }

    public final void deSerialize(NamespacedKey namespacedKey, PersistentDataContainer persistentDataContainer) {
        if(!persistentDataContainer.has(namespacedKey, PersistentDataType.TAG_CONTAINER))
            return;
        PersistentDataContainer serializedStorage = persistentDataContainer.getOrDefault(namespacedKey, PersistentDataType.TAG_CONTAINER, persistentDataContainer.getAdapterContext().newPersistentDataContainer());
        NBTThreeDimensionalStorageSerializer.deSerialize(storage, serializedStorage);
    }

    public ThreeDimensionalStorage<Short, T> getStorage() {
        return storage;
    }

    @Override
    public void saveNBTData(NBTContainer storage) {
        NBTContainer threeDimensionalStorageNBT = storage.createNBTContainer();
        this.storage.saveNBTData(threeDimensionalStorageNBT);
        storage.set("serializedThreeDimensionalStorage", threeDimensionalStorageNBT);
    }

    @Override
    public void loadNBTData(NBTContainer storage) {
        if(!storage.has("serializedThreeDimensionalStorage"))
            return;
        this.storage.loadNBTData(Objects.requireNonNull(storage.getNBTContainer("serializedThreeDimensionalStorage")));
    }
}

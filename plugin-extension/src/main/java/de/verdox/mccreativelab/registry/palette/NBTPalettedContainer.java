package de.verdox.mccreativelab.registry.palette;

import de.verdox.mccreativelab.registry.exception.PaletteValueUnknownException;
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
    private final Map<T, Set<Integer>> dataToIndicesMapping = new HashMap<>();
    private final int xSize;
    private final int ySize;
    private final int zSize;
    private int[] data;

    public NBTPalettedContainer(IdMap<T> idMap, int xSize, int ySize, int zSize) {
        this.idMap = idMap;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        if (xSize <= 0 || ySize <= 0 || zSize <= 0)
            throw new IllegalArgumentException("Values must be greater than 0");
        data = new int[xSize * ySize * zSize];
    }

    public final void setData(T data, int x, int y, int z) {
        int index = getIndex(x, y, z);
        checkInputs(x, y, z);
        try {
            T currentData = getData(x, y, z);

            // If state was not really changed to nothing
            if (data.equals(currentData))
                return;
            // Remove old state to index mapping
            if (dataToIndicesMapping.containsKey(currentData))
                dataToIndicesMapping.get(currentData).remove(index);
            // Cleanup mapping if it is empty
            cleanup(data);
        } catch (PaletteValueUnknownException ignored) {
            // Block state was never saved before, so we are ignoring the upper cleanup code
        }
        int blockStateID = idMap.getId(data);
        this.data[index] = blockStateID;
        dataToIndicesMapping.computeIfAbsent(data, fakeBlockState -> new HashSet<>()).add(index);
    }

    public final @Nullable T getData(int x, int y, int z) throws PaletteValueUnknownException {
        int index = getIndex(x, y, z);
        checkInputs(x, y, z);
        int foundID = data[index];
        if (foundID == 0)
            return null;
        T currentData = idMap.byId(foundID);
        if (currentData == null)
            throw new PaletteValueUnknownException("Unknown FakeBlockState found at " + x + " " + y + " " + z);
        return currentData;
    }

    public final @Nullable T getDataUnsafe(int x, int y, int z) {
        try {
            return getData(x, y, z);
        } catch (PaletteValueUnknownException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanup(T data) {
        if (dataToIndicesMapping.containsKey(data) && dataToIndicesMapping.get(data).isEmpty())
            dataToIndicesMapping.remove(data);
    }

    private int getIndex(int x, int y, int z) {
        return x + xSize * (y + ySize * z);
    }

    private void checkInputs(int x, int y, int z) {
        if (x < 0 || x >= xSize)
            throw new IndexOutOfBoundsException("x value " + x + " not in bounds between 0 and " + xSize);
        if (y < 0 || y >= ySize)
            throw new IndexOutOfBoundsException("y value " + y + " not in bounds between 0 and " + ySize);
        if (z < 0 || z >= zSize)
            throw new IndexOutOfBoundsException("z value " + z + " not in bounds between 0 and " + zSize);
    }

    public final void serialize(NamespacedKey namespacedKey, PersistentDataContainer persistentDataContainer) {
        if (dataToIndicesMapping.isEmpty())
            return;
        PersistentDataContainer container = persistentDataContainer.getAdapterContext().newPersistentDataContainer();

        List<PersistentDataContainer> containers = new LinkedList<>();
        dataToIndicesMapping.forEach((data, integers) -> {
            if (integers.isEmpty() || data == null)
                return;

            PersistentDataContainer dataToNbt = dataToNbt(container.getAdapterContext(), data);
            if (dataToNbt == null)
                return;
            dataToNbt.set(SERIALIZATION_ID, PersistentDataType.INTEGER, idMap.getId(data));
            containers.add(dataToNbt);
        });

        container.set(PALETTE_DATA_ARRAY_KEY, PersistentDataType.INTEGER_ARRAY, data);
        container.set(PALETTE_UNIQUE_DATA_LIST_KEY, PersistentDataType.TAG_CONTAINER_ARRAY, containers.toArray(PersistentDataContainer[]::new));

        persistentDataContainer.set(namespacedKey, PersistentDataType.TAG_CONTAINER, container);
    }

    public final void deSerialize(NamespacedKey namespacedKey, PersistentDataContainer persistentDataContainer) {
        if (!persistentDataContainer.has(namespacedKey))
            return;
        PersistentDataContainer serializedPalette = persistentDataContainer.get(namespacedKey, PersistentDataType.TAG_CONTAINER);
        if (serializedPalette == null)
            return;
        int[] data = serializedPalette.get(PALETTE_DATA_ARRAY_KEY, PersistentDataType.INTEGER_ARRAY);
        PersistentDataContainer[] serializedUniqueData = serializedPalette.get(PALETTE_UNIQUE_DATA_LIST_KEY, PersistentDataType.TAG_CONTAINER_ARRAY);
        if (serializedUniqueData == null)
            return;
        Map<Integer, Integer> idConversionMap = new HashMap<>();
        for (PersistentDataContainer serializedUniqueDatum : serializedUniqueData) {
            int formerSerializationID = serializedUniqueDatum.get(SERIALIZATION_ID, PersistentDataType.INTEGER).intValue();
            T deserializedData = nbtToData(serializedUniqueDatum);
            if (deserializedData == null) {
                idConversionMap.put(formerSerializationID, -1);
                continue;
            }

            int actualSerializationID = idMap.getId(deserializedData);

            if (formerSerializationID != actualSerializationID)
                idConversionMap.put(formerSerializationID, actualSerializationID);
        }
        if (data == null)
            return;
        for (int i = 0; i < data.length; i++) {
            int oldSerializationID = data[i];

            if (oldSerializationID == -1) {
                data[i] = 0;
                continue;
            }

            if (idConversionMap.containsKey(oldSerializationID)) {
                int newSerializationID = idConversionMap.get(oldSerializationID);
                data[i] = newSerializationID;
            }
            dataToIndicesMapping.computeIfAbsent(idMap.byId(data[i]), t -> new HashSet<>()).add(i);
            this.data = data;
        }
    }

    public PersistentDataContainer dataToNbt(PersistentDataAdapterContext adapterContext, T data) {
        return adapterContext.newPersistentDataContainer();
    }

    /**
     * @param persistentDataContainer the persistent data container used to deserialize data from
     * @return the data if it could be deserialized. null if the data could not be deserialized and should be discarded
     */
    public @Nullable T nbtToData(PersistentDataContainer persistentDataContainer) {
        return null;
    }
}

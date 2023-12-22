package de.verdox.mccreativelab.util.storage;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.List;

public class NBTThreeDimensionalStorageSerializer {
    private static final NamespacedKey STORAGE_KEY = new NamespacedKey("mccreativelab", "storage");
    private static final NamespacedKey ID_KEY = new NamespacedKey("mccreativelab", "id");
    private static final NamespacedKey INDIZES_KEY = new NamespacedKey("mccreativelab", "indizes");

    public static <K extends Number, T> PersistentDataContainer serialize(ThreeDimensionalStorage<K, T> storage, PersistentDataAdapterContext adapterContext) {
        PersistentDataContainer persistentDataContainer = adapterContext.newPersistentDataContainer();
        List<PersistentDataContainer> dataList = new LinkedList<>();
        storage.getDataToIndizesMappingInternal().forEach((dataID, indizes) -> {
            PersistentDataContainer dataContainer = adapterContext.newPersistentDataContainer();
            dataContainer.set(ID_KEY, PersistentDataType.INTEGER, dataID);
            List<Integer> data = indizes.stream().map(Number::intValue).toList();
            int[] dataArray = new int[data.size()];
            for (int i = 0; i < data.size(); i++)
                dataArray[i] = data.get(i);
            dataContainer.set(INDIZES_KEY, PersistentDataType.INTEGER_ARRAY, dataArray);
            dataList.add(dataContainer);
        });
        persistentDataContainer.set(STORAGE_KEY, PersistentDataType.TAG_CONTAINER_ARRAY, dataList.toArray(PersistentDataContainer[]::new));
        return persistentDataContainer;
    }

    public static <K extends Number, T> void deSerialize(ThreeDimensionalStorage<K, T> storage, PersistentDataContainer persistentDataContainer) {
        if (!persistentDataContainer.has(STORAGE_KEY, PersistentDataType.TAG_CONTAINER_ARRAY))
            return;

        PersistentDataContainer[] containerArray = persistentDataContainer.get(STORAGE_KEY, PersistentDataType.TAG_CONTAINER_ARRAY);
        for (PersistentDataContainer dataContainer : containerArray) {
            if(!dataContainer.has(ID_KEY, PersistentDataType.INTEGER) || !dataContainer.has(INDIZES_KEY, PersistentDataType.INTEGER_ARRAY))
                continue;

            int dataID = dataContainer.get(ID_KEY, PersistentDataType.INTEGER);
            int[] indizes = dataContainer.get(INDIZES_KEY, PersistentDataType.INTEGER_ARRAY);

            for (int index : indizes) {
                int[] parameter = storage.getIndexingStrategy().extractParameters(index);
                int x = parameter[0];
                int y = parameter[1];
                int z = parameter[2];

                T data = storage.getIdMap().byId(dataID);
                storage.setData(data, x, y ,z);
            }
        }
    }
}

package de.verdox.mccreativelab.world.item.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ItemDataMapper {
    private static final Map<Class<? extends ItemData<?>>, Supplier<ItemData<?>>> suppliers = new HashMap<>();
    private static final Map<String, Supplier<ItemData<?>>> keyToSupplierMapping = new HashMap<>();
    private static final Set<Class<? extends ItemData<?>>> defaultTypesMapping = new HashSet<>();

    public static void registerItemData(Class<? extends ItemData<?>> type, Supplier<ItemData<?>> supplier, boolean defaultApply) {
        suppliers.put(type, supplier);
        ItemData<?> itemData = supplier.get();
        String nbtKey = itemData.getNBTKey();
        if (keyToSupplierMapping.containsKey(nbtKey))
            throw new IllegalStateException(nbtKey + " already used in ItemDataMapper");
        keyToSupplierMapping.put(nbtKey, supplier);
        if(defaultApply)
            defaultTypesMapping.add(type);
    }
    public static void registerItemData(Class<? extends ItemData<?>> type, Supplier<ItemData<?>> supplier) {
        registerItemData(type, supplier, false);
    }

    static Set<Class<? extends ItemData<?>>> getDefaultData(){
        return defaultTypesMapping;
    }

    static <T extends ItemData<T>> T createItemDataByType(Class<? extends T> type) {
        if (!suppliers.containsKey(type))
            throw new IllegalStateException("ItemData class not registered to ItemDataMapper " + type.getName());
        return type.cast(suppliers.get(type).get());
    }

    static ItemData<?> createItemDataByKey(String nbtKey) {
        if (!keyToSupplierMapping.containsKey(nbtKey))
            throw new IllegalStateException("No item data found for key " + nbtKey);
        return keyToSupplierMapping.get(nbtKey).get();
    }
}

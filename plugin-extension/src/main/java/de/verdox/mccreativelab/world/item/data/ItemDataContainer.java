package de.verdox.mccreativelab.world.item.data;

import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.NBTSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

public class ItemDataContainer implements NBTSerializable {
    private final Map<Class<? extends ItemData<?>>, ItemData<?>> itemData = new HashMap<>();
    private final List<ItemData<?>> orderedItemData = new LinkedList<>();
    private final ItemStack stack;
    public static ItemDataContainer from(ItemStack stack) {
        return new ItemDataContainer(stack);
    }

    ItemDataContainer(ItemStack stack) {
        this.stack = stack;
        loadNBTData(NBTContainer.of("fixedminecraft", stack.getItemMeta().getPersistentDataContainer()));
        for (Class<? extends ItemData> defaultDatum : ItemDataMapper.getDefaultData())
            addDataIfNotExist(defaultDatum);
    }

    public void save() {
        this.stack.editMeta(itemMeta -> {
                itemMeta.lore(new LinkedList<>());
                for (ItemData<?> value : orderedItemData) {
                    value.applyItemMetaFormat(stack, itemMeta);
                }
                saveNBTData(NBTContainer.of("fixedminecraft", itemMeta.getPersistentDataContainer()));
            }
        );
    }

    public <T extends ItemData<T>> void edit(Class<? extends T> type, Consumer<T> consumer) {
        T data;
        if (itemData.containsKey(type))
            data = type.cast(itemData.get(type));
        else {
            data = type.cast(ItemDataMapper.createItemDataByType(type));
            itemData.put(type, data);
            orderedItemData.add(data);
        }
        consumer.accept(data);
        if (!data.hasData(stack)) {
            itemData.remove(type);
            orderedItemData.remove(itemData.remove(type));
        }

        save();
    }

    public <T extends ItemData<T>> void addDataIfNotExist(Class<? extends T> type) {
        if (itemData.containsKey(type))
            return;
        T data = type.cast(ItemDataMapper.createItemDataByType(type));
        itemData.put(type, data);
        orderedItemData.add(data);
        save();
    }

    @Override
    public void saveNBTData(NBTContainer storage) {
        NBTContainer itemDataNBT = storage.createNBTContainer();

        for (ItemData<?> itemData : orderedItemData) {
            NBTContainer nbtContainer = itemDataNBT.createNBTContainer();
            itemData.saveNBTData(nbtContainer);
            itemDataNBT.set(itemData.getNBTKey(), nbtContainer);
        }

        storage.set("item_data", itemDataNBT);
    }

    @Override
    public void loadNBTData(NBTContainer storage) {
        if (!storage.has("item_data"))
            return;
        this.itemData.clear();
        this.orderedItemData.clear();
        NBTContainer itemDataNBT = storage.getNBTContainer("item_data");
        for (String key : itemDataNBT.getKeys()) {
            ItemData<?> itemData = ItemDataMapper.createItemDataByKey(key);
            itemData.loadNBTData(itemDataNBT.getNBTContainer(key));
            this.itemData.put((Class<? extends ItemData<?>>) itemData.getClass(), itemData);
            orderedItemData.add(itemData);
        }
    }
}

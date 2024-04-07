package de.verdox.mccreativelab.util.player.inventory;

import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.world.item.FakeItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface PlayerInventoryCacheStrategy {
    /**
     * Called when a new item is cached
     * @param slot - The slot of the new item
     * @param stack - The item
     */
    void cacheItemInSlot(int slot, ItemStack stack);

    /**
     * Called when an old item is removed from cache
     * @param slot - The slot of the new item
     * @param stack - The item
     */
    void removeSlotFromCache(int slot, ItemStack stack);

    class CachedAmounts implements PlayerInventoryCacheStrategy {
        private final Map<CustomItemData, Integer> cachedAmounts = new HashMap<>();

        public int getAmount(CustomItemData customItemData){
            return cachedAmounts.getOrDefault(customItemData, 0);
        }

        public int getAmount(Material material){
            return getAmount(new CustomItemData(material, 0));
        }

        public int getAmount(ItemStack stack){
            return getAmount(CustomItemData.fromItemStack(stack));
        }

        public int getAmount(FakeItem fakeItem){
            return getAmount(new CustomItemData(fakeItem.getMaterial(), fakeItem.getCustomModelData()));
        }

        @Override
        public void cacheItemInSlot(int slot, ItemStack stack) {
            CustomItemData customItemData = CustomItemData.fromItemStack(stack);
            int newAmount = stack.getAmount();
            if (cachedAmounts.containsKey(customItemData))
                newAmount += cachedAmounts.get(customItemData);
            cachedAmounts.put(customItemData, newAmount);
        }

        @Override
        public void removeSlotFromCache(int slot, ItemStack stack) {
            CustomItemData customItemData = CustomItemData.fromItemStack(stack);
            if (!cachedAmounts.containsKey(customItemData))
                return;
            int newAmount = cachedAmounts.get(customItemData);
            newAmount -= stack.getAmount();
            if (newAmount > 0)
                cachedAmounts.put(customItemData, newAmount);
            else
                cachedAmounts.remove(customItemData);
        }
    }

    class CachedSlots implements PlayerInventoryCacheStrategy {
        private final Map<CustomItemData, Set<Integer>> dataToSlotMapping = new HashMap<>();
        private final Map<Integer, CustomItemData> slotToDataMapping = new HashMap<>();

        @Override
        public void cacheItemInSlot(int slot, ItemStack stack) {
            CustomItemData customItemData = CustomItemData.fromItemStack(stack);
            dataToSlotMapping.computeIfAbsent(customItemData, v -> new HashSet<>()).add(slot);
            slotToDataMapping.put(slot, customItemData);
        }

        @Override
        public void removeSlotFromCache(int slot, ItemStack stack) {
            if (!slotToDataMapping.containsKey(slot))
                return;
            CustomItemData customItemData = slotToDataMapping.get(slot);
            slotToDataMapping.remove(slot);
            if (!dataToSlotMapping.containsKey(customItemData))
                return;
            dataToSlotMapping.get(customItemData).remove(slot);
        }
    }
}

package de.verdox.mccreativelab.util.player.inventory;

import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.world.item.FakeItem;
import de.verdox.mccreativelab.wrapper.MCCItemType;
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
        private final Map<MCCItemType, Integer> cachedAmounts = new HashMap<>();

        public int getAmount(MCCItemType mccItemType){
            return cachedAmounts.getOrDefault(mccItemType, 0);
        }

        public int getAmount(Material material){
            return getAmount(MCCItemType.of(material));
        }

        public int getAmount(ItemStack stack){
            return getAmount(MCCItemType.of(stack));
        }

        public int getAmount(FakeItem fakeItem){
            return getAmount(MCCItemType.of(fakeItem));
        }

        @Override
        public void cacheItemInSlot(int slot, ItemStack stack) {
            MCCItemType type = MCCItemType.of(stack);
            int newAmount = stack.getAmount();
            if (cachedAmounts.containsKey(type))
                newAmount += cachedAmounts.get(type);
            cachedAmounts.put(type, newAmount);
        }

        @Override
        public void removeSlotFromCache(int slot, ItemStack stack) {
            MCCItemType type = MCCItemType.of(stack);
            if (!cachedAmounts.containsKey(type))
                return;
            int newAmount = cachedAmounts.get(type);
            newAmount -= stack.getAmount();
            if (newAmount > 0)
                cachedAmounts.put(type, newAmount);
            else
                cachedAmounts.remove(type);
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

package de.verdox.mccreativelab.world.item;

import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.registry.CustomRegistry;
import de.verdox.mccreativelab.registry.Reference;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeItemRegistry extends CustomRegistry<FakeItem> {
    private final Map<CustomItemData, Reference<? extends FakeItem>> customItemDataFakeItemMapping = new HashMap<>();
    public <T extends FakeItem> Reference<T> register(FakeItem.Builder<T> fakeItemBuilder){
        T fakeItem = fakeItemBuilder.buildItem();
        Reference<T> result = register(fakeItem.getKey(), fakeItem);
        customItemDataFakeItemMapping.put(CustomItemData.fromItemStack(fakeItem.createItemStack()), result);
        return result;
    }

    @Nullable
    public Reference<? extends FakeItem> getFakeItem(CustomItemData customItemData){
        if(!customItemDataFakeItemMapping.containsKey(customItemData))
            return null;
        return customItemDataFakeItemMapping.get(customItemData);
    }

    public Reference<? extends FakeItem> getFakeItem(@Nullable ItemStack stack){
        return stack == null ? null : getFakeItem(CustomItemData.fromItemStack(stack));
    }


}

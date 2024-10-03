package de.verdox.mccreativelab.world.item;

import de.verdox.mccreativelab.registry.CustomRegistry;
import de.verdox.mccreativelab.registry.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeItemRegistry extends CustomRegistry<FakeItem> {
    private final Map<Entry, Reference<? extends FakeItem>> fakeItemMapping = new HashMap<>();
    public <T extends FakeItem> Reference<T> register(FakeItem.Builder<T> fakeItemBuilder){
        T fakeItem = fakeItemBuilder.buildItem();
        Reference<T> result = register(fakeItem.getKey(), fakeItem);
        fakeItemMapping.put(Entry.of(fakeItem.createItemStack()), result);
        return result;
    }

    @Nullable
    public Reference<? extends FakeItem> getFakeItem(Entry entry){
        if(!fakeItemMapping.containsKey(entry))
            return null;
        return fakeItemMapping.get(entry);
    }

    public Reference<? extends FakeItem> getFakeItem(@Nullable ItemStack stack){
        return stack == null ? null : getFakeItem(Entry.of(stack));
    }

    public record Entry(Material vanillaMaterial, int customModelData){
        public static Entry of(ItemStack stack){
            return new Entry(stack.getType(), stack.hasItemMeta() ? stack.getItemMeta().hasCustomModelData() ? stack.getItemMeta().getCustomModelData() : 0 : 0);
        }
    }


}

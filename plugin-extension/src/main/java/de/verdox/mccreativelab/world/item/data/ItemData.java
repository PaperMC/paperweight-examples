package de.verdox.mccreativelab.world.item.data;

import de.verdox.mccreativelab.util.nbt.NBTSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface ItemData<T extends ItemData<T>> extends NBTSerializable {
    T copy();
    void merge(@Nullable T data);
    boolean hasData(ItemStack stack);
    default String getNBTKey(){
        return getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }
    void applyItemMetaFormat(ItemStack stack, ItemMeta meta);
}

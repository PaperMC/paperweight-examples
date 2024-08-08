package de.verdox.itemformat;

import de.verdox.mccreativelab.CustomBehaviour;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@FunctionalInterface
public interface BasicItemFormat {
    CustomBehaviour<BasicItemFormat> BASIC_FORMAT = new CustomBehaviour<>(BasicItemFormat.class, stack -> {}, "BasicItemFormat");
    void applyItemFormat(ItemStack stack);
    static ItemStack applyItemFormatAndReturn(ItemStack stack){
        return applyItemFormatAndReturn(stack, BASIC_FORMAT.getBehaviour());
    }

    static ItemStack applyItemFormatAndReturn(@NotNull ItemStack stack, @NotNull BasicItemFormat basicItemFormat) {
        Objects.requireNonNull(stack);
        Objects.requireNonNull(basicItemFormat);
        if(needsConversion(stack)){
            basicItemFormat.applyItemFormat(stack);
            applyConversionTag(stack);
        }
        return stack;
    }

    static ItemStack forceItemFormat(@NotNull ItemStack stack, @NotNull BasicItemFormat basicItemFormat){
        Objects.requireNonNull(stack);
        Objects.requireNonNull(basicItemFormat);
        basicItemFormat.applyItemFormat(stack);
        return stack;
    }

    String randomSessionID = Integer.toHexString(ThreadLocalRandom.current().nextInt(1000000));
    NamespacedKey sessionIDKey = new NamespacedKey("mcclab","session_id");
    static boolean needsConversion(ItemStack stack){
        if(org.bukkit.Bukkit.getServer() == null) // When Bukkit has not loaded yet do not use BasicItemFormat
            return false;
        if(stack == null || stack.isEmpty())
            return false;
        if(!stack.hasItemMeta())
            return true;
        if(!stack.getItemMeta().getPersistentDataContainer().has(sessionIDKey))
            return true;
        var storedID = stack.getItemMeta().getPersistentDataContainer().get(sessionIDKey, PersistentDataType.STRING);
        return !randomSessionID.equals(storedID);
    }
    static void applyConversionTag(ItemStack stack){
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(sessionIDKey, PersistentDataType.STRING, randomSessionID));
    }
}

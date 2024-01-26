package de.verdox.mccreativelab.util;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class ItemUtil {
    public static List<Component> insertIntoItemLore(@Nullable List<Component> oldLore, int atIndex, List<Component> loreToInsert){
        List<Component> newLore = new LinkedList<>();
        if(oldLore != null)
            newLore.addAll(oldLore);

        int counter = atIndex;
        for (Component component : loreToInsert) {

            if(counter < loreToInsert.size())
                newLore.add(counter, component);
            else
                newLore.add(component);

            counter++;
        }
        return newLore;
    }

    public static List<Component> appendToItemLore(@Nullable List<Component> oldLore, List<Component> loreToAppend){
        return insertIntoItemLore(oldLore, 9999, loreToAppend);
    }
}

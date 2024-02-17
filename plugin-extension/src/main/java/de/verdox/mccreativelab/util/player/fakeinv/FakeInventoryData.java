package de.verdox.mccreativelab.util.player.fakeinv;

import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.PlayerPersistentData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

public class FakeInventoryData extends PlayerPersistentData {
    private ItemStack[] savedContents;

    public void setSavedContents(ItemStack[] savedContents) {
        this.savedContents = savedContents;
    }

    public ItemStack[] getSavedContents() {
        return savedContents;
    }

    @Override
    public void saveNBTData(NBTContainer storage) {
        storage.set("saved_contents", savedContents);
    }

    @Override
    public void loadNBTData(NBTContainer storage) {
        if(storage.has("saved_contents"))
            savedContents = storage.getItemArray("saved_contents");
    }

    @Override
    protected String nbtKey() {
        return "fake_inventory_data";
    }

    public static void savePlayerInventory(Player player){
        FakeInventoryData.get(FakeInventoryData.class, player).setSavedContents(player.getInventory().getContents());

        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        player.getInventory().clear();
        player.getInventory().setArmorContents(armor);
        player.getInventory().setItemInOffHand(mainHand);
    }

    public static boolean hasActiveFakeInventory(Player player){
        return FakeInventoryData.get(FakeInventoryData.class, player).savedContents != null;
    }
    public static void restorePlayerInventory(Player player){
        FakeInventoryData fakeInventoryData = FakeInventoryData.get(FakeInventoryData.class, player);
        ItemStack[] found = fakeInventoryData.getSavedContents();
        if(found == null)
            return;
        player.getInventory().setContents(found);
        player.updateInventory();
        fakeInventoryData.setSavedContents(null);
        fakeInventoryData.save(player);
    }
}

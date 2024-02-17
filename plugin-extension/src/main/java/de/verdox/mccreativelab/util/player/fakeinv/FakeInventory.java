package de.verdox.mccreativelab.util.player.fakeinv;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;

public class FakeInventory implements Listener {
    public static void setFakeInventoryOfPlayer(Player player){
        FakeInventoryData.savePlayerInventory(player);
    }

    public static boolean hasFakeInventory(Player player){
        return FakeInventoryData.hasActiveFakeInventory(player);
    }

    public static void stopFakeInventoryOfPlayer(Player player){
        FakeInventoryData.restorePlayerInventory(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void playerDeathEvent(PlayerDeathEvent e) {
        stopFakeInventoryOfPlayer(e.getPlayer());

        if (!e.getKeepInventory()) {
            for (ItemStack item : e.getPlayer().getInventory().getContents()) {
                if (item != null && !item.isEmpty() && item.getEnchantmentLevel(Enchantment.VANISHING_CURSE) <= 0) {
                    e.getDrops().add(item); // Paper - drop function taken from Inventory#dropAll (don't fire drop event)
                }
            }
        }
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent e) {
        stopFakeInventoryOfPlayer(e.getPlayer());
    }

    @EventHandler
    public void playerQuitEvent(PlayerJoinEvent e) {
        stopFakeInventoryOfPlayer(e.getPlayer());
    }

    @EventHandler
    public void preventPlayerItemPickup(PlayerAttemptPickupItemEvent e) {
        if (!FakeInventoryData.hasActiveFakeInventory(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void preventClick(InventoryClickEvent e) {
        if (!FakeInventoryData.hasActiveFakeInventory((Player) e.getWhoClicked())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void openContainer(InventoryOpenEvent e) {
        if (!FakeInventoryData.hasActiveFakeInventory((Player) e.getPlayer()) || !(e.getInventory()
                                                                                    .getHolder() instanceof BlockInventoryHolder))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void preventClick(InventoryDragEvent e) {
        if (!FakeInventoryData.hasActiveFakeInventory((Player) e.getWhoClicked())) return;
        e.setCancelled(true);
    }

}

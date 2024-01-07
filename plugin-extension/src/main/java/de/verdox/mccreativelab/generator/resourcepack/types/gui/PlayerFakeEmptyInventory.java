package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerFakeEmptyInventory implements Listener {
    public static final NamespacedKey SAVED_INVENTORY_KEY = new NamespacedKey(MCCreativeLabExtension.getInstance(), "saved_inventory");
    private static PlayerFakeEmptyInventory inventoryStorageSystem;

    public static void registerStoredInventoryListener(JavaPlugin javaPlugin) {
        if (inventoryStorageSystem != null)
            return;
        inventoryStorageSystem = new PlayerFakeEmptyInventory();
        Bukkit.getPluginManager().registerEvents(inventoryStorageSystem, javaPlugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDeathEvent(PlayerDeathEvent e){
        restoreStoredInventory(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void restoreSavedInventoryOnQuit(PlayerQuitEvent e) {
        restoreStoredInventory(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void restoreSavedInventoryOnKick(PlayerKickEvent e) {
        restoreStoredInventory(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void restoreSavedInventoryOnJoin(PlayerJoinEvent e) {
        restoreStoredInventory(e.getPlayer());
    }

    public static PlayerFakeEmptyInventory getInventoryStorageSystem() {
        return inventoryStorageSystem;
    }

    public static void saveInventory(Player player) {
/*        player.getPersistentDataContainer()
              .set(SAVED_INVENTORY_KEY, DataType.ITEM_STACK_ARRAY, player.getInventory().getStorageContents());*/
    }

    public static void restoreStoredInventory(HumanEntity player) {
/*        if (!hasPlayerStoredInventory(player.getPersistentDataContainer()))
            return;
        var oldStorageContents = player.getPersistentDataContainer().get(SAVED_INVENTORY_KEY, DataType.ITEM_STACK_ARRAY);
        if (oldStorageContents == null)
            return;
        player.getInventory().setStorageContents(oldStorageContents);
        player.getPersistentDataContainer().remove(SAVED_INVENTORY_KEY);*/
    }

    private static boolean hasPlayerStoredInventory(PersistentDataContainer persistentDataContainer) {
        return persistentDataContainer.has(SAVED_INVENTORY_KEY);
    }
}

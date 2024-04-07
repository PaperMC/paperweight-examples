package de.verdox.mccreativelab.util.player.inventory;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlayerInventoryCachedData {
    private static final Map<Class<? extends PlayerInventoryCacheStrategy>, Supplier<PlayerInventoryCacheStrategy>> REGISTERED_STRATEGIES = new HashMap<>();

    public static <T extends PlayerInventoryCacheStrategy> void register(Class<? extends T> type, Supplier<T> constructor) {
        REGISTERED_STRATEGIES.put(type, (Supplier<PlayerInventoryCacheStrategy>) constructor);
    }

    public static <T, R extends PlayerInventoryCacheStrategy> T queryPlayerInventory(Player player, Class<? extends R> type, Function<R, T> function) {
        PlayerInventoryCachedData playerInventoryCachedData = (PlayerInventoryCachedData) player
            .getMetadata("cached_inventory_data").get(0).value();
        if (!playerInventoryCachedData.strategies.containsKey(type))
            throw new IllegalStateException("PlayerInventoryCacheStrategy " + type.getName() + " not registered yet!");
        return function.apply(type.cast(playerInventoryCachedData.strategies.get(type)));
    }
    private final Map<Class<? extends PlayerInventoryCacheStrategy>, PlayerInventoryCacheStrategy> strategies = new HashMap<>();
    public PlayerInventoryCachedData() {
        REGISTERED_STRATEGIES.values().stream().map(Supplier::get)
                             .forEach(playerInventoryCacheStrategy -> strategies.put(playerInventoryCacheStrategy.getClass(), playerInventoryCacheStrategy));
    }

    void cacheItemInSlot(int slot, ItemStack stack) {
        strategies.values()
                  .forEach(playerInventoryCacheStrategy -> playerInventoryCacheStrategy.cacheItemInSlot(slot, stack));
    }

    private void removeFromCache(int slot, ItemStack stack) {
        strategies.values()
                  .forEach(playerInventoryCacheStrategy -> playerInventoryCacheStrategy.removeSlotFromCache(slot, stack));
    }

    public static class Listener implements org.bukkit.event.Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onJoin(PlayerJoinEvent e) {
            e.getPlayer()
             .setMetadata("cached_inventory_data", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), new PlayerInventoryCachedData()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventorySlotChange(PlayerInventorySlotChangeEvent e) {
            PlayerInventoryCachedData playerInventoryCachedData = (PlayerInventoryCachedData) e.getPlayer()
                                                                                               .getMetadata("cached_inventory_data")
                                                                                               .get(0).value();
            playerInventoryCachedData.removeFromCache(e.getSlot(), e.getOldItemStack());
            playerInventoryCachedData.cacheItemInSlot(e.getSlot(), e.getNewItemStack());
        }
    }

}

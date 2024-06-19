package de.verdox.mccreativelab.world;

import de.verdox.mccreativelab.event.PlayerCraftItemEvent;
import de.verdox.mccreativelab.wrapper.MCCItemType;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CustomEventsCaller implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void callPlayerCraftItemEvent(InventoryClickEvent e) {
        if (e.getInventory() instanceof CraftingInventory craftingInventory && e.getRawSlot() == 0 && craftingInventory.getResult() != null) {
            Recipe recipe = craftingInventory.getRecipe();
            if (!(recipe instanceof Keyed keyed))
                return;
            NamespacedKey key = keyed.getKey();
            Player player = (Player) e.getWhoClicked();

            // Collecting one from the result
            int amount = 0;
            if (e.getAction().equals(InventoryAction.PICKUP_ALL)) {
                amount = craftingInventory.getResult().getAmount();
            }
            // Collecting all into the inventory
            else if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                int minAmount = 1000;
                // First we find the ingredient with the lowest amount.
                for (ItemStack matrix : craftingInventory.getMatrix()) {
                    if (matrix != null && matrix.getAmount() < minAmount)
                        minAmount = matrix.getAmount();
                }
                // Value has not changed yet
                if (minAmount == 1000)
                    minAmount = 0;
                int amountToBeCraftedInTheory = minAmount * recipe.getResult().getAmount();

                Inventory snapshot = Bukkit.createInventory(null, 4 * 9);
                snapshot.setStorageContents(player.getInventory().getStorageContents());

                int notAdded = 0;

                int counter = amountToBeCraftedInTheory;
                while (counter > 0) {
                    if (counter >= recipe.getResult().getMaxStackSize()) {
                        notAdded += snapshot.addItem(recipe.getResult().asQuantity(recipe.getResult().getMaxStackSize())).values().stream().mapToInt(ItemStack::getAmount).sum();
                        counter -= recipe.getResult().getMaxStackSize();
                    }
                    else {
                        counter = 0;
                        notAdded += snapshot.addItem(recipe.getResult().asQuantity(counter)).values().stream().mapToInt(ItemStack::getAmount).sum();
                    }
                }
                amount = amountToBeCraftedInTheory - notAdded;
                // Now we check if the items can be added to the player inventory
            }

            PlayerCraftItemEvent playerCraftItemEvent = new PlayerCraftItemEvent(e.getView(), e.getSlotType(), e.getSlot(), e.getClick(), e.getAction(), craftingInventory, recipe, key, amount);
            e.setCancelled(!playerCraftItemEvent.callEvent());
        }
    }
}

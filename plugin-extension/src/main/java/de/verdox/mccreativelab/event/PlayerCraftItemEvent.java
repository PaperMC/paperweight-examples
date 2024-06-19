package de.verdox.mccreativelab.event;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player clicks in a result slot of a crafting inventory.
 */
public class PlayerCraftItemEvent extends InventoryClickEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final CraftingInventory craftingInventory;
    private final Recipe recipe;
    private final NamespacedKey recipeKey;
    private final int amountCrafted;
    private boolean cancelled;

    public PlayerCraftItemEvent(@NotNull InventoryView view, InventoryType.@NotNull SlotType type, int slot, @NotNull ClickType click, @NotNull InventoryAction action, @NotNull CraftingInventory craftingInventory, @NotNull Recipe recipe, @NotNull NamespacedKey recipeKey, int amountCrafted) {
        super(view, type, slot, click, action);
        this.craftingInventory = craftingInventory;
        this.recipe = recipe;
        this.recipeKey = recipeKey;
        this.amountCrafted = amountCrafted;
    }

    public int getAmountCrafted() {
        return amountCrafted;
    }

    public NamespacedKey getRecipeKey() {
        return recipeKey;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public CraftingInventory getCraftingInventory() {
        return craftingInventory;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

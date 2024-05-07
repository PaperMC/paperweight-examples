package de.verdox.mccreativelab.event;

import de.verdox.mccreativelab.generator.resourcepack.types.gui.ActiveGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public class GUICloseEvent extends GUIEvent{
    private static final HandlerList handlers = new HandlerList();
    private final InventoryCloseEvent.Reason reason;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public GUICloseEvent(@NotNull Player player, @NotNull ActiveGUI activeGUI, @NotNull InventoryCloseEvent.Reason reason) {
        super(player, activeGUI);
        this.reason = reason;
    }

    public InventoryCloseEvent.Reason getReason() {
        return reason;
    }
}

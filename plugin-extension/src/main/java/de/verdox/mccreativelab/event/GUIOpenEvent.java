package de.verdox.mccreativelab.event;

import de.verdox.mccreativelab.generator.resourcepack.types.gui.ActiveGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GUIOpenEvent extends GUIEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean cancelled;

    public HandlerList getHandlers() {
        return handlers;
    }

    public GUIOpenEvent(@NotNull Player player, ActiveGUI activeGUI) {
        super(player, activeGUI);
    }


    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}

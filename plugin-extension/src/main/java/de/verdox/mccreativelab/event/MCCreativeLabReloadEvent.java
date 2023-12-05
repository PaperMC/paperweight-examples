package de.verdox.mccreativelab.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MCCreativeLabReloadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}

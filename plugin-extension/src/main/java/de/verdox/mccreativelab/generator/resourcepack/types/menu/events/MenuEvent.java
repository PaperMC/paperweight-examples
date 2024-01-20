package de.verdox.mccreativelab.generator.resourcepack.types.menu.events;

import de.verdox.mccreativelab.generator.resourcepack.types.menu.ActiveMenu;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MenuEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ActiveMenu activeMenu;

    public MenuEvent(ActiveMenu activeMenu){
        this.activeMenu = activeMenu;
    }

    public ActiveMenu getActiveMenu() {
        return activeMenu;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}

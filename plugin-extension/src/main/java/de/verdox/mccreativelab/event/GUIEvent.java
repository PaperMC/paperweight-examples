package de.verdox.mccreativelab.event;

import de.verdox.mccreativelab.generator.resourcepack.types.gui.ActiveGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class GUIEvent extends Event {
    private final Player player;
    private final ActiveGUI activeGUI;
    private boolean cancelled;

    public GUIEvent(@NotNull Player player, ActiveGUI activeGUI){
        this.player = player;
        this.activeGUI = activeGUI;
    }

    public Player getPlayer() {
        return player;
    }

    public ActiveGUI getActiveGUI() {
        return activeGUI;
    }
}

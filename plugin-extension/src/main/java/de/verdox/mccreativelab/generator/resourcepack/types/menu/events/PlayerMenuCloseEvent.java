package de.verdox.mccreativelab.generator.resourcepack.types.menu.events;

import de.verdox.mccreativelab.generator.resourcepack.types.menu.ActiveMenu;
import org.bukkit.entity.Player;

public class PlayerMenuCloseEvent extends MenuEvent{
    private final Player player;

    public PlayerMenuCloseEvent(Player player, ActiveMenu activeMenu) {
        super(activeMenu);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}

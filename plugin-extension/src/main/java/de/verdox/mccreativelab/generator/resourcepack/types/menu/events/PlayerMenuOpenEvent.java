package de.verdox.mccreativelab.generator.resourcepack.types.menu.events;

import de.verdox.mccreativelab.generator.resourcepack.types.menu.ActiveMenu;
import org.bukkit.entity.Player;

public class PlayerMenuOpenEvent extends MenuEvent{
    private final Player player;
    private boolean changeHotbar;

    public PlayerMenuOpenEvent(Player player, ActiveMenu activeMenu) {
        super(activeMenu);
        this.player = player;
    }

    public void setChangeHotbar(boolean changeHotbar) {
        this.changeHotbar = changeHotbar;
    }

    public boolean isChangeHotbar() {
        return changeHotbar;
    }
}

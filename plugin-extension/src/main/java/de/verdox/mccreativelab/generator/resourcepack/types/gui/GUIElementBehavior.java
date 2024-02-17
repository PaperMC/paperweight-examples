package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.generator.resourcepack.types.gui.element.active.ActiveGUIElement;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface GUIElementBehavior <T extends ActiveGUIElement<?>> {
    default void onOpen(ActiveGUI parentElement, Player player, T element){}
    default void whileOpen(ActiveGUI parentElement, Player player, T element){}
}

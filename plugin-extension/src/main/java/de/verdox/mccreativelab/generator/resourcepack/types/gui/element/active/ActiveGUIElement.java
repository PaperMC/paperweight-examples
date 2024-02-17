package de.verdox.mccreativelab.generator.resourcepack.types.gui.element.active;

import de.verdox.mccreativelab.generator.resourcepack.types.gui.ActiveGUI;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.element.GUIElement;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudTexture;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ActiveGUIElement<T extends GUIElement> {
    protected final ActiveGUI activeGUI;
    protected final T guiElement;

    public ActiveGUIElement(@NotNull ActiveGUI activeGUI, T guiElement) {
        this.activeGUI = activeGUI;
        this.guiElement = guiElement;
    }

    @NotNull
    public ActiveGUI getActiveGUI() {
        return activeGUI;
    }

    public T getGuiElement() {
        return guiElement;
    }

    public abstract void setVisible(boolean visible);
    public abstract void onClick(InventoryClickEvent inventoryClickEvent, int clickedX, int clickedY);
}

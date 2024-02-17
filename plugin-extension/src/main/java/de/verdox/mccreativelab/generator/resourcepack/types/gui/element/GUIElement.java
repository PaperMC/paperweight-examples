package de.verdox.mccreativelab.generator.resourcepack.types.gui.element;

import de.verdox.mccreativelab.generator.resourcepack.types.gui.ActiveGUI;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.element.active.ActiveGUIElement;

public interface GUIElement {
    ActiveGUIElement<?> toActiveElement(ActiveGUI activeGUI);
}

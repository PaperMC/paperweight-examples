package de.verdox.mccreativelab.generator.resourcepack.types.gui;

public interface GUIElement {
    record ClickableButton(ClickableItem clickableItem, int startIndex, String textureID, String textID) implements GUIElement {
    }
}

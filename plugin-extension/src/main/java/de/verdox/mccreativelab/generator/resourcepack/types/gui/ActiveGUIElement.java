package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudTexture;

public abstract class ActiveGUIElement<T extends GUIElement> {
    protected final ActiveGUI activeGUI;
    protected final T guiElement;

    public ActiveGUIElement(ActiveGUI activeGUI, T guiElement) {
        this.activeGUI = activeGUI;
        this.guiElement = guiElement;
    }

    public abstract void setVisible(boolean visible);

    public static class ClickableButton extends ActiveGUIElement<GUIElement.ClickableButton> {
        public ClickableButton(ActiveGUI activeGUI, GUIElement.ClickableButton guiElement) {
            super(activeGUI, guiElement);
        }

        @Override
        public void setVisible(boolean value) {
            activeGUI.getActiveHud().executeOnElement(this.guiElement.textureID(), SingleHudTexture.RenderedSingleHudTexture.class, texture -> texture.setVisible(value));
            activeGUI.getActiveHud().executeOnElement(this.guiElement.textID(), SingleHudText.RenderedSingleHudText.class, text -> text.setVisible(value));

            if (value)
                activeGUI.setItem(this.guiElement.startIndex(), this.guiElement.clickableItem());
            else {
                activeGUI.removeClickableItemFromSlot(this.guiElement.startIndex(), this.guiElement.clickableItem());
                activeGUI.getActiveHud().executeOnElement(this.guiElement.textID(), SingleHudText.RenderedSingleHudText.class, SingleHudText.RenderedSingleHudText::clearRenderedText);
            }

        }

        public void setRenderedText(String textToDisplay) {
            setVisible(true);
            activeGUI.getActiveHud()
                     .executeOnElement(this.guiElement.textID(), SingleHudText.RenderedSingleHudText.class, text -> text.setRenderedText(textToDisplay));
        }
    }
}

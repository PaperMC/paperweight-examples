package de.verdox.mccreativelab.generator.resourcepack.renderer.element.group;

import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.HudElementGroup;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.SingleHudElement;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudTexture;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Button(SingleHudText text, @Nullable SingleHudTexture selected, @Nullable SingleHudTexture enabled,
                     @Nullable SingleHudTexture disabled) implements HudElementGroup<SingleHudElement> {
    @Override
    public ScreenPosition screenPosition() {
        return text.screenPosition();
    }

    @Override
    public RenderedButton toRenderedElement() {
        return new RenderedButton(this, List.of(text.toRenderedElement(), enabled.toRenderedElement(), disabled.toRenderedElement(), selected.toRenderedElement()));
    }

    public static class RenderedButton extends RenderedGroup<SingleHudElement, Button, SingleHudElement.RenderedSingle<? extends SingleHudElement>> {
        SingleHudText.RenderedSingleHudText buttonText;
        SingleHudTexture.RenderedSingleHudTexture buttonEnabled;
        SingleHudTexture.RenderedSingleHudTexture buttonDisabled;
        SingleHudTexture.RenderedSingleHudTexture buttonSelected;

        public RenderedButton(Button hudElementGroup, List<SingleHudElement.RenderedSingle<? extends SingleHudElement>> groupedRenderedElements) {
            super(hudElementGroup, groupedRenderedElements);
            buttonText = (SingleHudText.RenderedSingleHudText) groupedRenderedElements.get(0);
            buttonEnabled = (SingleHudTexture.RenderedSingleHudTexture) groupedRenderedElements.get(1);
            buttonDisabled = (SingleHudTexture.RenderedSingleHudTexture) groupedRenderedElements.get(2);
            buttonSelected = (SingleHudTexture.RenderedSingleHudTexture) groupedRenderedElements.get(3);
            enableButton();
        }

        public void enableButton() {
            showButtonTextures(false, true, false);
        }

        public void selectButton() {
            showButtonTextures(true, false, false);
        }

        public void disableButton() {
            showButtonTextures(false, false, true);
        }

        private void showButtonTextures(boolean selected, boolean enabled, boolean disabled) {
            if (this.buttonEnabled != null)
                this.buttonEnabled.setVisible(enabled);
            if (this.buttonDisabled != null)
                this.buttonDisabled.setVisible(disabled);
            if (this.buttonSelected != null)
                this.buttonSelected.setVisible(selected);
        }
        public boolean isEnabled() {
            return !buttonDisabled.isVisible();
        }
        public void setRenderedText(String renderedText) {
            this.buttonText.setRenderedText(renderedText);
        }
        public void clearRenderedText() {
            this.buttonText.clearRenderedText();
        }

        private Component renderButtonTextureSafely(CustomHud customHud, SingleHudTexture.RenderedSingleHudTexture texture) {
            return texture != null ? texture.render(customHud) : Component.empty();
        }

        @Override
        protected Component doRendering(CustomHud customHud) {
            return buttonText.render(customHud)
                             .append(renderButtonTextureSafely(customHud, buttonEnabled))
                             .append(renderButtonTextureSafely(customHud, buttonSelected))
                             .append(renderButtonTextureSafely(customHud, buttonDisabled));
        }
    }
}

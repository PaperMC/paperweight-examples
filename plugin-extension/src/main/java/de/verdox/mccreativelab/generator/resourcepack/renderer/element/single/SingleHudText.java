package de.verdox.mccreativelab.generator.resourcepack.renderer.element.single;

import com.google.common.base.Strings;
import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.SingleHudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * @param font           Font that is used
 * @param screenPosition Position on screen while (0,0) is down left corner
 * @param alignment      Alignment for the text to use
 * @param alignmentWidth alignment width in pixels to adjust the alignment
 */
public record SingleHudText(Font font, ScreenPosition screenPosition, @Nullable StringAlign.Alignment alignment,
                            int alignmentWidth) implements SingleHudElement {

    @Override
    public RenderedSingleHudText toRenderedElement() {
        return new RenderedSingleHudText(this);
    }

    public static class RenderedSingleHudText extends RenderedSingle<SingleHudText> {
        private TextComponent renderedText;

        public RenderedSingleHudText(SingleHudText hudElement) {
            super(hudElement);
        }

        @Override
        protected void onVisibilityChange(boolean newVisibility) {
            if (!newVisibility)
                this.renderedText = null;
        }

        public TextComponent getRenderedText() {
            return renderedText;
        }

        public void setRenderedText(TextComponent renderedText) {
            setVisible(true);
            this.renderedText = renderedText;
        }

        public void setRenderedText(String text) {
            this.setRenderedText(Component.text(text.replace(" ", Strings.repeat(" ", 4)))); // TODO: Spaces are not seperated far enough so we append some more. This is a quick fix. Needs revision
        }

        public void clearRenderedText() {
            setVisible(false);
        }

        @Override
        protected Component doRendering(CustomHud customHud) {
            if (renderedText == null || renderedText.content().isEmpty())
                return Component.text("");
            SingleHudText hudText = getHudElement();

            if (hudText.alignment() == null)
                return createTextComponent();

            var textLength = hudText.font().getPixelWidth(ChatColor.stripColor(renderedText.content()));

            return switch (hudText.alignment()) {
                case LEFT -> createTextComponent();
                case CENTER -> createNegativeSpacing(textLength / 2).append(createTextComponent())
                                                                    .append(createSpacing(textLength / 2));
                case RIGHT -> createNegativeSpacing(textLength).append(createTextComponent())
                                                               .append(createSpacing(textLength));
            };
        }

        @VisibleForTesting
        public Component createTextComponent() {
            SingleHudText hudText = getHudElement();
            NamespacedKey fontKey;
            fontKey = hudText.font().key();

            var textFont = Key.key(fontKey.toString());
            if (renderedText == null)
                return Component.empty();

            /*            var alignmentPixels = hudText.alignmentWidth() >= renderedText.length() ? hudText.alignmentWidth() : renderedText.length();*/
            var textLength = hudText.font().getPixelWidth(ChatColor.stripColor(renderedText.content()));

            TextComponent textToRender = renderedText;
/*            if(hudText.alignment != null)
                textToRender = hudText.alignment().align(renderedText, alignmentPixels);*/

            return textToRender.font(textFont).append(createNegativeSpacing(textLength));
        }

        private Component createNegativeSpacing(int spacing) {
            return Component.translatable("space.-" + Math.abs(spacing)).font(Key.key("space:default"));
        }

        private Component createSpacing(int spacing) {
            return Component.translatable("space." + Math.abs(spacing)).font(Key.key("space:default"));
        }
    }
}

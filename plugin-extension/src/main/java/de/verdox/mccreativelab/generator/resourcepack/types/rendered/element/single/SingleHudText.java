package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.SingleHudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class SingleHudText implements SingleHudElement {
    private final Font font;
    private ScreenPosition screenPosition;
    private StringAlign.Alignment alignment;
    private final float textScale;

    /**
     * @param font           Font that is used
     * @param screenPosition Position on screen while (0,0) is down left corner
     * @param alignment      Alignment for the text to use
     */
    public SingleHudText(Font font, ScreenPosition screenPosition, @Nullable StringAlign.Alignment alignment, float textScale) {
        this.font = font;
        this.screenPosition = screenPosition;
        this.alignment = alignment;
        this.textScale = textScale;
    }

    @Override
    public JsonObject serializeToJson() {
        return JsonObjectBuilder
            .create()
            .add("alignment", alignment.name())
            .add("pos", ScreenPosition.toJson(screenPosition))
            .build();
    }

    @Override
    public void deserializeFromJson(JsonObject jsonObject) {
        this.alignment = StringAlign.Alignment.valueOf(jsonObject.getAsJsonPrimitive("alignment").getAsString());
        this.screenPosition = ScreenPosition.fromJson(screenPosition, jsonObject.getAsJsonObject("pos"));
    }

    @Override
    public ScreenPosition screenPosition() {
        return screenPosition;
    }

    public Font font() {
        return font;
    }

    public float scale() {
        return textScale;
    }

    public StringAlign.Alignment alignment() {
        return alignment;
    }

    @Override
    public RenderedSingleHudText toRenderedElement() {
        return new RenderedSingleHudText(this);
    }

    public static class RenderedSingleHudText extends RenderedSingle<SingleHudText> {
        private TextComponent renderedText;
        private int textLength;

        public RenderedSingleHudText(SingleHudText hudElement) {
            super(hudElement);
        }

        @Override
        protected void onVisibilityChange(boolean newVisibility) {
        }

        public TextComponent getRenderedText() {
            return renderedText;
        }

        public void setRenderedText(TextComponent renderedText) {
            setVisible(true);
            this.renderedText = reFormatText(renderedText);
            this.textLength = getTextLength(this.renderedText);
        }

        @Deprecated
        public void setRenderedText(String text) {
            this.setRenderedText(Component.text(text)); // TODO: Spaces are not seperated far enough so we append some more. This is a quick fix. Needs revision
        }

        public void clearRenderedText() {
            setVisible(false);
            this.renderedText = null;
        }

        @Override
        protected Component doRendering(ActiveComponentRendered<?, ?> activeComponentRendered) {
            if (renderedText == null || renderedText.content().isEmpty())
                return Component.empty();
            SingleHudText hudText = getHudElement();

            if (hudText.alignment() == null)
                return createTextComponent();

            String text = ChatColor.stripColor(renderedText.content());
            int textLength = hudText.font().getPixelWidth(text);
            //textLength = FontUtil.calculateStringLength(text);

            return switch (hudText.alignment()) {
                case LEFT -> createTextComponent();
                case CENTER -> createNegativeSpacing(Math.round(textLength * 1f / 2)).append(createTextComponent())
                    .append(createSpacing(Math.round(textLength * 1f / 2)));
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


            TextComponent textToRender = renderedText;
            return textToRender.font(textFont).append(createNegativeSpacing(textLength));
        }

        // Methode zur Ersetzung aller Leerzeichen in einem TextComponent durch 'A'
        private TextComponent reFormatText(TextComponent textComponent) {
            String modifiedContent = textComponent.content().replace(" ", " ".repeat(4));

            // Erstellen eines neuen TextComponents mit modifiziertem Inhalt
            TextComponent modified = Component.text(modifiedContent);

            // Rekursion über alle Kinder des Components und Anhängen der modifizierten Kinder
            for (Component child : textComponent.children()) {
                if(child instanceof TextComponent childText)
                    modified = modified.append(reFormatText(childText));
                else
                    modified = modified.append(child);
            }

            return modified.style(textComponent.style());
        }

        private int getTextLength(TextComponent textComponent) {
            int length = getHudElement().font().getPixelWidth(textComponent.content());
            for (Component child : textComponent.children()) {
                if (!(child instanceof TextComponent component)) {
                    if (child instanceof TranslatableComponent)
                        throw new IllegalStateException("HudTexts don't support components of type TranslatableComponent. Only TextComponents are allowed!");
                    continue;
                }
                length += getTextLength(component);
            }
            return length;
        }

        private Component createNegativeSpacing(int spacing) {
            return Component.translatable("space.-" + Math.abs(spacing)).font(Key.key("space:default"));
        }

        private Component createSpacing(int spacing) {
            return Component.translatable("space." + Math.abs(spacing)).font(Key.key("space:default"));
        }
    }
}

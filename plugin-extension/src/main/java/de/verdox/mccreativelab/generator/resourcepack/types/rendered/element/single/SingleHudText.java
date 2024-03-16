package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.SingleHudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.util.FontUtil;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
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

        public RenderedSingleHudText(SingleHudText hudElement) {
            super(hudElement);
        }

        @Override
        protected void onVisibilityChange(boolean newVisibility) {}

        public TextComponent getRenderedText() {
            return renderedText;
        }

        public void setRenderedText(TextComponent renderedText) {
            setVisible(true);
            this.renderedText = renderedText.content(renderedText.content().replace(" ", Strings.repeat(" ", 4)));
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

            String text = ChatColor.stripColor(renderedText.content());
            int textLength = hudText.font().getPixelWidth(text);
            //textLength = FontUtil.calculateStringLength(text);


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

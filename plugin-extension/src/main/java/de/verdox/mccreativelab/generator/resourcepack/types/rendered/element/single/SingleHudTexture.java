package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.SingleHudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.font.BitMap;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;

public class SingleHudTexture implements SingleHudElement {
    private final BitMap bitMap;
    private final String character;
    private int width;
    private int height;
    private ScreenPosition screenPosition;

    public SingleHudTexture(BitMap bitMap, String character, int width, int height, ScreenPosition screenPosition){
        this.bitMap = bitMap;
        this.character = character;
        this.width = width;
        this.height = height;
        this.screenPosition = screenPosition;
    }

    @Override
    public JsonObject serializeToJson() {
        return JsonObjectBuilder
            .create()
            .add("width", width)
            .add("height", height)
            .add("pos", ScreenPosition.toJson(screenPosition))
            .build();
    }

    @Override
    public void deserializeFromJson(JsonObject jsonObject) {
        this.screenPosition = ScreenPosition.fromJson(screenPosition, jsonObject.getAsJsonObject("pos"));
        this.width = jsonObject.getAsJsonPrimitive("width").getAsInt();
        this.height = jsonObject.getAsJsonPrimitive("height").getAsInt();
    }

    public BitMap bitMap() {
        return bitMap;
    }

    public String character() {
        return character;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public ScreenPosition screenPosition() {
        return screenPosition;
    }

    @Override
    public RenderedSingle<? extends SingleHudElement> toRenderedElement() {
        return new RenderedSingleHudTexture(this);
    }

    public static class RenderedSingleHudTexture extends RenderedSingle<SingleHudTexture> {
        public RenderedSingleHudTexture(SingleHudTexture hudElement) {
            super(hudElement);
        }

        @Override
        protected void onVisibilityChange(boolean newVisibility) {}

        @Override
        protected Component doRendering(ActiveComponentRendered<?,?> activeComponentRendered) {
            NamespacedKey fontKey;
            fontKey = activeComponentRendered.getComponentRendered().getHudTexturesFont().key();

            //TODO: Texture width aber ohne transparente Pixel. Muss umgeschrieben werden
            return Component.text(getHudElement().bitMap().character()[0]).color(TextColor.color(255, 255, 255)).font(Key.key(fontKey.toString()))
                            .append(Component.translatable("space.-" + ((getHudElement().width() + 1)))
                                             .font(Key.key("space:default")));
        }

        private Component createNegativeSpacing(int spacing) {
            return Component.translatable("space.-" + Math.abs(spacing + 1)).font(Key.key("space:default"));
        }

        private Component createSpacing(int spacing) {
            return Component.translatable("space." + Math.abs(spacing + 1)).font(Key.key("space:default"));
        }
    }

}

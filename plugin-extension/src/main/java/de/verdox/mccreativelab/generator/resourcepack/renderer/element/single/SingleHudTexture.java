package de.verdox.mccreativelab.generator.resourcepack.renderer.element.single;

import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.SingleHudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.font.BitMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;

public record SingleHudTexture(BitMap bitMap, String character, int width, int height, ScreenPosition screenPosition) implements SingleHudElement {

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
        protected Component doRendering(CustomHud customHud) {
            NamespacedKey fontKey;
            fontKey = customHud.getHudTexturesFont().key();

            //TODO: Texture width aber ohne transparente Pixel. Muss umgeschrieben werden
            return Component.text(getHudElement().bitMap().character()[0]).color(TextColor.color(0,0,0)).font(Key.key(fontKey.toString()))
                            .append(Component.translatable("space.-" + ((getHudElement().width() + 1)))
                                             .font(Key.key("space:default")));
        }
    }

}

package de.verdox.mccreativelab.generator.resourcepack.renderer;

import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import net.kyori.adventure.text.Component;

public interface HudElement {
    ScreenPosition screenPosition();
    Rendered<?> toRenderedElement();

    abstract class Rendered<H extends HudElement> {
        private boolean visible;
        private final H hudElement;

        public Rendered(H hudElement){
            this.hudElement = hudElement;
        }

        public H getHudElement() {
            return hudElement;
        }

        public final Component render(CustomHud customHud) {
            if (!isVisible())
                return Component.empty();
            return doRendering(customHud);
        }



        public final void setVisible(boolean visible) {
            this.visible = visible;
            onVisibilityChange(visible);
        }
        public final boolean isVisible() {
            return visible;
        }
        protected abstract void onVisibilityChange(boolean newVisibility);
        protected abstract Component doRendering(CustomHud customHud);
    }
}

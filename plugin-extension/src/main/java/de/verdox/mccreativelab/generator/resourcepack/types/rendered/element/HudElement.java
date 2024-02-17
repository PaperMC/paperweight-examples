package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element;

import de.verdox.mccreativelab.generator.JsonConfigurable;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import net.kyori.adventure.text.Component;

public interface HudElement extends JsonConfigurable {
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

        public final Component render(ActiveComponentRendered<?,?> activeComponentRendered) {
            if (!isVisible())
                return Component.empty();
            return doRendering(activeComponentRendered);
        }



        public final void setVisible(boolean visible) {
            this.visible = visible;
            onVisibilityChange(visible);
        }
        public final boolean isVisible() {
            return visible;
        }
        protected abstract void onVisibilityChange(boolean newVisibility);
        protected abstract Component doRendering(ActiveComponentRendered<?,?> activeComponentRendered);
    }
}

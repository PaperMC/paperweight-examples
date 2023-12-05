package de.verdox.mccreativelab.generator.resourcepack.renderer.element;

import de.verdox.mccreativelab.generator.resourcepack.renderer.HudElement;

public interface SingleHudElement extends HudElement {
    RenderedSingle<? extends SingleHudElement> toRenderedElement();

    abstract class RenderedSingle<H extends SingleHudElement> extends Rendered<H>{
        public RenderedSingle(H hudElement) {
            super(hudElement);
        }
    }
}

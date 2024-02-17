package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element;

public interface SingleHudElement extends HudElement {
    RenderedSingle<? extends SingleHudElement> toRenderedElement();

    abstract class RenderedSingle<H extends SingleHudElement> extends Rendered<H>{
        public RenderedSingle(H hudElement) {
            super(hudElement);
        }
    }
}

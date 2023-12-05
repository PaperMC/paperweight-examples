package de.verdox.mccreativelab.generator.resourcepack.renderer.element;

import de.verdox.mccreativelab.generator.resourcepack.renderer.HudElement;

import java.util.List;

public interface HudElementGroup<H extends SingleHudElement> extends HudElement {
    RenderedGroup<H, ? extends HudElementGroup<H>, ?> toRenderedElement();
    abstract class RenderedGroup<I extends SingleHudElement, H extends HudElementGroup<I>, R extends SingleHudElement.RenderedSingle<? extends I>> extends Rendered<H>{
        private final List<R> groupedRenderedElements;
        public RenderedGroup(H hudElementGroup, List<R> groupedRenderedElements) {
            super(hudElementGroup);
            this.groupedRenderedElements = groupedRenderedElements;
        }

        public List<R> getGroupedRenderedElements() {
            return groupedRenderedElements;
        }

        @Override
        protected final void onVisibilityChange(boolean newVisibility) {
            groupedRenderedElements.forEach(r -> r.setVisible(newVisibility));
        }
    }
}

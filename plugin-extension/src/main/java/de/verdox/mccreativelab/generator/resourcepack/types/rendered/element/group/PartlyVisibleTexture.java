package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group;

import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.HudElementGroup;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudTexture;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.stream.Collectors;

public record PartlyVisibleTexture(List<SingleHudTexture> hudTextures) implements HudElementGroup<SingleHudTexture> {
    @Override
    public ScreenPosition screenPosition() {
        return hudTextures.get(0).screenPosition();
    }

    @Override
    public RenderedGroup<SingleHudTexture, ? extends HudElementGroup<SingleHudTexture>, ?> toRenderedElement() {
        return new RenderedPartlyVisibleTexture(this, hudTextures.stream().map(SingleHudTexture::toRenderedElement)
                                                           .map(renderedSingle -> ((SingleHudTexture.RenderedSingleHudTexture) renderedSingle))
                                                           .collect(Collectors.toList()));
    }

    public static class RenderedPartlyVisibleTexture extends RenderedGroup<SingleHudTexture, PartlyVisibleTexture, SingleHudTexture.RenderedSingleHudTexture> {
        public RenderedPartlyVisibleTexture(PartlyVisibleTexture hudElementGroup, List<SingleHudTexture.RenderedSingleHudTexture> groupedRenderedElements) {
            super(hudElementGroup, groupedRenderedElements);
        }

        public void show(int partToShow) {
            if (partToShow < 0 || partToShow > getGroupedRenderedElements().size())
                return;

            for (int i = 0; i < getGroupedRenderedElements().size(); i++) {
                var part = getGroupedRenderedElements().get(i);


                part.setVisible(i == partToShow - 1);
                if(part.getHudElement().height() == 1 && part.getHudElement().width() == 1)
                    part.setVisible(false);
            }
        }

        @Override
        protected Component doRendering(ActiveComponentRendered<?,?> activeComponentRendered) {
            return Component.empty();
        }
    }
}

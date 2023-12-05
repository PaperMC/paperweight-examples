package de.verdox.mccreativelab.generator.resourcepack.renderer.element.group;

import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.HudElementGroup;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.stream.Collectors;

public record HudMultiLineText(List<SingleHudText> lines, int charsPerLine,
                               StringAlign.Alignment alignment) implements HudElementGroup<SingleHudText> {
    @Override
    public ScreenPosition screenPosition() {
        return lines.size() >= 1 ? lines.get(0).screenPosition() : new ScreenPosition(0, 0, 0, 0, 1);
    }

    @Override
    public RenderedGroup<SingleHudText, ? extends HudElementGroup<SingleHudText>, ?> toRenderedElement() {
        return new RenderedGroupMultiLineText(this, lines.stream().map(SingleHudText::toRenderedElement)
                                                         .map(renderedSingle -> ((SingleHudText.RenderedSingleHudText) renderedSingle))
                                                         .collect(Collectors.toList()));
    }


    public static class RenderedGroupMultiLineText extends RenderedGroup<SingleHudText, HudMultiLineText, SingleHudText.RenderedSingleHudText> {
        public RenderedGroupMultiLineText(HudMultiLineText hudElementGroup, List<SingleHudText.RenderedSingleHudText> groupedRenderedElements) {
            super(hudElementGroup, groupedRenderedElements);
        }

        @Override
        protected Component doRendering(CustomHud customHud) {
            var cmp = Component.text("");
            for (var renderedElement : getGroupedRenderedElements())
                cmp = cmp.append(renderedElement.render(customHud));
            return cmp;
        }
    }

}

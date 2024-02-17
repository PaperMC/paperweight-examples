package de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group;

import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.HudElementGroup;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

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
                                                         .collect(Collectors.toList()));
    }


    public static class RenderedGroupMultiLineText extends RenderedGroup<SingleHudText, HudMultiLineText, SingleHudText.RenderedSingleHudText> {
        public RenderedGroupMultiLineText(HudMultiLineText hudElementGroup, List<SingleHudText.RenderedSingleHudText> groupedRenderedElements) {
            super(hudElementGroup, groupedRenderedElements);
        }

        public void setRawText(List<String> text) {
            for (int line = 0; line < text.size() && line < getGroupedRenderedElements().size(); line++) {
                getGroupedRenderedElements().get(line).setRenderedText(text.get(line));
            }
        }

        public void setText(List<TextComponent> text) {
            for (int line = 0; line < text.size() && line < getGroupedRenderedElements().size(); line++) {
                getGroupedRenderedElements().get(line).setRenderedText(text.get(line));
            }
        }

        public void clearText() {
            getGroupedRenderedElements().forEach(SingleHudText.RenderedSingleHudText::clearRenderedText);
        }

        @Override
        protected Component doRendering(ActiveComponentRendered<?, ?> activeComponentRendered) {
            var cmp = Component.text("");
            for (var renderedElement : getGroupedRenderedElements())
                cmp = cmp.append(renderedElement.render(activeComponentRendered));
            return cmp;
        }
    }

}

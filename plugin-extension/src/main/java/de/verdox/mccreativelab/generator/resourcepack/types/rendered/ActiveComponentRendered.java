package de.verdox.mccreativelab.generator.resourcepack.types.rendered;

import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.HudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @param <T> - Self reference
 */
public abstract class ActiveComponentRendered<T extends ActiveComponentRendered<T, C>, C extends ComponentRendered<C, T>> {
    private final Map<String, HudElement.Rendered<?>> renderedElements = new LinkedHashMap<>();
    private final Map<HudElement, HudElement.Rendered<?>> hudElementToRenderedElementMapping = new HashMap<>();
    private final Player player;
    private final C componentRendered;
    private final List<HudElement.Rendered<?>> sorted;
    private Component lastRendered;
    private boolean needsUpdate = true;

    public ActiveComponentRendered(Player player, C componentRendered) {
        this.player = player;
        this.componentRendered = componentRendered;
        componentRendered.getElements()
                         .forEach((s, hudElement) -> registerElement(s, hudElement, hudElement.toRenderedElement()));

        this.sorted = List.copyOf(renderedElements.values()).stream().sorted(Comparator.comparingInt(rendered -> {
            if (rendered instanceof SingleHudText.RenderedSingleHudText renderedSingleHudText) {
                StringAlign.Alignment alignment = renderedSingleHudText.getHudElement().alignment();
                if (alignment == null)
                    alignment = StringAlign.Alignment.LEFT;
                // We want to sort them by alignment -> Else we get weird spacing behaviour
                return switch (alignment) {
                    case LEFT -> -1;
                    case CENTER -> 0;
                    case RIGHT -> 1;
                };
            }
            return -1;
        })).toList();

        forEachElementBehavior((activeHudRenderedHudElementBehavior, rendered) -> activeHudRenderedHudElementBehavior.onOpen((T) this, player, rendered));
    }

    public final void forEachElementBehavior(BiConsumer<RenderedElementBehavior<T, HudElement.Rendered<?>>, HudElement.Rendered<?>> forEach) {
        for (HudElement.Rendered<?> rendered : this.sorted) {
            HudElement hudElement = rendered.getHudElement();
            RenderedElementBehavior<T, HudElement.Rendered<?>> behavior = (RenderedElementBehavior<T, HudElement.Rendered<?>>) componentRendered
                .getBehaviors().getOrDefault(hudElement, null);
            if (behavior != null)
                forEach.accept(behavior, rendered);
        }
        forceUpdate();
    }

    public final void hideAll() {
        renderedElements.forEach((s, renderedElement) -> renderedElement.setVisible(false));
        forceUpdate();
        player.sendActionBar(Component.empty());
    }

    public final void showAll() {
        renderedElements.forEach((s, renderedElement) -> renderedElement.setVisible(true));
        forceUpdate();
    }

    public final void forceUpdate() {
        if (this.player == null || !this.player.isOnline())
            return;
        doUpdate();
        this.needsUpdate = true;
    }

    protected abstract void doUpdate();

    public final C getComponentRendered() {
        return componentRendered;
    }
    public final Component render() {
        if (needsUpdate) {
            Component component = Component.empty();

            for (HudElement.Rendered<?> element : sorted) {
                if(!element.isVisible())
                    continue;
                // Last Element does not need any spacing
                component = component.append(element.render(this));
            }


            lastRendered = component;
            needsUpdate = false;
        }
        return lastRendered;
    }
    public final <H extends HudElement.Rendered<?>> boolean editRenderedElement(String id, Class<? extends H> type, Consumer<H> execution) {
        return edit(id, renderedElements, type, h -> {
            h.setVisible(true);
            execution.accept(h);
        });
    }

    protected final <H> boolean edit(String id, Map<String, ? super H> map, Class<? extends H> type, Consumer<H> execution) {
        var element = map.getOrDefault(id, null);
        if (element != null && type.isAssignableFrom(element.getClass())) {
            execution.accept(type.cast(element));
            forceUpdate();
            return true;
        }
        return false;
    }

    public final <H extends HudElement.Rendered<?>, R> R readOnElementOrDefault(String id, Class<? extends H> type, Function<H, R> execution, R defaultValue) {
        var element = renderedElements.getOrDefault(id, null);
        if (element != null && type.isAssignableFrom(element.getClass())) {
            element.setVisible(true);
            return execution.apply(type.cast(element));
        }
        return defaultValue;
    }
    private void registerElement(String id, HudElement hudElement, HudElement.Rendered<?> renderedElement) {
        if (renderedElements.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already registered in active hud " + getComponentRendered().key());
        if (hudElementToRenderedElementMapping.containsKey(hudElement))
            throw new IllegalArgumentException("Hud element " + hudElement + " already registered in active hud for " + getComponentRendered().key());
        this.renderedElements.put(id, renderedElement);
        this.hudElementToRenderedElementMapping.put(hudElement, renderedElement);
    }
    public Player getPlayer() {
        return player;
    }
}

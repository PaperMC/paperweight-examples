package de.verdox.mccreativelab.generator.resourcepack.renderer;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.HudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActiveHud {
    private final Map<String, HudElement.Rendered<?>> renderedElements = new LinkedHashMap<>();
    private final Map<HudElement, HudElement.Rendered<?>> hudElementToRenderedElementMapping = new HashMap<>();
    private final Player player;
    private final CustomHud customHud;
    private Component lastRendered;
    private boolean needsUpdate = true;

    public ActiveHud(Player player, CustomHud customHud){
        this.player = player;
        this.customHud = customHud;
        customHud.getElements().forEach((s, hudElement) -> registerElement(s, hudElement, hudElement.toRenderedElement()));
    }

    public void hideAll() {
        renderedElements.forEach((s, renderedElement) -> renderedElement.setVisible(false));
        forceUpdate();
        player.sendActionBar(Component.empty());
    }

    public void showAll() {
        renderedElements.forEach((s, renderedElement) -> renderedElement.setVisible(true));
        forceUpdate();
    }

    public void forceUpdate() {
        this.needsUpdate = true;
        if(player == null)
            return;
        if (MCCreativeLabExtension.getInstance().getHudRenderer().getActiveHud(player, getCustomHud()) != null)
            MCCreativeLabExtension.getInstance().getHudRenderer().forceUpdate(player);
    }

    public CustomHud getCustomHud() {
        return customHud;
    }

    public Component render() {
        if (needsUpdate) {
            Component component = Component.empty();

            var renderingOrder = new LinkedList<>(renderedElements.values());

            for (HudElement.Rendered<?> element : renderingOrder) {
                // Last Element does not need any spacing
                component = component.append(element.render(customHud));
            }


            lastRendered = component;
            needsUpdate = false;
        }
        return lastRendered;
    }

    public <T extends HudElement.Rendered<?>, R> boolean executeOnElement(String id, Class<? extends T> type, Consumer<T> execution) {
        var element = renderedElements.getOrDefault(id, null);
        if (element != null && type.isAssignableFrom(element.getClass())) {
            element.setVisible(true);
            execution.accept(type.cast(element));
            forceUpdate();
            return true;
        }
        return false;
    }

    public <T extends HudElement.Rendered<?>, R> R readOnElementOrDefault(String id, Class<? extends T> type, Function<T,R> execution, R defaultValue){
        var element = renderedElements.getOrDefault(id, null);
        if (element != null && type.isAssignableFrom(element.getClass())) {
            element.setVisible(true);
            return execution.apply(type.cast(element));
        }
        return defaultValue;
    }

    private void registerElement(String id, HudElement hudElement, HudElement.Rendered<?> renderedElement) {
        if (renderedElements.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already registered in active hud " + customHud.key());
        if (hudElementToRenderedElementMapping.containsKey(hudElement))
            throw new IllegalArgumentException("Hud element " + hudElement + " already registered in active hud for " + customHud.key());
        this.renderedElements.put(id, renderedElement);
        this.hudElementToRenderedElementMapping.put(hudElement, renderedElement);
    }

    public Player getPlayer() {
        return player;
    }
}

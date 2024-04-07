package de.verdox.mccreativelab.generator.resourcepack.types.rendered;

import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.HudElement;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;

public interface RenderedElementBehavior<V extends ActiveComponentRendered<V,?>, T extends HudElement.Rendered<?>> {
    default void onOpen(V parentElement, Player player, T element){}
    default void whileOpen(V parentElement, Player player, T element){}

    static <V extends ActiveComponentRendered<V,?>, T extends HudElement.Rendered<?>> RenderedElementBehavior<V, T> createWhileOpen(TriConsumer<V, Player, T> function){
        return new RenderedElementBehavior<V, T>() {
            @Override
            public void whileOpen(V parentElement, Player player, T element) {
                function.accept(parentElement, player, element);
            }
        };
    }

    static <V extends ActiveComponentRendered<V,?>, T extends HudElement.Rendered<?>> RenderedElementBehavior<V, T> createOnOpen(TriConsumer<V, Player, T> function){
        return new RenderedElementBehavior<V, T>() {
            @Override
            public void onOpen(V parentElement, Player player, T element) {
                function.accept(parentElement, player, element);
            }
        };
    }
}

package de.verdox.mccreativelab.generator.resourcepack.types.rendered;

import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.HudElement;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;

public interface RenderedElementBehavior<V extends ActiveComponentRendered<V,?>, T extends HudElement.Rendered<?>> {
    default void onOpen(V parentElement, T element){}
    default void whileOpen(V parentElement, T element){}

    static <V extends ActiveComponentRendered<V,?>, T extends HudElement.Rendered<?>> RenderedElementBehavior<V, T> createWhileOpen(BiConsumer<V, T> function){
        return new RenderedElementBehavior<>() {
            @Override
            public void whileOpen(V parentElement, T element) {
                function.accept(parentElement, element);
            }
        };
    }

    static <V extends ActiveComponentRendered<V,?>, T extends HudElement.Rendered<?>> RenderedElementBehavior<V, T> createOnOpen(BiConsumer<V, T> function){
        return new RenderedElementBehavior<>() {
            @Override
            public void onOpen(V parentElement, T element) {
                function.accept(parentElement, element);
            }
        };
    }
}

package de.verdox.mccreativelab.ai.builder;

import de.verdox.mccreativelab.ai.behavior.AIBehavior;
import de.verdox.mccreativelab.ai.behavior.ControlledBehavior;
import de.verdox.mccreativelab.ai.behavior.CustomAIBehavior;
import de.verdox.mccreativelab.ai.behavior.OneShotBehavior;
import org.bukkit.entity.LivingEntity;

import java.util.function.Function;

public interface WeightedBehaviorsBuilder<E extends LivingEntity> {
    WeightedBehaviorsBuilder<E> withBehaviour(int priority, ControlledBehavior<? super E> aiBehavior);

    WeightedBehaviorsBuilder<E> withBehaviour(int priority, AIBehavior<? super E> customAiBehavior);

    WeightedBehaviorsBuilder<E> withBehaviour(int priority, CustomAIBehavior<? super E> customAiBehaviour);

    WeightedBehaviorsBuilder<E> withBehaviour(int priority, OneShotBehavior<? super E> aiBehavior);

    WeightedBehaviorsBuilder<E> withBehaviour(int priority, Function<BehaviorFactory, ControlledBehavior<? super E>> behaviourCreator);
}

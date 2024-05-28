package de.verdox.mccreativelab.ai.builder;

import de.verdox.mccreativelab.ai.MemoryStatus;
import de.verdox.mccreativelab.ai.behavior.AIBehavior;
import de.verdox.mccreativelab.ai.behavior.ControlledBehavior;
import de.verdox.mccreativelab.ai.behavior.CustomAIBehavior;
import de.verdox.mccreativelab.ai.behavior.OneShotBehavior;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ActivityBuilder<E extends LivingEntity> {

    ActivityBuilder<E> withRequiredMemory(MemoryKey<?> requiredMemoryKey, MemoryStatus memoryStatus);

    ActivityBuilder<E> withForgettingMemoriesWhenStopped(MemoryKey<?> forgettingMemoryKey);

    ActivityBuilder<E> withBehaviour(int priority, ControlledBehavior<? super E> controlledBehavior);

    ActivityBuilder<E> withBehaviour(int priority, AIBehavior<? super E> aiBehavior);

    ActivityBuilder<E> withBehaviour(int priority, CustomAIBehavior<? super E> customAIBehavior);

    ActivityBuilder<E> withBehaviour(int priority, OneShotBehavior<? super E> oneShotBehavior);

    ActivityBuilder<E> withBehaviour(int priority, Function<BehaviorFactory, ControlledBehavior<? super E>> behaviourCreator);
}

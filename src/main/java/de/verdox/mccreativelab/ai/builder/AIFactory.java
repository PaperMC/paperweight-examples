package de.verdox.mccreativelab.ai.builder;

import org.bukkit.EntityActivity;
import org.bukkit.entity.LivingEntity;

public interface AIFactory {
    BehaviorFactory getBehaviorFactory();
    GoalFactory getGoalFactory();
    <E extends LivingEntity> ActivityBuilder<E> createActivityBuilder(Class<? extends E> type, EntityActivity entityActivity);
    <E extends LivingEntity> WeightedBehaviorsBuilder<E> createWeightedBehaviorsBuilder(Class<? extends E> type);
}

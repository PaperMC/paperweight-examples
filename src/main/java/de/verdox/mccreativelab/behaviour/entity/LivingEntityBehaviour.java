package de.verdox.mccreativelab.behaviour.entity;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

public interface LivingEntityBehaviour<T extends LivingEntity> extends EntityBehaviour<T> {
    /**
     * Gets if an {@link LivingEntity} is sensitive to water
     * @param entity - The entity
     * @return - true if it is
     */
    default BehaviourResult.Bool isSensitiveToWater(T entity) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets the water-damage of an {@link LivingEntity} applied when it is sensitive to water
     * @param entity - The entity
     * @return - The water damage
     */
    default BehaviourResult.Object<Float> waterDamage(T entity) {
        return BehaviourResult.Object.DEFAULT_INSTANCE;
    }

    /**
     * Is called when an {@link LivingEntity} picks up an {@link Item}
     * @param entity - The Entity
     * @param item - The picked up Item
     * @return - nothing
     */
    default BehaviourResult.Void onItemPickup(T entity, Item item){
        return BehaviourResult.Void.DEFAULT_INSTANCE;
    }

    /**
     * Gets if an {@link LivingEntity} can disable shields
     * @param entity - The entity
     * @return - true if it can
     */
    default BehaviourResult.Bool canDisableShield(T entity) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets if an {@link LivingEntity} can attack an {@link Entity} with a particular {@link EntityType}
     * @param entity - The entity
     * @return - true if it can
     */
    default BehaviourResult.Bool canAttackType(T entity, EntityType entityType) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }
}

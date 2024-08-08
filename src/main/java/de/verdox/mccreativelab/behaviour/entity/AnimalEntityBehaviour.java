package de.verdox.mccreativelab.behaviour.entity;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;

public interface AnimalEntityBehaviour<T extends Animals> extends AgeableEntityBehaviour<T> {

    /**
     * Gets if an {@link Animals} can mate with another {@link Animals}
     * @param entity - The first animal
     * @param other - The other animal
     * @return - true if it can
     */
    default BehaviourResult.Bool canMate(T entity, Animals other) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets if an {@link org.bukkit.entity.Entity} would eat an {@link ItemStack}
     * @param entity - The Entity
     * @param stack - The ItemStack
     * @return - True if it can
     */
    default BehaviourResult.Bool isFood(T entity, ItemStack stack) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Called when an {@link Animals} breeds with another {@link Animals}
     * @param entity - The first animal
     * @param other - The second animal
     * @param child - The child
     * @return - nothing
     */
    default BehaviourResult.Callback onBreed(T entity, Animals other, Ageable child) {
        return BehaviourResult.Callback.DEFAULT_INSTANCE;
    }
}

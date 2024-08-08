package de.verdox.mccreativelab.behaviour.entity;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface MobBehaviour <T extends Mob> extends LivingEntityBehaviour<T> {
    /**
     * Is called when a {@link Player} interacts with a {@link Mob}
     * @param entity - The Entity
     * @param player - The Player
     * @param hand - The Interaction Hand
     * @return - An Interaction result
     */
    default BehaviourResult.Object<InteractionResult> mobInteract(T entity, Player player, EquipmentSlot hand){
        return BehaviourResult.Object.DEFAULT_INSTANCE;
    }

    /**
     * Gets if a {@link Mob} can fire projectile weapons
     * @param entity - The entity
     * @param weapon - The weapon material
     * @return - true if it can
     */
    default BehaviourResult.Bool canFireProjectileWeapon(T entity, Material weapon) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Called when an entity ate something
     * @param entity - The Entity
     * @return - nothing
     */
    default BehaviourResult.Void ate(T entity) {
        return BehaviourResult.Void.DEFAULT_INSTANCE;
    }

    /**
     * Gets if a {@link Mob} can hold an {@link ItemStack}
     * @param entity - The entity
     * @param stack - The ItemStack
     * @return - true if it can
     */
    default BehaviourResult.Bool canHoldItem(T entity, ItemStack stack) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets if a {@link Mob} can pickup an {@link ItemStack}
     * @param entity - The entity
     * @param stack - The ItemStack
     * @return - true if it can
     */
    default BehaviourResult.Bool wantsToPickUp(T entity, ItemStack stack) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets if a {@link Mob} should be removed when away a particular distance from players
     * @param entity - The entity
     * @param distanceSquared - The distance
     * @return - true if it should be removed
     */
    default BehaviourResult.Bool removeWhenFarAway(T entity, double distanceSquared) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets if a {@link Mob} can be leashed by a {@link Player}
     * @param entity - The entity
     * @param player - The Player
     * @return - true if it can
     */
    default BehaviourResult.Bool canBeLeashed(T entity, Player player) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }
}

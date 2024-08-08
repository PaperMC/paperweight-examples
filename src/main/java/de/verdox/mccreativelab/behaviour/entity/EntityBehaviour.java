package de.verdox.mccreativelab.behaviour.entity;

import de.verdox.mccreativelab.behaviour.Behaviour;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface EntityBehaviour<T extends Entity> extends Behaviour {
    /**
     * Gets if an {@link Entity} is fire immune
     * @param entity - The entity
     * @return - true if it is fire immune
     */
    default BehaviourResult.Bool fireImmune(T entity) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Gets if an {@link Entity} ignores a particular explosion
     * @param entity - The entity
     * @return - true if it ignores the explosion
     */
    default BehaviourResult.Bool ignoreExplosion(T entity, Location explosionLocation, float radius, @Nullable Entity source, boolean explosionHasFire, Map<Player, Vector> hitPlayers, List<Location> hitBlocks) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Called on every tick of an {@link Entity}
     * @param entity - The entity
     * @return - nothing
     */
    default BehaviourResult.Callback onTick(T entity){
        return BehaviourResult.Callback.DEFAULT_INSTANCE;
    }

    /**
     * Gets if an {@link Entity} can change dimensions
     * @param entity - The entity
     * @return - true if it can change dimensions
     */
    default BehaviourResult.Bool canChangeDimensions(T entity) {
        return BehaviourResult.Bool.DEFAULT_INSTANCE;
    }

    /**
     * Is called when an {@link Entity} is loaded
     * @param entity - The entity
     * @return - nothing
     */
    default BehaviourResult.Callback readAdditionalSaveData(T entity, PersistentDataContainer persistentDataContainer){
        return done();
    }

    /**
     * Is called when an {@link Entity} is saved
     * @param entity - The entity
     * @return - nothing
     */
    default BehaviourResult.Callback addAdditionalSaveData(T entity, PersistentDataContainer persistentDataContainer){
        return done();
    }
}

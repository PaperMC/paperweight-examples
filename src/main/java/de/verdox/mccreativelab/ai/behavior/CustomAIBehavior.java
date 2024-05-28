package de.verdox.mccreativelab.ai.behavior;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

/**
 * Represents a player based implementation of a behaviour
 * @param <E> - The entity type
 */
public interface CustomAIBehavior<E extends LivingEntity> extends AIBehavior<E> {

    void start(World world, E entity, long time);
    void tick(World world, E entity, long time);

    void stop(World world, E entity, long time);

    boolean canStillUse(World world, E entity, long time);

    boolean checkExtraStartConditions(World world, E entity, long time);
    Class<? extends E> getEntityType();

    enum Status {
        STOPPED,
        RUNNING,
    }
}

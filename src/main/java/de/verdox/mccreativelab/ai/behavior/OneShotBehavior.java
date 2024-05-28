package de.verdox.mccreativelab.ai.behavior;

import org.bukkit.entity.LivingEntity;

/**
 * A behaviour that is run in one try. It does not use ticks
 * @param <E> - The Entity Type
 */
public interface OneShotBehavior<E extends LivingEntity> extends ControlledBehavior<E> {
}

package de.verdox.mccreativelab.ai.behavior;

import de.verdox.mccreativelab.ai.MemoryStatus;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;

import java.util.Map;

/**
 * Represents an AI behaviour that is run over more than one tick
 * @param <E> - The entity type
 */
public interface AIBehavior<E extends LivingEntity> extends ControlledBehavior<E> {
    int getMinDuration();

    int getMaxDuration();

    Map<MemoryKey<?>, MemoryStatus> getRequiredMemoryStates();
}

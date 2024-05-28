package de.verdox.mccreativelab.ai.behavior;

import de.verdox.mccreativelab.ai.MemoryStatus;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;

import java.util.HashMap;
import java.util.Map;

public abstract class PaperCustomAIBehavior<E extends LivingEntity> implements CustomAIBehavior<E> {
    private final Class<? extends E> entityType;
    private final Map<MemoryKey<?>, MemoryStatus> requiredMemoryState;
    private final int minRunTime;
    private final int maxRunTime;

    public PaperCustomAIBehavior(Class<? extends E> entityType, Map<MemoryKey<?>, MemoryStatus> requiredMemoryState, int minRunTime, int maxRunTime){
        this.entityType = entityType;
        this.requiredMemoryState = requiredMemoryState;
        this.minRunTime = minRunTime;
        this.maxRunTime = maxRunTime;
    }

    public PaperCustomAIBehavior(Class<? extends E> entityType, Map<MemoryKey<?>, MemoryStatus> requiredMemoryState) {
        this(entityType, requiredMemoryState, 60);
    }

    public PaperCustomAIBehavior(Class<? extends E> entityType, Map<MemoryKey<?>, MemoryStatus> requiredMemoryState, int runTime) {
        this(entityType, requiredMemoryState, runTime, runTime);
    }

    @Override
    public final int getMinDuration() {
        return minRunTime;
    }

    @Override
    public final int getMaxDuration() {
        return maxRunTime;
    }

    @Override
    public final Map<MemoryKey<?>, MemoryStatus> getRequiredMemoryStates() {
        return new HashMap<>(requiredMemoryState);
    }

    @Override
    public final Class<? extends E> getEntityType() {
        return entityType;
    }
}

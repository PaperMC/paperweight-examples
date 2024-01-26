package de.verdox.mccreativelab.entity.ai;

import de.verdox.mccreativelab.ai.MemoryStatus;
import de.verdox.mccreativelab.ai.behavior.PaperCustomAIBehavior;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;

import java.util.Map;

public class AsyncBehavior<E extends LivingEntity> extends PaperCustomAIBehavior<E> {
    public AsyncBehavior(Class<? extends E> entityType, Map<MemoryKey<?>, MemoryStatus> requiredMemoryState, int minRunTime, int maxRunTime) {
        super(entityType, requiredMemoryState, minRunTime, maxRunTime);
    }

    public AsyncBehavior(Class<? extends E> entityType, Map<MemoryKey<?>, MemoryStatus> requiredMemoryState) {
        super(entityType, requiredMemoryState);
    }

    public AsyncBehavior(Class<? extends E> entityType, Map<MemoryKey<?>, MemoryStatus> requiredMemoryState, int runTime) {
        super(entityType, requiredMemoryState, runTime);
    }

    @Override
    public void start(World world, LivingEntity entity, long time) {

    }

    @Override
    public void tick(World world, LivingEntity entity, long time) {

    }

    @Override
    public void stop(World world, LivingEntity entity, long time) {

    }

    @Override
    public boolean canStillUse(World world, LivingEntity entity, long time) {
        return false;
    }

    @Override
    public boolean checkExtraStartConditions(World world, LivingEntity entity, long time) {
        return false;
    }
}

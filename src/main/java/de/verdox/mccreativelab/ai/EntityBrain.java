package de.verdox.mccreativelab.ai;

import de.verdox.mccreativelab.ai.builder.ActivityBuilder;
import de.verdox.mccreativelab.ai.builder.BehaviorFactory;
import org.bukkit.EntityActivity;
import org.bukkit.EntitySchedule;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public interface EntityBrain<E extends LivingEntity> {
    BehaviorFactory getBehaviorFactory();

    ActivityBuilder<E> createActivityBuilder(EntityActivity entityActivity);

    void tick(World world, E entity);

    <U> void eraseMemory(MemoryKey<U> memoryKey);

    <U> void setMemory(MemoryKey<U> type, @Nullable U value);

    <U> void setMemoryWithExpiry(MemoryKey<U> type, U value, long expiry);

    <U> Optional<U> getMemory(MemoryKey<U> type);

    <U> long getTimeUntilExpiry(MemoryKey<U> type);

    <U> boolean isMemoryValue(MemoryKey<U> type, U value);

    boolean checkMemory(MemoryKey<?> type, MemoryStatus state);

    EntitySchedule getSchedule();

    void setSchedule(EntitySchedule schedule);

    void setCoreActivities(Set<EntityActivity> coreActivities);

    void useDefaultActivity();

    Optional<EntityActivity> getActiveNonCoreActivity();

    void setActiveActivityIfPossible(EntityActivity activity);

    void updateActivityFromSchedule(long timeOfDay, long time);

    void setActiveActivityToFirstValid(List<EntityActivity> activities);

    void setDefaultActivity(EntityActivity activity);

    void addActivity(ActivityBuilder<E> activityBuilder, boolean replaceCompleteActivity, boolean replaceActivityRequirements, boolean replaceForgettingMemories);

    default void addActivity(ActivityBuilder<E> activityBuilder, boolean replaceCompleteActivity, boolean replaceActivityRequirements) {
        addActivity(activityBuilder, replaceCompleteActivity, replaceActivityRequirements, false);
    }

    default void addActivity(ActivityBuilder<E> activityBuilder, boolean replaceCompleteActivity) {
        addActivity(activityBuilder, replaceCompleteActivity, false, false);
    }

    default void addActivity(ActivityBuilder<E> activityBuilder) {
        addActivity(activityBuilder, true, false, false);
    }

    void addActivity(EntityActivity activity, Consumer<ActivityBuilder<E>> activityBuilder, boolean replaceCompleteActivity, boolean replaceActivityRequirements, boolean replaceForgettingMemories);

    default void addActivity(EntityActivity activity, Consumer<ActivityBuilder<E>> activityBuilder, boolean replaceCompleteActivity, boolean replaceActivityRequirements) {
        addActivity(activity, activityBuilder, replaceCompleteActivity, replaceActivityRequirements, false);
    }

    default void addActivity(EntityActivity activity, Consumer<ActivityBuilder<E>> activityBuilder, boolean replaceCompleteActivity) {
        addActivity(activity, activityBuilder, replaceCompleteActivity, false, false);
    }

    default void addActivity(EntityActivity activity, Consumer<ActivityBuilder<E>> activityBuilder) {
        addActivity(activity, activityBuilder, true, false, false);
    }

    boolean isActive(EntityActivity activity);

    EntityBrain<E> copyWithoutBehaviours();

    void stopAll(World world, E entity);

}

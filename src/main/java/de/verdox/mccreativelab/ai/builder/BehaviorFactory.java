package de.verdox.mccreativelab.ai.builder;

import com.destroystokyo.paper.entity.RangedEntity;
import de.verdox.mccreativelab.ai.behavior.AIBehavior;
import de.verdox.mccreativelab.ai.behavior.ControlledBehavior;
import de.verdox.mccreativelab.ai.behavior.OneShotBehavior;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;

import java.util.List;
import java.util.function.*;

public interface BehaviorFactory {

    OneShotBehavior<Mob> acquirePOI(Predicate<PoiType> poiTypePredicate, MemoryKey<Location> poiPosModule, MemoryKey<Location> potentialPoiPosModule, boolean onlyRunIfChild, boolean entityEvent);
    default OneShotBehavior<Mob> acquirePOI(Predicate<PoiType> poiTypePredicate, MemoryKey<Location> poiPosModule, boolean onlyRunIfChild, boolean entityEvent){
        return acquirePOI(poiTypePredicate, poiPosModule, poiPosModule, onlyRunIfChild, entityEvent);
    }

    AIBehavior<Animals> animalMakeLove(EntityType targetType, float speed);

    AIBehavior<Mob> animalPanic(float speed, Predicate<Mob> panicPredicate);

    OneShotBehavior<Villager> assignProfessionFromJobSite();

    OneShotBehavior<Ageable> babyFollowAdult(int minRange, int maxRange, float speed);

    OneShotBehavior<Mob> backupIfTooClose(int distance, float forwardMovement);

    OneShotBehavior<LivingEntity> becomePassiveIfMemoryPresent(MemoryKey<?> requiredMemory, int duration);

    AIBehavior<Villager> celebrateVillagersSurviveRaid(int minRuntime, int maxRunTime);

    <T> OneShotBehavior<LivingEntity> copyMemoryWithExpiry(Predicate<LivingEntity> runPredicate, MemoryKey<T> sourceType, MemoryKey<T> targetType, int minExpiry, int maxExpiry);

    AIBehavior<LivingEntity> countDownCooldownTicks(MemoryKey<Integer> moduleType);

    AIBehavior<Frog> croak();

    AIBehavior<RangedEntity> crossbowAttack();

    OneShotBehavior<LivingEntity> dismountOrSkipMounting(int range, BiPredicate<LivingEntity, Entity> alternativeRideCondition);

    ControlledBehavior<LivingEntity> doNothing(int minRuntime, int maxRunTime);

    OneShotBehavior<LivingEntity> eraseMemoryIf(Predicate<LivingEntity> condition, MemoryKey<?> memory);

    AIBehavior<Mob> followTemptation(Function<LivingEntity, Float> speed, Function<LivingEntity, Double> stopDistanceGetter);

    AIBehavior<Villager> giveGiftToHero(int delay);

    OneShotBehavior<Villager> goToClosestVillage(float speed, int completionRange);

    AIBehavior<Villager> goToPotentialJobSite(float speed);

    OneShotBehavior<Mob> goToTargetLocation(MemoryKey<Location> posModule, int completionRange, float speed);

    OneShotBehavior<LivingEntity> goToWantedItem(Predicate<LivingEntity> startCondition, float speed, boolean requiresWalkTarget, int radius);

    AIBehavior<Villager> harvestFarmland();

    OneShotBehavior<Mob> insideBrownianWalk(float speed);

    OneShotBehavior<LivingEntity> interactWith(EntityType entityType, int maxDistance, Predicate<LivingEntity> interactorPredicate, Predicate<Entity> targetPredicate, MemoryKey<Entity> memoryKey, float speed, int completionRange);

    default OneShotBehavior<LivingEntity> interactWith(EntityType entityType, int maxDistance, MemoryKey<Entity> memoryKey, float speed, int completionRange) {
        return interactWith(entityType, maxDistance, livingEntity -> true, entity -> true, memoryKey, speed, completionRange);
    }

    OneShotBehavior<LivingEntity> interactWithDoor();

    AIBehavior<Mob> jumpOnBed(float walkSpeed);

    OneShotBehavior<LivingEntity> locateHidingPlace(int maxDistance, float walkSpeed, int preferredDistance);

    AIBehavior<Mob> longJumpMidJump(int minCooldownRange, int maxCooldownRange, Sound.Type soundType);

    AIBehavior<Mob> longJumpToPreferredBlock(int minCooldownRange, int maxCooldownRange, int verticalRange, int horizontalRange, float maxRange, Sound.Type jumpSound, Tag<Material> favoredBlocks, float biasChance, BiPredicate<Mob, Location> jumpToPredicate);

    AIBehavior<Mob> longJumpToRandomBlock(int minCooldownRange, int maxCooldownRange, int verticalRange, int horizontalRange, float maxRange, Sound.Type jumpSound);

    AIBehavior<Villager> lookAndFollowTradingPlayerSink(float speed);

    AIBehavior<Mob> lookAtTargetSink(int minRunTime, int maxRunTime);

    OneShotBehavior<Mob> meleeAttack(int cooldown);

    OneShotBehavior<LivingEntity> mount(float speed);

    OneShotBehavior<LivingEntity> moveToSkySeeingSpot(float speed);

    AIBehavior<Mob> moveToTargetSink(int minRunTime, int maxRunTime);

    OneShotBehavior<Mob> playTagWithOtherKids();

    OneShotBehavior<Villager> poiCompetitorScan();

    AIBehavior<Mob> randomLookAround(int minCooldownRange, int maxCooldownRange, float maxYaw, float minPitch, float maxPitch);

    OneShotBehavior<Mob> randomStroll(float speed, boolean strollInsideWater);

    OneShotBehavior<Mob> randomStroll(float speed, int horizontalRadius, int verticalRadius);

    OneShotBehavior<Mob> randomFlyStroll(float speed);

    OneShotBehavior<Mob> randomSwimStroll(float speed);

    OneShotBehavior<Villager> reactToBell();

    OneShotBehavior<Villager> resetProfession();

    OneShotBehavior<Villager> resetRaidStatus();

    OneShotBehavior<Villager> ringBell();

    OneShotBehavior<Villager> setCloseHomeAsWalkTarget(float speed);

    OneShotBehavior<LivingEntity> setEntityLookTarget(Predicate<LivingEntity> predicate, float maxDistance);

    default OneShotBehavior<LivingEntity> setEntityLookTarget(EntityType type, float maxDistance) {
        return setEntityLookTarget(livingEntity -> livingEntity.getType() == type, maxDistance);
    }

    OneShotBehavior<Villager> setHiddenState(int maxHiddenSeconds, int distance);

    OneShotBehavior<LivingEntity> setLookAndInteract(EntityType entityType, int maxDistance);

    OneShotBehavior<LivingEntity> setRaidStatus();

    OneShotBehavior<Mob> setWalkTargetAwayFromPos(MemoryKey<Location> memoryKey, float speed, int range, boolean requiresWalkTarget);

    OneShotBehavior<Mob> setWalkTargetAwayFromEntity(MemoryKey<Entity> memoryKey, float speed, int range, boolean requiresWalkTarget);

    OneShotBehavior<Mob> setWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> speed);

    OneShotBehavior<Mob> setWalkTargetFromBlockMemory(MemoryKey<Location> blockMemoryKey, float speed, int completionRange, int maxDistance, int maxRunTime);

    OneShotBehavior<Mob> setWalkTargetFromLookTarget(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> speed, int completionRange);

    default OneShotBehavior<Mob> setWalkTargetFromLookTarget(float speed, int completionRange) {
        return setWalkTargetFromLookTarget(livingEntity -> true, livingEntity -> speed, completionRange);
    }

    AIBehavior<Villager> showTradesToPlayer(int minRunTime, int maxRunTime);

    AIBehavior<Villager> sleepInBed();

    OneShotBehavior<Villager> socializeAtBell();

    OneShotBehavior<Mob> startAttacking(Function<Mob, LivingEntity> targetGetter);

    OneShotBehavior<Piglin> startCelebratingIfTargetDead(int celebrationDuration, BiPredicate<LivingEntity, LivingEntity> predicate);

    AIBehavior<Mob> stopAttackingIfTargetInvalid(Predicate<LivingEntity> alternativeCondition, BiConsumer<Mob, LivingEntity> forgetCallback, boolean shouldForgetIfTargetUnreachable);

    OneShotBehavior<Mob> stopBeingAngryIfTargetDead();

    OneShotBehavior<Mob> strollAroundPoi(MemoryKey<Location> posMemory, float walkSpeed, int maxDistance);

    OneShotBehavior<Mob> strollToPoi(MemoryKey<Location> posMemory, float walkSpeed, int completionRange, int maxDistance);

    OneShotBehavior<Mob> strollToPoiList(MemoryKey<List<Location>> posMemory, float walkSpeed, int completionRange, int primaryPositionActivationDistance, MemoryKey<Location> primaryPosition);

    AIBehavior<Mob> swim(float chance);

    AIBehavior<Villager> tradeWithVillager();

    OneShotBehavior<Mob> tryFindLand(int range, float speed);

    OneShotBehavior<Mob> tryFindLandNearWater(int range, float speed);

    OneShotBehavior<Mob> tryFindWater(int range, float speed);

    OneShotBehavior<Frog> tryLaySpawnOnWaterNearLand(Material spawn);

    OneShotBehavior<Mob> updateActivityFromSchedule();

    AIBehavior<Villager> useBonemeal();

    OneShotBehavior<Villager> validateNearbyPoi(Predicate<PoiType> poiTypePredicate, MemoryKey<Location> poiMemory);

    OneShotBehavior<Villager> villageBoundRandomStroll(float walkSpeed, int horizontalRange, int verticalRange);

    default OneShotBehavior<Villager> villageBoundRandomStroll(float walkSpeed) {
        return villageBoundRandomStroll(walkSpeed, 10, 7);
    }

    OneShotBehavior<Villager> villagerCalmDown();

    AIBehavior<Villager> villagerMakeLove();

    AIBehavior<Villager> villagerPanicTrigger();

    OneShotBehavior<Villager> wakeUp();

    AIBehavior<Villager> workAtComposter();

    AIBehavior<Villager> workAtPoi();

    OneShotBehavior<Villager> yieldJobSite(float speed);

    // Logic
    <T extends LivingEntity> ControlledBehavior<T> gateBehaviour(Consumer<ActivityBuilder<T>> activityBuilder, GateOrderPolicy gateOrderPolicy, GateRunningPolicy gateRunningPolicy);

    default <T extends LivingEntity> ControlledBehavior<T> runOne(Consumer<ActivityBuilder<T>> activityBuilder) {
        return gateBehaviour(activityBuilder, GateOrderPolicy.SHUFFLED, GateRunningPolicy.RUN_ONE);
    }

    <T extends LivingEntity> OneShotBehavior<T> triggerGate(Consumer<WeightedBehaviorsBuilder<T>> weightedTasks, GateOrderPolicy order, GateRunningPolicy runMode);

    default <T extends LivingEntity> OneShotBehavior<T> triggerOneShuffled(Consumer<WeightedBehaviorsBuilder<T>> weightedTasks) {
        return triggerGate(weightedTasks, GateOrderPolicy.SHUFFLED, GateRunningPolicy.RUN_ONE);
    }

    <T extends LivingEntity> OneShotBehavior<T> sequence(OneShotBehavior<T> predicateBehavior, OneShotBehavior<T> task);

    <T extends LivingEntity> OneShotBehavior<T> triggerIf(Predicate<T> predicate, OneShotBehavior<T> task);

    <T extends LivingEntity> OneShotBehavior<T> triggerIf(Predicate<T> predicate);

    <T extends LivingEntity> OneShotBehavior<T> triggerIf(BiPredicate<World, T> predicate);

    enum GateOrderPolicy {
        ORDERED,
        SHUFFLED
    }

    enum GateRunningPolicy {
        RUN_ONE,
        TRY_ALL
    }
}

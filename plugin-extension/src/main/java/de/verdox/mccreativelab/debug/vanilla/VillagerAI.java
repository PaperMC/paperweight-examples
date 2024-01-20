package de.verdox.mccreativelab.debug.vanilla;

import de.verdox.mccreativelab.ai.MemoryStatus;
import de.verdox.mccreativelab.ai.behavior.AIBehavior;
import de.verdox.mccreativelab.ai.behavior.ControlledBehavior;
import de.verdox.mccreativelab.ai.builder.ActivityBuilder;
import de.verdox.mccreativelab.ai.builder.BehaviorFactory;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;

public final class VillagerAI {
    public static ActivityBuilder<Villager> corePackageBuilder(PoiType heldJobSite, PoiType acquirableJobSite, float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.CORE)
                     .withBehaviour(0, behaviorFactory -> behaviorFactory.swim(0.8f))
                     .withBehaviour(0, BehaviorFactory::interactWithDoor)
                     .withBehaviour(0, behaviorFactory -> behaviorFactory.lookAtTargetSink(45, 90))
                     .withBehaviour(0, BehaviorFactory::villagerPanicTrigger)
                     .withBehaviour(0, BehaviorFactory::wakeUp)
                     .withBehaviour(0, BehaviorFactory::reactToBell)
                     .withBehaviour(0, BehaviorFactory::setRaidStatus)
                     .withBehaviour(0, behaviorFactory -> behaviorFactory.validateNearbyPoi(poiType -> is(poiType, heldJobSite), MemoryKey.JOB_SITE))
                     .withBehaviour(0, behaviorFactory -> behaviorFactory.validateNearbyPoi(poiType -> is(poiType, acquirableJobSite), MemoryKey.POTENTIAL_JOB_SITE))
                     .withBehaviour(1, behaviorFactory -> behaviorFactory.moveToTargetSink(150, 250))
                     .withBehaviour(2, BehaviorFactory::poiCompetitorScan)
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.lookAndFollowTradingPlayerSink(speed))
                     .withBehaviour(5, behaviorFactory -> behaviorFactory.goToWantedItem(livingEntity -> !livingEntity.isSleeping(), speed, false, 4))
                     .withBehaviour(6, behaviorFactory -> behaviorFactory.acquirePOI(poiType -> poiType.getKey()
                                                                                                       .equals(acquirableJobSite.getKey()), MemoryKey.JOB_SITE, MemoryKey.POTENTIAL_JOB_SITE, true, false))
                     .withBehaviour(7, behaviorFactory -> behaviorFactory.goToPotentialJobSite(speed))
                     .withBehaviour(8, behaviorFactory -> behaviorFactory.yieldJobSite(speed))
                     .withBehaviour(8, behaviorFactory -> behaviorFactory.acquirePOI(poiType -> is(poiType, PoiType.HOME), MemoryKey.HOME, false, true))
                     .withBehaviour(8, behaviorFactory -> behaviorFactory.acquirePOI(poiType -> is(poiType, PoiType.MEETING), MemoryKey.MEETING_POINT, true, true))
                     .withBehaviour(10, BehaviorFactory::assignProfessionFromJobSite)
                     .withBehaviour(10, BehaviorFactory::resetProfession);
    }

    public static ActivityBuilder<Villager> workPackageBuilder(Villager.Profession profession, float speed) {
        AIBehavior<Villager> workAtPoi;
        if (profession.equals(Villager.Profession.FARMER))
            workAtPoi = Bukkit.getAIFactory().getBehaviorFactory().workAtComposter();
        else
            workAtPoi = Bukkit.getAIFactory().getBehaviorFactory().workAtPoi();

        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.WORK)
                     .withBehaviour(2, behaviorFactory -> behaviorFactory.setWalkTargetFromBlockMemory(MemoryKey.JOB_SITE, speed, 9, 100, 1200))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.giveGiftToHero(100))
                     .withBehaviour(5, behaviorFactory ->
                         behaviorFactory.runOne(builder -> builder
                             .withBehaviour(2, behaviorFactory.strollAroundPoi(MemoryKey.JOB_SITE, speed, 4))
                             .withBehaviour(5, behaviorFactory.strollToPoi(MemoryKey.JOB_SITE, speed, 1, 10))
                             .withBehaviour(5, behaviorFactory.strollToPoiList(MemoryKey.SECONDARY_JOB_SITE, speed, 1, 10, MemoryKey.JOB_SITE))
                             .withBehaviour(profession.getKey()
                                                      .equals(Villager.Profession.FARMER.getKey()) ? 2 : 5, behaviorFactory.harvestFarmland())
                             .withBehaviour(profession.getKey()
                                                      .equals(Villager.Profession.FARMER.getKey()) ? 4 : 7, behaviorFactory.useBonemeal())
                             .withBehaviour(7, workAtPoi)
                             .withBehaviour(10, behaviorFactory.showTradesToPlayer(400, 1600))
                         )
                     )
                     .withBehaviour(10, behaviorFactory -> behaviorFactory.showTradesToPlayer(400, 1600))
                     .withBehaviour(10, behaviorFactory -> behaviorFactory.setLookAndInteract(EntityType.PLAYER, 4))
                     .withBehaviour(99, BehaviorFactory::updateActivityFromSchedule);
    }

    public static ActivityBuilder<Villager> playPackageBuilder(float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.PLAY)
                     .withBehaviour(0, behaviorFactory -> behaviorFactory.moveToTargetSink(80, 120))
                     .withBehaviour(5, fullLookBehaviour())
                     .withBehaviour(5, BehaviorFactory::playTagWithOtherKids)
                     .withBehaviour(5, behaviorFactory -> behaviorFactory.runOne(activityBuilder -> activityBuilder
                         .withRequiredMemory(MemoryKey.VISIBLE_VILLAGER_BABIES, MemoryStatus.VALUE_ABSENT)
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.jumpOnBed(speed))
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.doNothing(20, 40))
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.interactWith(EntityType.VILLAGER, 8, MemoryKey.INTERACTION_TARGET, speed, 2))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.interactWith(EntityType.CAT, 8, MemoryKey.INTERACTION_TARGET, speed, 2))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.villageBoundRandomStroll(speed))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.setWalkTargetFromLookTarget(speed, 2))

                     ))
                     .withBehaviour(99, BehaviorFactory::updateActivityFromSchedule)
            ;
    }

    public static ActivityBuilder<Villager> restPackageBuilder(float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.REST)
                     .withBehaviour(2, behaviorFactory -> behaviorFactory.setWalkTargetFromBlockMemory(MemoryKey.HOME, speed, 1, 150, 1200))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.validateNearbyPoi(poiType -> is(poiType, PoiType.HOME), MemoryKey.HOME))
                     .withBehaviour(3, BehaviorFactory::sleepInBed)
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.runOne(activityBuilder -> activityBuilder
                         .withRequiredMemory(MemoryKey.HOME, MemoryStatus.VALUE_ABSENT)
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.setCloseHomeAsWalkTarget(speed))
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.doNothing(20, 40))
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.goToClosestVillage(speed, 1))
                         .withBehaviour(4, behaviorFactory1 -> behaviorFactory1.insideBrownianWalk(speed))
                     ))
            ;
    }

    public static ActivityBuilder<Villager> meetPackageBuilder(float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.MEET)
                     .withBehaviour(2, behaviorFactory -> behaviorFactory.triggerOneShuffled(weightedBehaviorsBuilder -> weightedBehaviorsBuilder
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.strollAroundPoi(MemoryKey.MEETING_POINT, speed, 40))
                         .withBehaviour(2, BehaviorFactory::socializeAtBell)))
                     .withBehaviour(10, behaviorFactory -> behaviorFactory.showTradesToPlayer(400, 1600))
                     .withBehaviour(10, behaviorFactory -> behaviorFactory.setLookAndInteract(EntityType.PLAYER, 4))
                     .withBehaviour(2, behaviorFactory -> behaviorFactory.setWalkTargetFromBlockMemory(MemoryKey.MEETING_POINT, speed, 6, 100, 200))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.giveGiftToHero(100))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.validateNearbyPoi(poiType -> is(poiType, PoiType.MEETING), MemoryKey.MEETING_POINT))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.gateBehaviour(activityBuilder -> activityBuilder
                         .withForgettingMemoriesWhenStopped(MemoryKey.INTERACTION_TARGET)
                         .withBehaviour(1, BehaviorFactory::tradeWithVillager), BehaviorFactory.GateOrderPolicy.ORDERED, BehaviorFactory.GateRunningPolicy.RUN_ONE))
                     .withBehaviour(5, fullLookBehaviour())
                     .withBehaviour(99, BehaviorFactory::updateActivityFromSchedule);
    }

    public static ActivityBuilder<Villager> idlePackageBuilder(float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.IDLE)
                     .withBehaviour(2, behaviorFactory -> behaviorFactory.runOne(activityBuilder -> activityBuilder
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.interactWith(EntityType.VILLAGER, 8, MemoryKey.INTERACTION_TARGET, speed, 2))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.interactWith(EntityType.VILLAGER, 8, livingEntity -> livingEntity instanceof Breedable breedable && breedable.canBreed(), entity -> entity instanceof Breedable breedable && breedable.canBreed(), MemoryKey.BREED_TARGET, speed, 2))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.interactWith(EntityType.CAT, 8, MemoryKey.INTERACTION_TARGET, speed, 2))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.villageBoundRandomStroll(speed))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.setWalkTargetFromLookTarget(speed, 2))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.jumpOnBed(speed))
                         .withBehaviour(1, behaviorFactory1 -> behaviorFactory1.doNothing(30, 60))
                     ))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.giveGiftToHero(100))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.setLookAndInteract(EntityType.PLAYER, 4))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.showTradesToPlayer(400, 1600))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.gateBehaviour(activityBuilder -> activityBuilder
                         .withForgettingMemoriesWhenStopped(MemoryKey.INTERACTION_TARGET)
                         .withBehaviour(1, BehaviorFactory::tradeWithVillager), BehaviorFactory.GateOrderPolicy.ORDERED, BehaviorFactory.GateRunningPolicy.RUN_ONE))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.gateBehaviour(activityBuilder -> activityBuilder
                         .withForgettingMemoriesWhenStopped(MemoryKey.BREED_TARGET)
                         .withBehaviour(1, BehaviorFactory::villagerMakeLove), BehaviorFactory.GateOrderPolicy.ORDERED, BehaviorFactory.GateRunningPolicy.RUN_ONE))
                     .withBehaviour(5, fullLookBehaviour())
                     .withBehaviour(99, BehaviorFactory::updateActivityFromSchedule)
            ;
    }

    public static ActivityBuilder<Villager> panicPackageBuilder(float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.PANIC)
                     .withBehaviour(0, BehaviorFactory::villagerCalmDown)
                     .withBehaviour(1, behaviorFactory -> behaviorFactory.setWalkTargetAwayFromEntity(MemoryKey.NEAREST_HOSTILE, speed * 1.5f, 6, false))
                     .withBehaviour(1, behaviorFactory -> behaviorFactory.setWalkTargetAwayFromEntity(MemoryKey.HURT_BY_ENTITY, speed * 1.5f, 6, false))
                     .withBehaviour(3, behaviorFactory -> behaviorFactory.villageBoundRandomStroll(speed * 1.5f, 2, 2))
                     .withBehaviour(5, fullLookBehaviour())
            ;
    }

    public static ActivityBuilder<Villager> prePreRaidPackageBuilder(float speed) {
        return Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.PRE_RAID)
                     .withBehaviour(0, BehaviorFactory::ringBell)
                     .withBehaviour(0, behaviorFactory -> behaviorFactory.triggerOneShuffled(weightedBehaviorsBuilder -> weightedBehaviorsBuilder
                         .withBehaviour(6, behaviorFactory1 -> behaviorFactory1.setWalkTargetFromBlockMemory(MemoryKey.MEETING_POINT, speed * 1.5f, 2, 150, 200))
                         .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.villageBoundRandomStroll(speed * 1.5f))
                     ))
                     .withBehaviour(5, fullLookBehaviour())
                     .withBehaviour(99, BehaviorFactory::resetRaidStatus)
            ;
    }

    public static ActivityBuilder<Villager> raidPackageBuilder(float speed){
        ActivityBuilder<Villager> activityBuilder = Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.RAID);
        BehaviorFactory behaviorFactory = Bukkit.getAIFactory().getBehaviorFactory();

        return activityBuilder
            .withBehaviour(0, behaviorFactory.sequence(behaviorFactory.triggerIf(VillagerAI::raidExistsAndNotVictory),
                behaviorFactory.triggerOneShuffled(weightedBehaviorsBuilder -> weightedBehaviorsBuilder
                    .withBehaviour(5, behaviorFactory.moveToSkySeeingSpot(speed))
                    .withBehaviour(5, behaviorFactory.villageBoundRandomStroll(speed * 1.1f)))))
            .withBehaviour(0, behaviorFactory.celebrateVillagersSurviveRaid(600,600))
            .withBehaviour(2, behaviorFactory.sequence(behaviorFactory.triggerIf(VillagerAI::raidExistsAndActive), behaviorFactory.locateHidingPlace(24, speed * 1.4f, 1)))
            .withBehaviour(5, fullLookBehaviour())
            .withBehaviour(99, behaviorFactory.resetRaidStatus())
            ;
    }

    public static ActivityBuilder<Villager> hidePackageBuilder(float speed){
        ActivityBuilder<Villager> activityBuilder = Bukkit.getAIFactory().createActivityBuilder(Villager.class, EntityActivity.HIDE);
        BehaviorFactory behaviorFactory = Bukkit.getAIFactory().getBehaviorFactory();

        return activityBuilder.withBehaviour(0, behaviorFactory.setHiddenState(15, 3))
            .withBehaviour(1, behaviorFactory.locateHidingPlace(32, speed * 1.25f, 2))
            .withBehaviour(5, fullLookBehaviour())
            ;
    }

    private static boolean raidExistsAndActive (World world, LivingEntity livingEntity){
        Raid raid = world.locateNearestRaid(livingEntity.getLocation(),9216);
        return raid != null && raid.getStatus().equals(Raid.RaidStatus.ONGOING);
    }

    private static boolean raidExistsAndNotVictory (World world, LivingEntity livingEntity){
        Raid raid = world.locateNearestRaid(livingEntity.getLocation(),9216);
        return raid != null && raid.getStatus().equals(Raid.RaidStatus.VICTORY);
    }

    public static <T extends LivingEntity> ControlledBehavior<T> fullLookBehaviour() {
        return Bukkit.getAIFactory().getBehaviorFactory().runOne(activityBuilder -> activityBuilder
            .withBehaviour(8, behaviorFactory1 -> behaviorFactory1.setEntityLookTarget(EntityType.CAT, 8))
            .withBehaviour(2, behaviorFactory1 -> behaviorFactory1.setEntityLookTarget(livingEntity -> true, 8)));
    }

    private static boolean is(Keyed keyed1, Keyed keyed2) {
        return keyed1.getKey().equals(keyed2.getKey());
    }
}

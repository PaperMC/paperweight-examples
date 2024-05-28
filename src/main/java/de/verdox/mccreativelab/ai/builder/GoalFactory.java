package de.verdox.mccreativelab.ai.builder;

import com.destroystokyo.paper.entity.RangedEntity;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import java.util.function.Function;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public interface GoalFactory {
    VanillaGoal<Mob> avoidEntity(Mob mob, EntityType entityType, Predicate<LivingEntity> extraInclusionPredicate, float distance, double slowSpeed, double fastSpeed, Predicate<LivingEntity> inclusionSelector);

    VanillaGoal<Wolf> beg(Wolf wolf, float begDistance);

    VanillaGoal<Mob> breakDoor(Mob mob, Predicate<Difficulty> difficultySufficientPredicate);

    VanillaGoal<Mob> breathAir(Mob mob);

    VanillaGoal<Animals> breed(Animals animal, double speed, EntityType breedTarget);

    VanillaGoal<Cat> catLieOnBed(Cat cat, double speed, int range);

    VanillaGoal<Cat> catSitOnBlock(Cat cat, double speed);

    VanillaGoal<Mob> climbOnTopOfPowderSnow(Mob mob, World world);

    VanillaGoal<Dolphin> dolphinJump(Dolphin dolphin, int chance);

    VanillaGoal<Mob> eatBlock(Mob mob, Function<VanillaRandomSource, Boolean> chanceToEat, Predicate<BlockState> predicate);

    VanillaGoal<Mob> fleeSun(Mob mob, double speed);

    VanillaGoal<Mob> floatOnWater(Mob mob);

    VanillaGoal<Mob> followBoat(Mob mob);

    VanillaGoal<Fish> followFlockLeader(Fish fish);

    VanillaGoal<Fish> followMob(Mob mob, double speed, float minDistance, float maxDistance, Predicate<Mob> followPredicate);

    VanillaGoal<Tameable> followOwner(Tameable tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed);

    VanillaGoal<Animals> followParent(Animals animal, double speed);

    VanillaGoal<Mob> randomStrollInVillage(Mob mob, double speed);

    VanillaGoal<Mob> interact(Mob mob, EntityType entityType, float range, float chance);

    VanillaGoal<Parrot> landOnOwnersShoulders(Parrot parrot);

    VanillaGoal<Mob> leapAtTarget(Mob mob, float velocity);

    VanillaGoal<Llama> llamaFollowCaravan(Llama llama, double speed);

    VanillaGoal<Mob> lookAtMob(Mob mob, EntityType targetType, float range, float change, boolean lookForward);

    VanillaGoal<AbstractVillager> lookAtTradingPlayer(AbstractVillager abstractVillager);

    VanillaGoal<Mob> meleeAttack(Mob mob, double speed, boolean pauseWhenMobIdle);

    VanillaGoal<Mob> moveBackToVillage(Mob mob, double speed, boolean canDespawn);

    VanillaGoal<Mob> moveThroughVillage(Mob mob, double speed, boolean requiresNighttime, int distance, BooleanSupplier doorPassingThroughGetter);

    VanillaGoal<Mob> moveTowardsTarget(Mob mob, double speed, float maxDistance);

    VanillaGoal<Mob> ocelotAttack(Mob mob);

    VanillaGoal<IronGolem> offerFlower(IronGolem ironGolem);

    VanillaGoal<Mob> openDoor(Mob mob, boolean delayedClose);

    VanillaGoal<Mob> panic(Mob mob, double speed);

    VanillaGoal<Raider> pathFindToRaid(Raider raider);

    VanillaGoal<Mob> randomLookAround(Mob mob);

    VanillaGoal<AbstractHorse> randomStand(AbstractHorse abstractHorse);

    VanillaGoal<Mob> randomSwim(Mob mob, double speed, int chance);

    VanillaGoal<RangedEntity> rangedAttack(RangedEntity rangedEntity, double mobSpeed, int minIntervalTicks, int maxIntervalTicks, float maxShootRange);

    VanillaGoal<RangedEntity> rangedBowAttack(RangedEntity rangedEntity, double speed, int attackInterval, float range);

    VanillaGoal<RangedEntity> rangedCrossBowAttack(RangedEntity rangedEntity, double speed, float range);

    VanillaGoal<Mob> removeBlock(Mob mob, Material blockType, double speed, int maxYDifference);

    VanillaGoal<Mob> restrictSun(Mob mob);

    VanillaGoal<AbstractHorse> runAroundLikeCrazy(AbstractHorse abstractHorse, double speed);

    VanillaGoal<Tameable> sitWhenOrderedTo(Tameable tameable);

    VanillaGoal<Mob> strollThroughVillage(Mob mob, int searchEngine);

    VanillaGoal<Creeper> swellGoal(Creeper creeper, double distanceToStartSwell, double distanceToStopSwell);

    VanillaGoal<Mob> temptGoal(Mob mob, double speed, RecipeChoice food, boolean canBeScared);

    VanillaGoal<AbstractVillager> tradeWithPlayer(AbstractVillager abstractVillager);

    VanillaGoal<Mob> tryFindWater(Mob mob);

    VanillaGoal<Mob> useItem(Mob mob, ItemStack item, Sound.Type type, Predicate<Mob> mobPredicate);
    VanillaGoal<Mob> waterAvoidRandomFly(Mob mob, double speed);
    VanillaGoal<Mob> waterAvoidRandomStroll(Mob mob, double speed, float probability);
    VanillaGoal<Zombie> zombieAttackGoal(Zombie zombie, double speed, boolean pauseWhenMobIdle);
}

package de.verdox.mccreativelab.entity;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.entity.AnimalEntityBehaviour;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;

/**
 * Represents a cow behaviour.
 * This cow will receive damage in water and cannot be attached to a leash
 */
public class UnleashableSugarCow implements AnimalEntityBehaviour<Cow> {
    @Override
    public BehaviourResult.Bool isSensitiveToWater(Cow entity) {
        return new BehaviourResult.Bool(true, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Object<Float> waterDamage(Cow entity) {
        return new BehaviourResult.Object<>(5f, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool canBeLeashed(Cow entity, Player player) {
        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }
}

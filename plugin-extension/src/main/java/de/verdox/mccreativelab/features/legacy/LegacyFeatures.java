package de.verdox.mccreativelab.features.legacy;

import de.verdox.mccreativelab.features.legacy.herobrine.HerobrineFeature;

public final class LegacyFeatures {
    public final LegacyFoodSystem legacyFoodSystem = new LegacyFoodSystem();
    public final LegacyCombatSystem legacyCombatSystem = new LegacyCombatSystem();
    public final HerobrineFeature herobrineFeature = new HerobrineFeature();
    public void enableOldFoodSystem() {
        legacyFoodSystem.enable();
    }

    public void enableOldCombatSystem() {
        legacyCombatSystem.enable();
    }

    public void enableHerobrine() {
        herobrineFeature.enable();
    }
}

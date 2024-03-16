package de.verdox.mccreativelab.features.legacy;

import de.verdox.mccreativelab.features.legacy.herobrine.HerobrineFeature;

public final class LegacyFeatures {
    private LegacyFoodSystem legacyFoodSystem;
    private LegacyCombatSystem legacyCombatSystem;
    private HerobrineFeature herobrineFeature;
    public void enableOldFoodSystem() {
        legacyFoodSystem = new LegacyFoodSystem();
        legacyFoodSystem.enable();
    }

    public void enableOldCombatSystem() {
        legacyCombatSystem = new LegacyCombatSystem();
        legacyCombatSystem.enable();
    }

    public void enableHerobrine() {
        herobrineFeature = new HerobrineFeature();
        herobrineFeature.enable();
    }

    public HerobrineFeature getHerobrineFeature() {
        return herobrineFeature;
    }

    public LegacyCombatSystem getLegacyCombatSystem() {
        return legacyCombatSystem;
    }

    public LegacyFoodSystem getLegacyFoodSystem() {
        return legacyFoodSystem;
    }
}

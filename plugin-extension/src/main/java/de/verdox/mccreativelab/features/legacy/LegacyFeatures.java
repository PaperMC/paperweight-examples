package de.verdox.mccreativelab.features.legacy;

public final class LegacyFeatures {
    public void enableOldFoodSystem(){
        new LegacyFoodSystem().enable();
    }

    public void enableOldCombatSystem(){
        new LegacyCombatSystem().enable();
    }
}

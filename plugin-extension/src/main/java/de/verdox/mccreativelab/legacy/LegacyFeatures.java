package de.verdox.mccreativelab.legacy;

public class LegacyFeatures {
    public void enableOldFoodSystem(){
        new LegacyFoodSystem().enable();
    }

    public void enableOldCombatSystem(){
        new LegacyCombatSystem().enable();
    }
}

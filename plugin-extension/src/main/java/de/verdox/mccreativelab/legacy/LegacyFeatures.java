package de.verdox.mccreativelab.legacy;

public class LegacyFeatures {
    public static void enableOldFoodSystem(){
        new LegacyFoodSystem().enable();
    }

    public static void enableOldCombatSystem(){
        new LegacyCombatSystem().enable();
    }
}

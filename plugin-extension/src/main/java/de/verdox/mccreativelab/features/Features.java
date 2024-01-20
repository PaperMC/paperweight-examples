package de.verdox.mccreativelab.features;

public final class Features {

    public void useTrueDarkness(){
        new TrueDarknessFeature().enable();
    }

    public void useBetaFog(){
        new BetaFogFeature().enable();
    }
}

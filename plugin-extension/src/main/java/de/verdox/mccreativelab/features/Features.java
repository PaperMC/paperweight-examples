package de.verdox.mccreativelab.features;

import de.verdox.mccreativelab.features.gui.GuiUtility;

public final class Features {

    public void useTrueDarkness(){
        new TrueDarknessFeature().enable();
    }

    public void useBetaFog(){
        new BetaFogFeature().enable();
    }

    public void enableGUIUtility(){
        new GuiUtility().enable();
    }
}

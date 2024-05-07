package de.verdox.mccreativelab.features.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.features.Feature;

public class GuiUtility extends Feature {

    private final YesNoGui yesNoGui = new YesNoGui();

    @Override
    protected void onEnable() {
        GUIButtons.init();
        MCCreativeLabExtension.getCustomResourcePack().addTranslation(YesNoGui.YES);
        MCCreativeLabExtension.getCustomResourcePack().addTranslation(YesNoGui.NO);
        MCCreativeLabExtension.getCustomResourcePack().register(yesNoGui);
    }

    public static YesNoGui.Builder yesNoGui(){
        return new YesNoGui.Builder();
    }
}

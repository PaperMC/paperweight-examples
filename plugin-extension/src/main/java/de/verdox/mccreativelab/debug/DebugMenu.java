package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.CustomMenu;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.Resolution;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class DebugMenu extends CustomMenu {
    public DebugMenu(@NotNull NamespacedKey namespacedKey) {
        super(namespacedKey);
        withBackgroundPicture("background", new Asset<>("/resolution/debug_menu.png"), Resolution.FULL_HD);
        createState("standard", true, menuState -> {
            menuState.onEnable(activeMenu -> activeMenu.setBackgroundPicture("background"));
        });
    }
}

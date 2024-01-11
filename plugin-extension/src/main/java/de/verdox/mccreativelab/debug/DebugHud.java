package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import de.verdox.mccreativelab.util.io.StringAlign;
import org.bukkit.NamespacedKey;

public class DebugHud extends CustomHud {
    public DebugHud(NamespacedKey namespacedKey) {
        super(namespacedKey);

        withText("debugText", new ScreenPosition(100, 1, 0, 10, 0), StringAlign.Alignment.RIGHT, 4, 1);
    }
}

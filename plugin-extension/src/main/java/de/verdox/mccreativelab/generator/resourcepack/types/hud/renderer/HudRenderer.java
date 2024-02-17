package de.verdox.mccreativelab.generator.resourcepack.types.hud.renderer;

import de.verdox.mccreativelab.generator.resourcepack.types.hud.ActiveHud;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import org.bukkit.entity.Player;

public interface HudRenderer {
    ActiveHud getActiveHud(Player player, CustomHud customHud);
    ActiveHud getOrStartActiveHud(Player player, CustomHud customHud);
    boolean stopActiveHud(Player player, CustomHud customHud);
    void forceUpdate(Player player);
}

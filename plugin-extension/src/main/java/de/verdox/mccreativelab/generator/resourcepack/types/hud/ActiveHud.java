package de.verdox.mccreativelab.generator.resourcepack.types.hud;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import org.bukkit.entity.Player;

public class ActiveHud extends ActiveComponentRendered<ActiveHud, CustomHud> {
    public ActiveHud(Player player, CustomHud customHud){
        super(customHud);
        viewers.add(player);
    }

    @Override
    protected void doUpdate() {
        for (Player viewer : getViewers()) {
            if (MCCreativeLabExtension.getHudRenderer().getActiveHud(viewer, getComponentRendered()) != null)
                MCCreativeLabExtension.getHudRenderer().forceUpdate(viewer);
        }
    }
}

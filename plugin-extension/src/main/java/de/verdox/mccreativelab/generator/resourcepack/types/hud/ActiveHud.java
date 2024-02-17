package de.verdox.mccreativelab.generator.resourcepack.types.hud;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import org.bukkit.entity.Player;

public class ActiveHud extends ActiveComponentRendered<ActiveHud, CustomHud> {
    public ActiveHud(Player player, CustomHud customHud){
        super(player, customHud);
    }

    @Override
    protected void doUpdate() {
        if (MCCreativeLabExtension.getHudRenderer().getActiveHud(getPlayer(), (CustomHud) getComponentRendered()) != null)
            MCCreativeLabExtension.getHudRenderer().forceUpdate(getPlayer());
    }
}

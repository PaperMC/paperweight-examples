package de.verdox.mccreativelab.generator.listener;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.event.MCCreativeLabReloadEvent;
import de.verdox.mccreativelab.generator.ResourcePackFileHoster;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class ResourcePackListener implements Listener {
    @EventHandler
    public void kickPlayersIfResourcePackWasNotAppliedButIsRequired(PlayerResourcePackStatusEvent e) {
        switch (e.getStatus()) {
            case DECLINED, FAILED_DOWNLOAD -> e.getPlayer().kick(null, PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
        }
    }

    @EventHandler
    public void applyRequiredResourcePackOnJoin(PlayerJoinEvent e) {
        MCCreativeLabExtension.getInstance().getResourcePackFileHoster().sendDefaultResourcePackToPlayer(e.getPlayer());
    }

    @EventHandler
    public void buildPackOnServerLoad(ServerLoadEvent e) {
        MCCreativeLabExtension.getInstance().onServerLoad(e.getType());
    }

    @EventHandler
    public void onPluginReload(MCCreativeLabReloadEvent ignored){
        MCCreativeLabExtension.getInstance().onServerLoad(ServerLoadEvent.LoadType.RELOAD);
    }
}

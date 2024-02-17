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
import java.util.*;

public class ResourcePackListener implements Listener {
    private final Map<Player, Set<UUID>> loadedResourcePacks = new HashMap<>();

    @EventHandler
    public void kickPlayersIfResourcePackWasNotAppliedButIsRequired(PlayerResourcePackStatusEvent e) {
        switch (e.getStatus()) {
            case DECLINED, FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL ->
                e.getPlayer().kick(null, PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
            case SUCCESSFULLY_LOADED -> {
                loadedResourcePacks.computeIfAbsent(e.getPlayer(), player -> new HashSet<>()).add(e.getID());
                MCCreativeLabExtension.getResourcePackFileHoster().sendDefaultResourcePackToPlayer(e.getPlayer());
            }
            case DISCARDED ->
                loadedResourcePacks.computeIfAbsent(e.getPlayer(), player -> new HashSet<>()).remove(e.getID());
        }
    }

    @EventHandler
    public void applyRequiredResourcePackOnJoin(PlayerJoinEvent e) {
        MCCreativeLabExtension.getInstance().getResourcePackFileHoster().sendDefaultResourcePackToPlayer(e.getPlayer());
    }
}

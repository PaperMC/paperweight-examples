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
        sendDefaultResourcePackToPlayer(e.getPlayer());
    }

    @EventHandler
    public void buildPackOnServerLoad(ServerLoadEvent e) {
        if (!buildPackAndZipFiles()) return;

        if (!e.getType().equals(ServerLoadEvent.LoadType.RELOAD))
            return;
        sendDefaultResourcePackToPlayers(Bukkit.getOnlinePlayers());
    }

    @EventHandler
    public void onPluginReload(MCCreativeLabReloadEvent e){
        if (!buildPackAndZipFiles()){
            Bukkit.shutdown();
            return;
        }
        sendDefaultResourcePackToPlayers(Bukkit.getOnlinePlayers());
    }

    private boolean buildPackAndZipFiles() {
        try {
            MCCreativeLabExtension.getInstance().getCustomResourcePack().installPack();
            MCCreativeLabExtension.getInstance().getResourcePackFileHoster().createResourcePackZipFiles();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    private void sendDefaultResourcePackToPlayer(Player player) {
        ResourcePackFileHoster.ResourcePackInfo resourcePackInfo = getDefaultResourcePackInfo();
        if (resourcePackInfo == null)
            return;
        sendResourcePackToPlayer(player, resourcePackInfo);
    }

    private void sendDefaultResourcePackToPlayers(Collection<? extends Player> players) {
        ResourcePackFileHoster.ResourcePackInfo resourcePackInfo = getDefaultResourcePackInfo();
        if (resourcePackInfo == null)
            return;
        players.forEach(player -> sendResourcePackToPlayer(player, resourcePackInfo));
    }

    private void sendResourcePackToPlayer(Player player, ResourcePackFileHoster.ResourcePackInfo packInfo) {
        String downloadURL = MCCreativeLabExtension.getInstance().getResourcePackFileHoster()
                                                   .createDownloadUrl(packInfo.hash());

        player.setResourcePack(downloadURL, packInfo.hash().getBytes(), true);
    }

    private ResourcePackFileHoster.ResourcePackInfo getDefaultResourcePackInfo() {
        return MCCreativeLabExtension.getInstance()
                                     .getResourcePackFileHoster()
                                     .getRequiredResourcePack();
    }
}

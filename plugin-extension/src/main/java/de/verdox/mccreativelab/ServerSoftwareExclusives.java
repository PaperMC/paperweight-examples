package de.verdox.mccreativelab;

import de.verdox.mccreativelab.event.MCCreativeLabReloadEvent;
import de.verdox.mccreativelab.world.block.FakeBlockRegistry;
import de.verdox.mccreativelab.world.block.replaced.ReplacedBlocks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerSoftwareExclusives implements Listener {

    public void onLoad(){

    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public void onServerLoad(ServerLoadEvent.LoadType loadType){
        if(loadType.equals(ServerLoadEvent.LoadType.STARTUP)){
            FakeBlockRegistry.setupFakeBlocks();
            ReplacedBlocks.setup();
            Bukkit.getLogger().info("ServerSoftware exclusive features started");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void buildPackOnServerLoad(ServerLoadEvent e) {
        onServerLoad(e.getType());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginReload(MCCreativeLabReloadEvent ignored) {
        onServerLoad(ServerLoadEvent.LoadType.RELOAD);
    }
}

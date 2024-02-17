package de.verdox.mccreativelab.util.nbt;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import de.verdox.mccreativelab.events.ChunkDataSaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.persistence.PersistentDataHolder;

public class PersistentDataSaver implements Listener {

    @EventHandler
    public void saveEntity(EntityRemoveFromWorldEvent e) {
        save(e.getEntity(), true);
    }

    @EventHandler
    public void saveChunk(ChunkDataSaveEvent e) {
        save(e.getChunk(), e.isUnloaded());
    }

    @EventHandler
    public void worldSave(WorldSaveEvent e) {
        saveWorld(e.getWorld());
    }

    @EventHandler
    public void savePlayerOnQuit(PlayerQuitEvent e) {
        save(e.getPlayer(), true);
    }

    private static void save(PersistentDataHolder persistentDataHolder, boolean clearCache) {
        for (PersistentData<PersistentDataHolder> allCachedDatum : PersistentData.getAllCachedData(persistentDataHolder)) {
            allCachedDatum.save(persistentDataHolder);
        }
        if (clearCache)
            PersistentData.clearCache(persistentDataHolder);
    }

    private static void saveWorld(World world) {
        save(world, false);

        // Also includes players
        for (Entity entity : world.getEntities())
            save(entity, false);

        for (Chunk loadedChunk : world.getLoadedChunks())
            save(loadedChunk, false);
    }
}

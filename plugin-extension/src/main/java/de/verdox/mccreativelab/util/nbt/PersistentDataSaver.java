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
import org.bukkit.event.world.WorldSaveEvent;

public class PersistentDataSaver implements Listener {
    @EventHandler
    public void saveEntity(EntityRemoveFromWorldEvent e) {
        saveEntity(e.getEntity());
    }

    @EventHandler
    public void saveChunk(ChunkDataSaveEvent e) {
        if (e.getChunk() != null)
            saveChunk(e.getChunk());
    }

    @EventHandler
    public void worldSave(WorldSaveEvent e) {
        saveWorld(e.getWorld());
    }

    private static void saveEntity(Entity entity) {
        for (Class<? extends EntityPersistentData> type : PersistentData.entityPersistentDataClasses)
            PersistentData.get(type, entity).save();

    }

    private static void saveChunk(Chunk chunk) {
        for (Class<? extends ChunkPersistentData> type : PersistentData.chunkPersistentDataClasses)
            PersistentData.get(type, chunk).save();
    }

    private static void savePlayer(Player player) {
        for (Class<? extends PlayerPersistentData> type : PersistentData.playerPersistentDataClasses)
            PersistentData.get(type, player).save();
    }

    private static void saveWorld(World world) {
        for (Class<? extends WorldPersistentData> type : PersistentData.worldPersistentDataClasses)
            PersistentData.get(type, world).save();

        for (Chunk loadedChunk : world.getLoadedChunks())
            saveChunk(loadedChunk);

        for (Entity entity : world.getEntities())
            saveEntity(entity);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            savePlayer(onlinePlayer);
    }
}

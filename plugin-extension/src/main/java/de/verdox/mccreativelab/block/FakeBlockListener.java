package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.events.ChunkDataCreateEvent;
import de.verdox.mccreativelab.events.ChunkDataLoadEvent;
import de.verdox.mccreativelab.events.ChunkDataSaveEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class FakeBlockListener implements Listener {

    @EventHandler
    public void worldSave(WorldSaveEvent e){
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().save(e.getWorld());
    }
    @EventHandler
    public void onChunkDataCreate(ChunkDataCreateEvent e) {
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().createData(e.getWorld(), Chunk.getChunkKey(e.getChunkPos().x(), e.getChunkPos().z()));
    }

    @EventHandler
    public void onChunkDataLoad(ChunkDataLoadEvent e) {
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().loadChunk(e.getChunk(), e.getPersistentDataContainer());
    }


    @EventHandler
    public void onChunkDataSave(ChunkDataSaveEvent e) {
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().saveChunk(e.getChunk(), e.getPersistentDataContainer(), e.isUnloaded());
    }
}

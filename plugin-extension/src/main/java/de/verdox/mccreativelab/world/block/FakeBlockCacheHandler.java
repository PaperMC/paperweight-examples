package de.verdox.mccreativelab.world.block;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.events.ChunkDataEvent;
import de.verdox.mccreativelab.world.block.display.strategy.FakeBlockVisualStrategy;
import de.verdox.mccreativelab.events.ChunkDataCreateEvent;
import de.verdox.mccreativelab.events.ChunkDataLoadEvent;
import de.verdox.mccreativelab.events.ChunkDataSaveEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FakeBlockCacheHandler implements Listener {
    private static FakeBlockCacheHandler INSTANCE;

    public FakeBlockCacheHandler() {
        if (INSTANCE != null)
            throw new IllegalArgumentException("FakeBlockCacheHandler singleton instantiated more than once");
        INSTANCE = this;
    }

    @EventHandler
    public void worldSave(WorldSaveEvent e) {
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().save(e.getWorld());
    }

    @EventHandler
    public void onChunkDataCreate(ChunkDataCreateEvent e) {
        MCCreativeLabExtension.getInstance().getFakeBlockStorage()
                              .createData(e.getWorld(), Chunk.getChunkKey(e.getChunkPos().x(), e.getChunkPos().z()));
    }

    @EventHandler
    public void onChunkDataLoad(ChunkDataLoadEvent e) {
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage()
                              .loadChunk(e.getChunk(), e.getPersistentDataContainer());
    }

    @EventHandler
    public void onChunkDataSave(ChunkDataSaveEvent e) {
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage()
                              .saveChunk(e.getChunk(), e.getPersistentDataContainer(), e.isUnloaded());
    }

    private Map<ChunkDataEvent.ChunkPos, Set<FakeBlockVisualStrategy.PotentialItemDisplay>> potentialLoaded = new ConcurrentHashMap<>();

    @EventHandler
    public void linkDisplayEntitiesToFakeBlocks(EntityAddToWorldEvent e) {
        if (!(e.getEntity() instanceof ItemDisplay itemDisplay))
            return;
        FakeBlockVisualStrategy.PotentialItemDisplay potentialItemDisplay = FakeBlockVisualStrategy.loadPotentialDisplay(itemDisplay);
        if(potentialItemDisplay == null)
            return;
        potentialLoaded.computeIfAbsent(new ChunkDataEvent.ChunkPos(e.getEntity().getChunk().getX(), e.getEntity().getChunk().getZ()), chunkPos -> new HashSet<>()).add(potentialItemDisplay);
    }

    @EventHandler
    public void loadAndLinkDisplayEntitiesWhenChunkWasLoaded(ChunkLoadEvent e){
        if(e.isNewChunk())
            return;
        ChunkDataEvent.ChunkPos chunkPos = new ChunkDataEvent.ChunkPos(e.getChunk().getX(), e.getChunk().getZ());
        if(!potentialLoaded.containsKey(chunkPos))
            return;
        for (FakeBlockVisualStrategy.PotentialItemDisplay potentialItemDisplay : potentialLoaded.get(chunkPos)) {
            potentialItemDisplay.load();
        }
    }
}

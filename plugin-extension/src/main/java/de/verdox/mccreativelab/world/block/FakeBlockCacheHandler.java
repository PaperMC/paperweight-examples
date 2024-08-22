package de.verdox.mccreativelab.world.block;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.entity.EntityBehaviour;
import de.verdox.mccreativelab.events.ChunkDataEvent;
import de.verdox.mccreativelab.world.block.display.strategy.FakeBlockVisualStrategy;
import de.verdox.mccreativelab.events.ChunkDataCreateEvent;
import de.verdox.mccreativelab.events.ChunkDataLoadEvent;
import de.verdox.mccreativelab.events.ChunkDataSaveEvent;
import de.verdox.mccreativelab.world.block.entity.FakeBlockEntity;
import de.verdox.mccreativelab.world.block.entity.FakeBlockEntityStorage;
import org.bukkit.Chunk;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Marker;
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

    /*@EventHandler
    public void worldSave(WorldSaveEvent e) {
        if (FakeBlockStorage.USE_NEW_IMPLEMENTATION)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().save(e.getWorld());
    }

    @EventHandler
    public void onChunkDataCreate(ChunkDataCreateEvent e) {
        if (FakeBlockStorage.USE_NEW_IMPLEMENTATION)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage()
            .createData(e.getWorld(), Chunk.getChunkKey(e.getChunkPos().x(), e.getChunkPos().z()));
    }

    @EventHandler
    public void onChunkDataLoad(ChunkDataLoadEvent e) {
        if (FakeBlockStorage.USE_NEW_IMPLEMENTATION)
            return;
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage()
            .loadChunk(e.getChunk(), e.getPersistentDataContainer());
    }

    @EventHandler
    public void onChunkDataSave(ChunkDataSaveEvent e) {
        if (FakeBlockStorage.USE_NEW_IMPLEMENTATION)
            return;
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage()
            .saveChunk(e.getChunk(), e.getPersistentDataContainer(), e.isUnloaded());
    }*/

    private Map<ChunkDataEvent.ChunkPos, Set<FakeBlockVisualStrategy.PotentialItemDisplay>> potentialLoadedBlockVisuals = new ConcurrentHashMap<>();
    private Map<ChunkDataEvent.ChunkPos, Set<Marker>> potentialLoadedBlockEntities = new ConcurrentHashMap<>();

    @EventHandler
    public void linkDisplayEntitiesToFakeBlocks(EntityAddToWorldEvent e) {
        if ((e.getEntity() instanceof ItemDisplay itemDisplay)) {
            FakeBlockVisualStrategy.PotentialItemDisplay potentialItemDisplay = FakeBlockVisualStrategy.loadPotentialDisplay(itemDisplay);
            if (potentialItemDisplay == null)
                return;
            potentialLoadedBlockVisuals.computeIfAbsent(new ChunkDataEvent.ChunkPos(e.getEntity().getChunk().getX(), e.getEntity().getChunk().getZ()), chunkPos -> new HashSet<>()).add(potentialItemDisplay);
            return;
        }
        if (e.getEntity() instanceof Marker marker) {
            potentialLoadedBlockEntities.computeIfAbsent(new ChunkDataEvent.ChunkPos(e.getEntity().getChunk().getX(), e.getEntity().getChunk().getZ()), chunkPos -> new HashSet<>()).add(marker);
        }
    }

    @EventHandler
    public void entityRemoveFromWorld(EntityRemoveFromWorldEvent e) {
        if (e.getEntity() instanceof Marker marker) {
            FakeBlockEntity fakeBlockEntity = FakeBlockEntityStorage.getAsFakeBlockEntity(marker);
            if (fakeBlockEntity != null) {
                fakeBlockEntity.onUnload();
            }
        }
    }

    @EventHandler
    public void loadAndLinkDisplayEntitiesWhenChunkWasLoaded(ChunkLoadEvent e) {
        if (e.isNewChunk())
            return;
        ChunkDataEvent.ChunkPos chunkPos = new ChunkDataEvent.ChunkPos(e.getChunk().getX(), e.getChunk().getZ());

        if (potentialLoadedBlockVisuals.containsKey(chunkPos)) {
            for (FakeBlockVisualStrategy.PotentialItemDisplay potentialItemDisplay : potentialLoadedBlockVisuals.get(chunkPos)) {
                potentialItemDisplay.load();
            }
            potentialLoadedBlockVisuals.remove(chunkPos);
        }

        if (potentialLoadedBlockEntities.containsKey(chunkPos)) {
            for (Marker marker : potentialLoadedBlockEntities.get(chunkPos)) {
                FakeBlockEntityStorage.getFakeBlockEntityAt(marker.getLocation().getBlock());
            }
            potentialLoadedBlockEntities.remove(chunkPos);

        }
    }
}

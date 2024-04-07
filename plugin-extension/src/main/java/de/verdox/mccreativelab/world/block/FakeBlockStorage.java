package de.verdox.mccreativelab.world.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.util.storage.palette.NBTPalettedContainer;
import de.verdox.mccreativelab.registry.exception.PaletteValueUnknownException;
import de.verdox.mccreativelab.util.storage.palette.IdMap;
import de.verdox.mccreativelab.util.PaletteUtil;
import de.verdox.mccreativelab.world.item.FakeItem;
import de.verdox.mccreativelab.worldgen.WorldGenChunk;
import org.bukkit.*;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeBlockStorage {
    private static final NamespacedKey FAKE_BLOCK_PALETTE_KEY = new NamespacedKey("mccreativelab", "fake_block_storage");
    private static final NamespacedKey BLOCK_STATE_BLOCK_KEY = new NamespacedKey("mccreativelab", "fake_block_key");
    private static final NamespacedKey BLOCK_STATE_ID = new NamespacedKey("mccreativelab", "block_state_id");

    public static boolean setFakeBlock(Location location, @Nullable FakeBlock fakeBlock, boolean forceLoad) {
        return setFakeBlockState(location, fakeBlock != null ? fakeBlock.getDefaultBlockState() : null, forceLoad);
    }

    public static FakeBlock getFakeBlock(Location location, boolean forceLoad) {
        FakeBlock.FakeBlockState fakeBlockState = getFakeBlockState(location, forceLoad);
        if (fakeBlockState == null)
            return null;
        return fakeBlockState.getFakeBlock();
    }

    public static boolean setFakeBlockState(Location location, @Nullable FakeBlock.FakeBlockState fakeBlockState, boolean forceLoad) {
        FakeBlockStorage fakeBlockStorage = MCCreativeLabExtension.getFakeBlockStorage();

        int localX = PaletteUtil.worldXToPaletteXCoordinate(location.getBlockX());
        int localY = PaletteUtil.worldYCoordinateToPaletteCoordinate(location.getWorld(), location.getBlockY());
        int localZ = PaletteUtil.worldZToPaletteXCoordinate(location.getBlockZ());

        long chunkKey = Chunk.getChunkKey(location);
        World world = location.getWorld();
        boolean isChunkLoaded = world.isChunkLoaded((int) chunkKey, (int) (chunkKey >> 32));

        //TODO: Zusätzlich prüfen ob fake block palette geladen wurde  // Wenn nicht muss das hier gemacht werden
        if (!isChunkLoaded) {
            if (forceLoad)
                location.getChunk().load(true);
            else return false;
        }
        FakeBlockPalettedContainer fakeBlockPaletteContainer = fakeBlockStorage.getFakeBlockPalette(world, chunkKey);
        FakeBlock.FakeBlockState currentFakeBlockState = fakeBlockPaletteContainer.getData(localX, localY, localZ);
        if (currentFakeBlockState != null) {
            currentFakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy()
                                 .removeFakeBlockDisplay(location.getBlock());
            location.getBlock().setBlockData(Bukkit.createBlockData(Material.AIR));
        }
        fakeBlockPaletteContainer.setData(fakeBlockState, localX, localY, localZ);

        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy().spawnFakeBlockDisplay(location.getBlock(), fakeBlockState);
            location.getBlock().setBlockData(fakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData());
        }
        return true;
    }

    public static @Nullable FakeBlock.FakeBlockState getFakeBlockState(Location location, boolean forceLoad) {
        if (location.getBlockY() > location.getWorld().getMaxHeight()) {
            Bukkit.getLogger().warning("Tried to read a fakeblockstate above world max height");
            return null;
        }
        FakeBlockStorage fakeBlockStorage = MCCreativeLabExtension.getInstance().getFakeBlockStorage();

        int localX = PaletteUtil.worldXToPaletteXCoordinate(location.getBlockX());
        int localY = PaletteUtil.worldYCoordinateToPaletteCoordinate(location.getWorld(), location.getBlockY());
        int localZ = PaletteUtil.worldXToPaletteXCoordinate(location.getBlockZ());

        long chunkKey = Chunk.getChunkKey(location);
        World world = location.getWorld();
        boolean isChunkLoaded = world.isChunkLoaded((int) chunkKey, (int) (chunkKey >> 32));

        //TODO: Zusätzlich prüfen ob fake block palette geladen wurde  // Wenn nicht muss das hier gemacht werden
        if (!isChunkLoaded) {
            if (forceLoad)
                location.getChunk().load(true);
            else {
                return null;
            }
        }

        FakeBlockPalettedContainer fakeBlockPaletteContainer = fakeBlockStorage.getFakeBlockPalette(world, chunkKey);
        if (fakeBlockPaletteContainer == null)
            throw new IllegalStateException("FakeBlockPaletteContainer was null - This is a bug");
        return fakeBlockPaletteContainer.getData(localX, localY, localZ);
    }

    private final Map<ChunkEntry, FakeBlockPalettedContainer> fakeBlockPalettes = new ConcurrentHashMap<>();

    public FakeBlockStorage() {
    }

    public void saveAll() {
        Bukkit.getLogger().info("FakeBlockStorage: saveAll");
        for (World world : Bukkit.getWorlds()) {
            save(world);
        }
    }

    void save(World world) {
        for (Chunk loadedChunk : world.getLoadedChunks())
            saveChunk(loadedChunk, loadedChunk.getPersistentDataContainer(), false);
    }

    FakeBlockPalettedContainer getFakeBlockPalette(World world, long chunkKey) {
        ChunkEntry chunkEntry = new ChunkEntry(world.getName(), chunkKey);
        if (!fakeBlockPalettes.containsKey(chunkEntry))
            return createData(world, chunkKey);
        return fakeBlockPalettes.get(chunkEntry);
    }

    FakeBlockPalettedContainer createData(World world, long chunkKey) {
        ChunkEntry chunkEntry = new ChunkEntry(world.getName(), chunkKey);
        FakeBlockPalettedContainer fakeBlockPaletteContainer = new FakeBlockPalettedContainer(FakeBlockRegistry.FAKE_BLOCK_STATE_ID_MAP, 16, PaletteUtil.getMaxYPaletteFromWorldLimits(world), 16);
        fakeBlockPalettes.put(chunkEntry, fakeBlockPaletteContainer);
        return fakeBlockPaletteContainer;
    }

    void loadChunk(WorldGenChunk chunk, PersistentDataContainer persistentDataContainer) {
        ChunkEntry chunkEntry = new ChunkEntry(chunk.getWorld().getName(), chunk.getChunkKey());
        FakeBlockPalettedContainer fakeBlockPaletteContainer = new FakeBlockPalettedContainer(FakeBlockRegistry.FAKE_BLOCK_STATE_ID_MAP, 16, PaletteUtil.getMaxYPaletteFromWorldLimits(chunk.getWorld()), 16);

        if (persistentDataContainer.has(FAKE_BLOCK_PALETTE_KEY)) {
            PersistentDataContainer fakeBlockPaletteNBT = persistentDataContainer.get(FAKE_BLOCK_PALETTE_KEY, PersistentDataType.TAG_CONTAINER);
            if (fakeBlockPaletteNBT == null) {
                Bukkit.getLogger()
                      .warning("Corrupt fake block palette found for chunk " + chunk.getX() + " " + chunk.getZ() + " in world " + chunk
                          .getWorld().getName());
            } else
                fakeBlockPaletteContainer.deSerialize(FAKE_BLOCK_PALETTE_KEY, persistentDataContainer);
        }

        fakeBlockPalettes.put(chunkEntry, fakeBlockPaletteContainer);
    }

    void saveChunk(WorldGenChunk chunk, PersistentDataContainer persistentDataContainer, boolean unload) {
        ChunkEntry chunkEntry = new ChunkEntry(chunk.getWorld().getName(), chunk.getChunkKey());
        FakeBlockPalettedContainer fakeBlockPaletteContainer = fakeBlockPalettes.get(chunkEntry);
        if (fakeBlockPaletteContainer == null)
            return;
        fakeBlockPaletteContainer.serialize(FAKE_BLOCK_PALETTE_KEY, persistentDataContainer);
        if (unload)
            fakeBlockPalettes.remove(chunkEntry);
    }

    public static class FakeBlockPalettedContainer extends NBTPalettedContainer<FakeBlock.FakeBlockState> {
        public FakeBlockPalettedContainer(IdMap<FakeBlock.FakeBlockState> idMap, int xSize, int ySize, int zSize) {
            super(idMap, xSize, ySize, zSize);
        }

        public PersistentDataContainer dataToNbt(PersistentDataAdapterContext adapterContext, FakeBlock.FakeBlockState data) {
            PersistentDataContainer blockStateData = adapterContext.newPersistentDataContainer();

            FakeBlock fakeBlock = data.getFakeBlock();
            int blockStateID = fakeBlock.getBlockStateID(data);
            if (blockStateID == -1)
                return null;
            NamespacedKey blockKey = MCCreativeLabExtension.getFakeBlockRegistry().getKey(fakeBlock);

            blockStateData.set(BLOCK_STATE_BLOCK_KEY, PersistentDataType.STRING, blockKey.asString());
            blockStateData.set(BLOCK_STATE_ID, PersistentDataType.INTEGER, blockStateID);

            return blockStateData;
        }

        @Nullable
        public FakeBlock.FakeBlockState nbtToData(PersistentDataContainer persistentDataContainer) {
            String blockKeyAsString = persistentDataContainer.get(BLOCK_STATE_BLOCK_KEY, PersistentDataType.STRING);
            if (blockKeyAsString == null)
                return null;
            if (!persistentDataContainer.has(BLOCK_STATE_ID))
                return null;
            int blockStateSubId = persistentDataContainer.get(BLOCK_STATE_ID, PersistentDataType.INTEGER).intValue();

            NamespacedKey namespacedKey = NamespacedKey.fromString(blockKeyAsString);
            FakeBlock foundFakeBlock = MCCreativeLabExtension.getFakeBlockRegistry().get(namespacedKey);
            if (foundFakeBlock == null)
                return null;
            return foundFakeBlock.getBlockState(blockStateSubId);
        }
    }


    private record ChunkEntry(String worldName, long chunkKey) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkEntry that = (ChunkEntry) o;
            return chunkKey == that.chunkKey && Objects.equals(worldName, that.worldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldName, chunkKey);
        }
    }
}

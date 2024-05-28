package de.verdox.mccreativelab.world.block;

import de.verdox.mccreativelab.util.PaletteUtil;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.NBTPersistent;
import de.verdox.mccreativelab.util.storage.palette.NBTPalettedContainer;
import de.verdox.mccreativelab.world.block.customhardness.BlockBreakSpeedModifier;
import org.bukkit.*;

import javax.annotation.Nullable;

public class FakeBlockContainer implements NBTPersistent {
    public static FakeBlockContainer load(Chunk chunk){
        return chunk.getPersistentDataContainer().getPersistentDataObjectCache().loadOrSupplyPersistentDataObject(new NamespacedKey("mccreativelab", "fake_block_storage"), () -> new FakeBlockContainer(chunk));
    }

    public static boolean setFakeBlockState(Location location, @Nullable FakeBlock.FakeBlockState fakeBlockState, boolean updateBlockData, boolean forceLoad) {
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

        Chunk chunk = world.getChunkAt(chunkKey);

        // We have 2 calls for setBlockData but do not want to trigger BlockPhysics twice!
        FakeBlockContainer fakeBlockContainer = FakeBlockContainer.load(chunk);


        FakeBlock.FakeBlockState currentFakeBlockState = fakeBlockContainer.fakeBlockPalettedContainer.getData(localX, localY, localZ);

        if (currentFakeBlockState != null) {
            currentFakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy()
                .removeFakeBlockDisplay(location.getBlock());
            fakeBlockContainer.fakeBlockPalettedContainer.setData(fakeBlockState, localX, localY, localZ);
            location.getBlock().tick();
            if (updateBlockData && fakeBlockState == null && !location.getBlock().getType().equals(Material.AIR))
                location.getBlock().setBlockData(Bukkit.createBlockData(Material.AIR));

        } else fakeBlockContainer.fakeBlockPalettedContainer.setData(fakeBlockState, localX, localY, localZ);

        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy().spawnFakeBlockDisplay(location.getBlock(), fakeBlockState);

            if (updateBlockData && !location.getBlock().getBlockData().equals(fakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData()))
                location.getBlock().setBlockData(fakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData());
        }

        BlockBreakSpeedModifier.stopBlockBreakAtBlock(location.getBlock());
        return true;
    }

    public static @Nullable FakeBlock.FakeBlockState getFakeBlockState(Location location, boolean forceLoad) {
        if (location.getBlockY() > location.getWorld().getMaxHeight()) {
            Bukkit.getLogger().warning("Tried to read a fakeblockstate above world max height");
            return null;
        }

        if (location.getBlockY() < location.getWorld().getMinHeight()) {
            Bukkit.getLogger().warning("Tried to read a fakeblockstate below world min height");
            return null;
        }

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

        Chunk chunk = world.getChunkAt(chunkKey);
        return FakeBlockContainer.load(chunk).fakeBlockPalettedContainer.getData(localX, localY, localZ);
    }

    private final NBTPalettedContainer<FakeBlock.FakeBlockState> fakeBlockPalettedContainer;

    public FakeBlockContainer(Chunk chunk){
        fakeBlockPalettedContainer = new FakeBlockStorage.FakeBlockPalettedContainer(FakeBlockRegistry.FAKE_BLOCK_STATE_ID_MAP, 16, PaletteUtil.getMaxYPaletteFromWorldLimits(chunk.getWorld()), 16);
    }

    @Override
    public void saveNBTData(NBTContainer storage) {
        NBTContainer serializedPalette = storage.createNBTContainer();
        fakeBlockPalettedContainer.saveNBTData(serializedPalette);
        storage.set("serializedPalette", serializedPalette);
    }

    @Override
    public void loadNBTData(NBTContainer storage) {
        if(storage.has("serializedPalette"))
            this.fakeBlockPalettedContainer.loadNBTData(storage.getNBTContainer("serializedPalette"));
    }
}

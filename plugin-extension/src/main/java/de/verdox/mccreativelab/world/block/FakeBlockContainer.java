package de.verdox.mccreativelab.world.block;

import de.verdox.mccreativelab.MCCreativeLab;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.MCCWorldHook;
import de.verdox.mccreativelab.util.PaletteUtil;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.NBTPersistent;
import de.verdox.mccreativelab.util.storage.palette.NBTPalettedContainer;
import de.verdox.mccreativelab.world.block.customhardness.BlockBreakSpeedModifier;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;

public class FakeBlockContainer implements NBTPersistent {
    public static FakeBlockContainer load(Chunk chunk) {
        return chunk.getPersistentDataContainer().getPersistentDataObjectCache().loadOrSupplyPersistentDataObject(new NamespacedKey("mccreativelab", "fake_block_storage"), () -> new FakeBlockContainer(chunk));
    }

    public static boolean setFakeBlockState(Location location, @Nullable FakeBlock.FakeBlockState fakeBlockState, boolean updateBlockData, boolean forceLoad) {
        if(!MCCreativeLabExtension.isServerSoftware())
            return false;
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
        fakeBlockContainer.setFakeBlockState(location, fakeBlockState);
        return true;
    }

    public static @Nullable FakeBlock.FakeBlockState getFakeBlockState(Location location, boolean forceLoad) {
        if(!MCCreativeLabExtension.isServerSoftware())
            return null;
        if (location.getBlockY() > location.getWorld().getMaxHeight()) {
            Bukkit.getLogger().warning("Tried to read a fakeblockstate at ("+location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ()+") above world max height"+location.getWorld().getMaxHeight());
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                Bukkit.getLogger().warning(stackTraceElement+"");
            }
            return null;
        }

        if (location.getBlockY() < location.getWorld().getMinHeight()) {
            Bukkit.getLogger().warning("Tried to read a fakeblockstate at ("+location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ()+") below world min height"+location.getWorld().getMinHeight());
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                Bukkit.getLogger().warning(stackTraceElement+"");
            }
            return null;
        }

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
        return FakeBlockContainer.load(chunk).getFakeBlockState(location);
    }

    private final NBTPalettedContainer<FakeBlock.FakeBlockState> fakeBlockPalettedContainer;
    private final Chunk chunk;

    public FakeBlockContainer(Chunk chunk) {
        fakeBlockPalettedContainer = new FakeBlockStorage.FakeBlockPalettedContainer(FakeBlockRegistry.FAKE_BLOCK_STATE_ID_MAP, 16, PaletteUtil.getMaxYPaletteFromWorldLimits(chunk.getWorld()), 16);
        this.chunk = chunk;
    }

    public void setFakeBlockState(Location location, @Nullable FakeBlock.FakeBlockState fakeBlockState){
        int localX = PaletteUtil.worldXToPaletteXCoordinate(location.getBlockX());
        int localY = PaletteUtil.worldYCoordinateToPaletteCoordinate(location.getWorld(), location.getBlockY());
        int localZ = PaletteUtil.worldZToPaletteXCoordinate(location.getBlockZ());

        MCCWorldHook mccWorldHook = MCCreativeLab.getWorldHook(chunk.getWorld());
        FakeBlock.FakeBlockState currentFakeBlockState = fakeBlockPalettedContainer.getData(localX, localY, localZ);

        if (fakeBlockState != null)
            mccWorldHook.setBlockContext(location.getBlock(), new FakeBlockWorldHook.SetFakeBlockContext(location.getBlock(), fakeBlockState));
        else if (currentFakeBlockState != null)
            mccWorldHook.setBlockContext(location.getBlock(), new FakeBlockWorldHook.RemoveFakeBlockContext(location.getBlock(), currentFakeBlockState));
        else
            mccWorldHook.setBlockContext(location.getBlock(), new FakeBlockWorldHook.FakeBlockContext(location.getBlock()));
        try {

            if (currentFakeBlockState != null) {
                currentFakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy().removeFakeBlockDisplay(location.getBlock());
                fakeBlockPalettedContainer.setData(fakeBlockState, localX, localY, localZ);
                location.getBlock().tick();
                if (fakeBlockState == null && !location.getBlock().getType().equals(Material.AIR)) {
                    BlockData actualBlockData = location.getBlock().getBlockData();

                    if(actualBlockData.equals(currentFakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData()))
                        location.getBlock().setBlockData(Bukkit.createBlockData(Material.AIR));
                }

            } else fakeBlockPalettedContainer.setData(fakeBlockState, localX, localY, localZ);

            if (fakeBlockState != null) {
                fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy().spawnFakeBlockDisplay(location.getBlock(), fakeBlockState);

                // Only remove the block data if it has not changed since without the system noticing.
                if (!location.getBlock().getBlockData().equals(fakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData()))
                    location.getBlock().setBlockData(fakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData());
            }

            BlockBreakSpeedModifier.stopBlockBreakAtBlock(location.getBlock());
        } finally {
            mccWorldHook.setBlockContext(location.getBlock(), null);
        }
    }

    @Nullable
    public FakeBlock.FakeBlockState getFakeBlockState(Location location) {
        int localX = PaletteUtil.worldXToPaletteXCoordinate(location.getBlockX());
        int localY = PaletteUtil.worldYCoordinateToPaletteCoordinate(location.getWorld(), location.getBlockY());
        int localZ = PaletteUtil.worldXToPaletteXCoordinate(location.getBlockZ());
        return fakeBlockPalettedContainer.getData(localX, localY, localZ);
    }

    @Override
    public void saveNBTData(NBTContainer storage) {
        NBTContainer serializedPalette = storage.createNBTContainer();
        fakeBlockPalettedContainer.saveNBTData(serializedPalette);
        storage.set("serializedPalette", serializedPalette);
    }

    @Override
    public void loadNBTData(NBTContainer storage) {
        if (storage.has("serializedPalette"))
            this.fakeBlockPalettedContainer.loadNBTData(storage.getNBTContainer("serializedPalette"));
    }
}

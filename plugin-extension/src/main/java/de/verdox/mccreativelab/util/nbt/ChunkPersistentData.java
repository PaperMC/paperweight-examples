package de.verdox.mccreativelab.util.nbt;

import de.verdox.mccreativelab.worldgen.WorldGenChunk;
import org.bukkit.World;

public abstract class ChunkPersistentData extends PersistentData<WorldGenChunk> {
    private World world;
    private int chunkX;
    private int chunkZ;

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public World getWorld() {
        return world;
    }

    @Override
    void setup(WorldGenChunk persistentDataHolder) {
        this.world = persistentDataHolder.getWorld();
        this.chunkX = persistentDataHolder.getX();
        this.chunkZ = persistentDataHolder.getZ();
    }
}

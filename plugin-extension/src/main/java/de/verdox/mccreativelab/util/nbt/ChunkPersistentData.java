package de.verdox.mccreativelab.util.nbt;

import org.bukkit.Chunk;

public abstract class ChunkPersistentData extends PersistentData<Chunk>{
    protected ChunkPersistentData(Chunk persistentDataHolder) {
        super(persistentDataHolder);
    }
}

package de.verdox.mccreativelab.util.nbt;

import org.bukkit.World;

public abstract class WorldPersistentData extends PersistentData<World> {
    protected WorldPersistentData(World persistentDataHolder) {
        super(persistentDataHolder);
    }
}

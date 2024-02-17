package de.verdox.mccreativelab.util.nbt;

import org.bukkit.World;

import java.util.UUID;

public abstract class WorldPersistentData extends PersistentData<World> {
    private UUID worldUUID;
    private String worldName;

    @Override
    void setup(World persistentDataHolder) {
        this.worldName = persistentDataHolder.getName();
        this.worldUUID = persistentDataHolder.getUID();
    }

    public String getWorldName() {
        return worldName;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }
}

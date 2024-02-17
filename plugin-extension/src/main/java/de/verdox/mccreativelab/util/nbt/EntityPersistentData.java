package de.verdox.mccreativelab.util.nbt;

import org.bukkit.entity.Entity;

import java.util.UUID;

public abstract class EntityPersistentData<T extends Entity> extends PersistentData<T>{
    private UUID entityUUID;
    @Override
    void setup(T persistentDataHolder) {
        this.entityUUID = persistentDataHolder.getUniqueId();
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }
}

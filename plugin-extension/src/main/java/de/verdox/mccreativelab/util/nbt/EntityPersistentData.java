package de.verdox.mccreativelab.util.nbt;

import org.bukkit.entity.Entity;

public abstract class EntityPersistentData<T extends Entity> extends PersistentData<T>{
    protected EntityPersistentData(T persistentDataHolder) {
        super(persistentDataHolder);
    }
}

package de.verdox.mccreativelab.util.nbt;

import org.bukkit.entity.Player;

public abstract class PlayerPersistentData extends PersistentData<Player>{
    protected PlayerPersistentData(Player persistentDataHolder) {
        super(persistentDataHolder);
    }
}

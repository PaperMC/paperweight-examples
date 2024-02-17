package de.verdox.mccreativelab.util.nbt;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class PlayerPersistentData extends PersistentData<Player>{
    private UUID playerUUID;

    @Override
    void setup(Player persistentDataHolder) {
        playerUUID = persistentDataHolder.getUniqueId();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}

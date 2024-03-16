package de.verdox.mccreativelab.util.nbt;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataObject;
import org.jetbrains.annotations.NotNull;

public interface NBTPersistent extends PersistentDataObject, NBTSerializable {
    @Override
    default PersistentDataContainer serialize(@NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer persistentDataContainer = context.newPersistentDataContainer();
        NBTContainer nbtContainer = NBTContainer.of("nbt", persistentDataContainer);
        saveNBTData(nbtContainer);
        return persistentDataContainer;
    }

    @Override
    default void deSerialize(PersistentDataContainer persistentDataContainer) {
        NBTContainer nbtContainer = NBTContainer.of("nbt", persistentDataContainer);
        loadNBTData(nbtContainer);
    }
}

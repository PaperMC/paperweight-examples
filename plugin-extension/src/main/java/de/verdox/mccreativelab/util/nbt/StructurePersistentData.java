package de.verdox.mccreativelab.util.nbt;

import org.bukkit.generator.structure.GeneratedStructure;

import java.util.UUID;

public abstract class StructurePersistentData extends PersistentData<GeneratedStructure> {
    private UUID structureUUID;

    @Override
    void setup(GeneratedStructure persistentDataHolder) {
        NBTContainer nbtContainer = NBTContainer.of("mccreativelab", persistentDataHolder.getPersistentDataContainer());
        UUID uuid;
        if(nbtContainer.has("uuid"))
            uuid = nbtContainer.getUUID("uuid");
        else {
            uuid = UUID.randomUUID();
            nbtContainer.set("uuid", uuid);
        }
        structureUUID = uuid;
    }

    public UUID getStructureUUID() {
        return structureUUID;
    }
}

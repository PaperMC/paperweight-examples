package de.verdox.mccreativelab.util.nbt;

public interface NBTSerializable {
    void saveNBTData(NBTContainer storage);
    void loadNBTData(NBTContainer storage);

}

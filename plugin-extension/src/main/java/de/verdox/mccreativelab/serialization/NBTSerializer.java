package de.verdox.mccreativelab.serialization;

import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.wrapper.MCCWrapped;

public interface NBTSerializer<T> {
    void serialize(T data, NBTContainer nbtContainer);
    T deserialize(NBTContainer nbtContainer);
}

package de.verdox.mccreativelab.generator.datapack;

import de.verdox.mccreativelab.generator.AbstractResource;
import org.bukkit.NamespacedKey;

public abstract class DataPackResource extends AbstractResource<CustomDataPack> {
    public DataPackResource(NamespacedKey namespacedKey) {
        super(namespacedKey);
    }
}

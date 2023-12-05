package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.AbstractResource;
import org.bukkit.NamespacedKey;

public abstract class ResourcePackResource extends AbstractResource<CustomResourcePack> {
    public ResourcePackResource(NamespacedKey namespacedKey) {
        super(namespacedKey);
    }
}

package de.verdox.mccreativelab.generator;

import org.bukkit.NamespacedKey;

import java.io.IOException;

/**
 * Represents a resource in a custom pack.
 * @param <C> The pack type
 */
public interface Resource<C extends CustomPack<C>> {
    /**
     * The resource key of this resource
     * @return The key
     */
    NamespacedKey key();

    void installToDataPack(C customPack) throws IOException;
}

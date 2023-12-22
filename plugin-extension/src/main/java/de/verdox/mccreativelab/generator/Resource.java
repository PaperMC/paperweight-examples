package de.verdox.mccreativelab.generator;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Represents a resource in a custom pack.
 * @param <C> The pack type
 */
public interface Resource<C extends CustomPack<C>> extends Keyed {
    /**
     * The resource key of this resource
     * @return The key
     */
    @NotNull NamespacedKey key();

    @Override
    @NotNull default NamespacedKey getKey(){
        return key();
    }

    /**
     * Is called to install this resource
     * @param customPack The customPack
     * @throws IOException when something fails
     */
    void installResourceToPack(C customPack) throws IOException;

    /**
     * Is called before this resource is installed to the pack
     * @param customPack The customPack
     * @throws IOException when something fails
     */
    void beforeResourceInstallation(C customPack) throws IOException;

    /**
     * Is called after this resource is installed to the pack
     * @param customPack The customPack
     * @throws IOException when something fails
     */
    void afterResourceInstallation(C customPack) throws IOException;

    /**
     * Called when the resource is registered to a custom pack
     * @param customPack The custom pack
     */
    void onRegister(C customPack);
}

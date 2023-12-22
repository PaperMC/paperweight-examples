package de.verdox.mccreativelab.generator;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class AbstractResource<C extends CustomPack<C>> implements Resource<C> {
    private final NamespacedKey namespacedKey;

    public AbstractResource(NamespacedKey namespacedKey) {
        this.namespacedKey = namespacedKey;
    }

    @Override
    public void onRegister(C customPack) {

    }

    @Override
    public void beforeResourceInstallation(C customPack) throws IOException {

    }

    @Override
    public void afterResourceInstallation(C customPack) throws IOException {

    }

    @Override
    public final @NotNull NamespacedKey key() {
        return namespacedKey;
    }
}

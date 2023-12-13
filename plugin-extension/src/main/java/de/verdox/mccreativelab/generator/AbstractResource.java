package de.verdox.mccreativelab.generator;

import org.bukkit.NamespacedKey;

public abstract class AbstractResource<C extends CustomPack<C>> implements Resource<C> {
    private final NamespacedKey namespacedKey;

    public AbstractResource(NamespacedKey namespacedKey) {
        this.namespacedKey = namespacedKey;
    }

    @Override
    public void onRegister(C customPack) {

    }

    @Override
    public final NamespacedKey key() {
        return namespacedKey;
    }
}

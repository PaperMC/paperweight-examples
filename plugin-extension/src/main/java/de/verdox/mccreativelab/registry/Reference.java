package de.verdox.mccreativelab.registry;

import org.bukkit.NamespacedKey;

import java.util.Objects;

public interface Reference<T> {
    NamespacedKey getKey();
    CustomRegistry<T> getRegistry();
    T unwrapValue();
    static <T> Reference<T> create(CustomRegistry<T> registry, NamespacedKey namespacedKey){
        Objects.requireNonNull(registry);
        Objects.requireNonNull(namespacedKey);
        return new ReferenceImpl<>(registry, namespacedKey);
    }
}

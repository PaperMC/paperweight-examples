package de.verdox.mccreativelab.registry;

import org.bukkit.NamespacedKey;

public class OpenRegistry<T> extends CustomRegistry<T> {
    private final String namespace;

    public OpenRegistry(String namespace){
        this.namespace = namespace;
    }

    public  <S extends T> Reference<S> register(String key, S data) {
        return this.register(new NamespacedKey(namespace, key), data);
    }

    @Override
    protected <S extends T> Reference<S> register(NamespacedKey namespacedKey, S data) {
        return super.register(namespacedKey, data);
    }
}

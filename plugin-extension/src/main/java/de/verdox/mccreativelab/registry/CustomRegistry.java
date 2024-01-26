package de.verdox.mccreativelab.registry;

import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CustomRegistry<T> {
    private final AtomicInteger idCounter = new AtomicInteger();
    private final Map<NamespacedKey, T> registry = new HashMap<>();
    private final Map<T, NamespacedKey> dataToKeyMapping = new HashMap<>();
    private final Map<NamespacedKey, Integer> keyToId = new HashMap<>();
    private final Map<Integer, NamespacedKey> idToKey = new HashMap<>();
    private boolean freeze;
    protected <S extends T> Reference<S> register(NamespacedKey namespacedKey, S data){
        if(freeze)
            throw new IllegalStateException("Registry already frozen");
        int id = idCounter.getAndIncrement();
        checkForDuplicates(namespacedKey, data);
        registry.put(namespacedKey, data);
        keyToId.put(namespacedKey, id);
        idToKey.put(id, namespacedKey);
        dataToKeyMapping.put(data, namespacedKey);

        return (Reference<S>) Reference.create(this, namespacedKey);
    }

    public void freeze(){
        onFreeze();
    }

    protected void onFreeze(){}

    public boolean isEmpty(){
        return registry.isEmpty();
    }

    public Iterator<T> values(){
        return registry.values().iterator();
    }

    public Iterator<NamespacedKey> keys(){
        return registry.keySet().iterator();
    }

    public NamespacedKey getKey(T data) {
        return dataToKeyMapping.get(data);
    }

    @Nullable
    public NamespacedKey getKey(int id) {
        return idToKey.getOrDefault(id, null);
    }

    public int getId(NamespacedKey namespacedKey) {
        return keyToId.getOrDefault(namespacedKey, -1);
    }

    public T get(NamespacedKey namespacedKey) {
        return registry.get(namespacedKey);
    }

    public @Nullable T get(int id) {
        NamespacedKey key = getKey(id);
        if(key == null)
            return null;
        return registry.get(key);
    }

    protected void checkForDuplicates(NamespacedKey namespacedKey, T data) {
        if (registry.containsKey(namespacedKey))
            throw new IllegalStateException(data.getClass()
                                                .getSimpleName() + " with key " + namespacedKey.toString() + " already registered");
        if (dataToKeyMapping.containsKey(data))
            throw new IllegalStateException(data.getClass()
                                                .getSimpleName() + " already registered with key " + dataToKeyMapping
                .get(data).toString());
    }

}

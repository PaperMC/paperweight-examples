package de.verdox.mccreativelab.registry.palette;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IdMap<T> {
    private int nextId = 1;
    private final Object2IntMap<T> dataToId;
    private final List<T> idToData;

    public IdMap() {
        this(512, IdentityStrategy.INSTANCE);
    }

    public IdMap(Hash.Strategy<Object> strategy) {
        this(512, strategy);
    }

    public IdMap(int initialSize) {
        this(initialSize, IdentityStrategy.INSTANCE);
    }

    public IdMap(int initialSize, Hash.Strategy<Object> strategy) {
        this.idToData = Lists.newArrayListWithExpectedSize(initialSize);
        this.dataToId = new Object2IntOpenCustomHashMap<>(initialSize, strategy);
        this.dataToId.defaultReturnValue(-1);
    }

    public void addMapping(T value, int id) {
        this.dataToId.put(value, id);

        while (this.idToData.size() <= id)
            this.idToData.add(null);

        this.idToData.set(id, value);
        if (this.nextId <= id)
            this.nextId = id + 1;
    }

    public void add(T value) {
        this.addMapping(value, this.nextId);
    }

    public int getId(T value) {
        return this.dataToId.getInt(value);
    }

    @Nullable
    public T byId(int index) {

        return (T) (index >= 0 && index < this.idToData.size() ? this.idToData.get(index) : null);
    }

    public Iterator<T> iterator() {
        return Iterators.filter(this.idToData.iterator(), Objects::nonNull);
    }

    public boolean contains(int index) {
        return this.byId(index) != null;
    }

    public int size() {
        return this.dataToId.size();
    }

    public enum IdentityStrategy implements Hash.Strategy<Object> {
        INSTANCE;

        @Override
        public int hashCode(Object object) {
            return System.identityHashCode(object);
        }

        @Override
        public boolean equals(Object object, Object object2) {
            return object == object2;
        }
    }
}

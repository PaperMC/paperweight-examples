package de.verdox.mccreativelab.util.storage;

import de.verdox.mccreativelab.util.storage.palette.IdMap;
import it.unimi.dsi.fastutil.Pair;

import java.util.*;
import java.util.stream.Stream;

public class HashedThreeDimensionalStorage<K extends Number, T> extends ThreeDimensionalStorage<K, T> {
    private final Map<K, Integer> indexToDataMapping = new HashMap<>();
    private final Map<Integer, Set<K>> dataToIndexMapping = new HashMap<>();

    public HashedThreeDimensionalStorage(IdMap<T> idMap, IndexingStrategy<K> indexingStrategy) {
        super(idMap, indexingStrategy);
    }

    @Override
    protected void setDataInternal(K index, int dataID) {
        indexToDataMapping.put(index, dataID);
        dataToIndexMapping.computeIfAbsent(dataID, integer -> new HashSet<>()).add(index);
    }

    @Override
    protected void removeDataInternal(K index) {
        if (!hasDataInternal(index))
            return;
        int data = indexToDataMapping.get(index);
        indexToDataMapping.remove(index);
        if (dataToIndexMapping.containsKey(data)) {
            Set<K> storedIndizes = dataToIndexMapping.get(data);
            storedIndizes.remove(index);
            if (storedIndizes.isEmpty())
                dataToIndexMapping.remove(data);
        }
    }

    @Override
    protected Stream<Pair<K, Integer>> streamEntries() {
        return indexToDataMapping.entrySet().stream().map(kIntegerEntry -> Pair.of(kIntegerEntry.getKey(), kIntegerEntry.getValue()));
    }

    @Override
    protected Map<Integer, Set<K>> getDataToIndizesMappingInternal() {
        return Map.copyOf(dataToIndexMapping);
    }

    @Override
    protected int getDataInternal(K index) {
        if (!hasDataInternal(index))
            throw new NoSuchElementException("No element found for index " + index);
        return indexToDataMapping.get(index);
    }

    @Override
    protected boolean hasDataInternal(K index) {
        return indexToDataMapping.containsKey(index);
    }
}

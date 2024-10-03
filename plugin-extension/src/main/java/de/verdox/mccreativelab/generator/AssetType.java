package de.verdox.mccreativelab.generator;

import java.util.Objects;

public record AssetType<C extends CustomPack<C>> (AssetPath resourceTypePath) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetType<?> assetType = (AssetType<?>) o;
        return Objects.equals(resourceTypePath, assetType.resourceTypePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceTypePath);
    }

    @Override
    public String toString() {
        return "AssetType{" +
            "resourceTypePath=" + resourceTypePath +
            '}';
    }
}

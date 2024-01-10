package de.verdox.mccreativelab.generator;

public record AssetType<C extends CustomPack<C>> (AssetPath resourceTypePath) {

    @Override
    public String toString() {
        return "AssetType{" +
            "resourceTypePath=" + resourceTypePath +
            '}';
    }
}

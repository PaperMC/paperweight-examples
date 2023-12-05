package de.verdox.mccreativelab.generator;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Path;

public interface AssetPath {
    /**
     * Returns the full path of this asset
     * @return The path
     */
    Path toPath();

    /**
     * Returns the parent path of this asset
     * @return The parent path
     */
    AssetPath getParentPath();

    /**
     * Returns the path as array of strings
     * @return The array
     */
    String[] getPath();

    /**
     * Returns the file ending of this asset if one was provided.
     * @return The file ending if this path points to a file.
     * If this path points to a directory this method returns null.
     */
    @Nullable
    String getFileEndingIfAvailable();

    /**
     * Returns a path but with a new parent
     * @param parentPath The parent
     * @return The new path
     */
    AssetPath withNewParentPath(AssetPath parentPath);

    /**
     * Returns a path but with a new parent
     * @param parentPath The parent
     * @return The new path
     */
    default AssetPath withNewParentPath(String... parentPath){
        return withNewParentPath(buildPath(parentPath));
    }

    /**
     * Returns a new path with another path concatenated to its end
     * @param concat The other path
     * @return The new path
     */
    default AssetPath concatPath(String... concat){
        return withNewParentPath(buildPath(concat));
    }
    /**
     * Returns a new path with another path concatenated to its end
     * @param concat The other path
     * @return The new path
     */
    AssetPath concatPath(AssetPath concat);

    /**
     * Tries to get data from this asset path from the projects resources
     * @return The InputStream
     */
    @Nullable
    default InputStream asResourceStream() {
        return getClass().getClassLoader().getResourceAsStream(toPath().toString());
    }

    static AssetPath buildPath(String... array) {
        return buildPath(null, array);
    }

    static AssetPath buildPath(AssetPath parent, String... array) {
        return new AssetPathImpl(parent, array);
    }
}

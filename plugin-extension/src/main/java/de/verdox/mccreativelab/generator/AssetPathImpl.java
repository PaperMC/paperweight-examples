package de.verdox.mccreativelab.generator;

import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AssetPathImpl implements AssetPath {
    private final AssetPath parentPath;
    private final String[] path;
    private final String[] fullPath;

    AssetPathImpl(AssetPath parentPath, String... path) {
        this.parentPath = parentPath;
        this.path = splitFileSeperators(path);
        if (this.parentPath != null) {
            fullPath = Stream.concat(Arrays.stream(parentPath.getPath()), Arrays.stream(this.path))
                             .toArray(String[]::new);
        } else
            fullPath = this.path;
    }

    @Override
    public Path toPath() {
        if (parentPath == null)
            return Path.of(String.join(File.separator, path));
        else
            return Path.of(parentPath.toPath() + File.separator + String.join(File.separator, path));
    }

    @Override
    public AssetPath getParentPath() {
        return parentPath;
    }

    @Override
    public String[] getPath() {
        return Arrays.copyOf(fullPath, fullPath.length);
    }

    @Nullable
    @Override
    public String getFileEndingIfAvailable() {
        String extension = FileUtils.extension(fullPath[fullPath.length-1]);
        return extension.equals("") ? null : extension;
    }

    @Override
    public String toString() {
        return toPath().toString();
    }

    @Override
    public AssetPath withNewParentPath(AssetPath parentPath) {
        return new AssetPathImpl(parentPath, fullPath);
    }

    @Override
    public AssetPath concatPath(String... concat) {
        var newPath = Arrays.copyOf(path, path.length + concat.length);
        if (newPath.length - path.length >= 0)
            System.arraycopy(concat, 0, newPath, path.length, newPath.length - path.length);
        return AssetPath.buildPath(parentPath, newPath);
    }

    @Override
    public AssetPath concatPath(AssetPath concat) {
        return concatPath(concat.getPath());
    }

    private static String[] splitFileSeperators(String[] inputArray) {
        // Neue Liste für das Ergebnis erstellen
        List<String> resultList = new ArrayList<>();

        // Durch jedes Element im Eingabe-Array iterieren
        for (String element : inputArray) {
            // Überprüfen, ob der Dateiseparator im String enthalten ist
            if (element.contains("/")) {
                // Wenn ja, den String nach dem Dateiseparator aufteilen und zur Liste hinzufügen
                String[] parts = element.split("/");
                resultList.addAll(Arrays.asList(parts));
            } else {
                // Andernfalls das Element einfach zur Liste hinzufügen
                resultList.add(element);
            }
        }

        // Liste in ein Array umwandeln und zurückgeben
        return resultList.toArray(new String[0]);
    }
}

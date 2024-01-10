package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.AssetType;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ResourcePackScanner {
    private final Map<AssetType<CustomResourcePack>, Map<NamespacedKey, AssetBasedResourcePackResource>> foundAssets = new HashMap<>();
    private final Set<AssetBasedResourcePackResource> resources = new HashSet<>();
    private final Path packMainFolder;

    public ResourcePackScanner(Path packMainFolder) {
        this.packMainFolder = packMainFolder;
    }

    public void scan() throws IOException {
        if (!Path.of(packMainFolder + "/pack.mcmeta").toFile().exists()) {
            Bukkit.getLogger().info(packMainFolder + " is not a resource pack");
            return;
        }
        Bukkit.getLogger().info("Scanning resource pack located at " + packMainFolder);
        Path assetPaths = Path.of(packMainFolder + "/assets");
        try (Stream<Path> fileStream = Files.walk(assetPaths)) {
            fileStream.forEachOrdered(path -> {
                if (!path.toFile().isFile())
                    return;
                String[] pathSplit = getRelativePath(assetPaths.toString(), path.toString()).split(Pattern.quote(File.separator));
                String namespace = pathSplit[0];
                String type = pathSplit[1];
                int indexOfType = path.toString().indexOf(type);
                AssetType<CustomResourcePack> resourceType = new AssetType<>(AssetPath.buildPath(pathSplit[1]));
                String key = path.toString()
                                 .substring(indexOfType + type.length() + 1)
                                 .split("\\.")[0].replace("\\", "/");

                Asset<CustomResourcePack> asset = new Asset<>(() -> {
                    try {
                        return new FileInputStream(path.toFile());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }, false);

                AssetBasedResourcePackResource assetBasedResourcePackResource = new AssetBasedResourcePackResource(new NamespacedKey(namespace, key), asset, resourceType, FileUtils.extension(path.toFile().getName()));
                foundAssets.computeIfAbsent(resourceType, assetType -> new HashMap<>()).put(new NamespacedKey(namespace, key), assetBasedResourcePackResource);
                resources.add(assetBasedResourcePackResource);
            });
        }
    }

    public Set<AssetBasedResourcePackResource> getResources() {
        return resources;
    }

    private String getRelativePath(String parentPath, String fullAssetPath) {
        if (!fullAssetPath.startsWith(parentPath)) {
            // Der vollständige Pfad sollte mit dem Basispfad beginnen
            return "Ungültige Pfade";
        }

        // Entferne den Basispfad und das Trennzeichen am Anfang
        return fullAssetPath.substring(parentPath.length() + 1);
    }
}

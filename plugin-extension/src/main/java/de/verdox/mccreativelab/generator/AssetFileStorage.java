package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class AssetFileStorage {
    private static final Map<String, AssetFileStorage> map = new HashMap<>();

    public static AssetFileStorage get(Plugin javaPlugin) {
        return map.computeIfAbsent(javaPlugin.getName(), AssetFileStorage::new);
    }

    public static AssetFileStorage get(String pluginName) {
        return map.computeIfAbsent(pluginName, AssetFileStorage::new);
    }

    private final File pluginDataFolder;

    private AssetFileStorage(Plugin javaPlugin) {
        this.pluginDataFolder = javaPlugin.getDataFolder();
    }

    private AssetFileStorage(String pluginName) {
        this.pluginDataFolder = new File(Bukkit.getPluginsFolder() + "/" + pluginName);
    }

    public AssetBasedResourcePackResource loadAsset(NamespacedKey namespacedKey, AssetType<CustomResourcePack> resourcePackAssetType, String fileEnding) {
        File assetPath = new File(pluginDataFolder + "/assets/" + resourcePackAssetType.resourceTypePath().toPath() + "/" + namespacedKey.namespace() + "/" + namespacedKey.value() + "." + fileEnding);
        Asset<CustomResourcePack> asset = new Asset<>(() -> {
            try {
                return new FileInputStream(assetPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        });
        return new AssetBasedResourcePackResource(namespacedKey, asset, resourcePackAssetType, fileEnding);
    }
}

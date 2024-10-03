package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.util.FileUtil;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record Asset<C extends CustomPack<C>>(Supplier<InputStream> assetInputStream, boolean installIfExists,
                                             @Nullable Consumer<File> installationCallback) {
    public Asset(Supplier<InputStream> assetInputStream) {
        this(assetInputStream, true, null);
    }

    public Asset(Supplier<InputStream> assetInputStream, boolean installIfExists) {
        this(assetInputStream, installIfExists, null);
    }

    public Asset(String resourcePath, @Nullable Consumer<File> installationCallback) {
        this(() -> Asset.class.getResourceAsStream(resourcePath), true, installationCallback);
    }

    public Asset(String resourcePath, boolean installIfExists, @Nullable Consumer<File> installationCallback) {
        this(() -> Asset.class.getResourceAsStream(resourcePath), installIfExists, installationCallback);
    }

    public Asset(String resourcePath) {
        this(() -> Asset.class.getResourceAsStream(resourcePath), true, null);
    }

    public Asset(String resourcePath, boolean installIfExists) {
        this(() -> Asset.class.getResourceAsStream(resourcePath), installIfExists, null);
    }

    public Asset(File assetFile, @Nullable Consumer<File> installationCallback) {
        this(() -> {
            try {
                return new FileInputStream(assetFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }, true, installationCallback);
    }

    public Asset(File assetFile) {
        this(assetFile, null);
    }

    public Asset<C> withCallback(@org.jetbrains.annotations.Nullable Consumer<File> installCallback) {
        return new Asset<>(assetInputStream, installIfExists, installCallback);
    }

    public Asset<C> withInstallFlag(boolean installIfExists) {
        return new Asset<>(assetInputStream, installIfExists, installationCallback);
    }

    public boolean isInputStreamValid() {
        if (assetInputStream == null || assetInputStream.get() == null)
            return false;
        try (InputStream stream = assetInputStream.get()) {
            return stream != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public File installAsset(C customPack, NamespacedKey namespacedKey, AssetType<C> assetType, String fileEnding) throws IOException {
        if (!fileEnding.contains("."))
            fileEnding = "." + fileEnding;
        if (!isInputStreamValid())
            throw new IOException("InputStream of asset is not valid for asset "+namespacedKey+" with type "+assetType);
        try (InputStream stream = assetInputStream.get()) {

            if (stream == null) {
                Bukkit.getLogger().info("InputStream of asset " + namespacedKey + " is empty");
                return null;
            }


            AssetPath savePath = getPathRelativeToPack(customPack, namespacedKey, assetType).withNewParentPath(customPack.getPathToSavePackDataTo());
            savePath.toPath().getParent().toFile().mkdirs();
            Path pathToCopyTo = Path.of(savePath.toPath() + fileEnding);
            if (installIfExists || !pathToCopyTo.toFile().exists())
                Files.copy(stream, pathToCopyTo, StandardCopyOption.REPLACE_EXISTING);
            else
                Bukkit.getLogger().info("Could not install "+namespacedKey+" cause it already exists");
            if (installationCallback != null)
                installationCallback.accept(pathToCopyTo.toFile());

            AssetPath pathOfAssetFromLastInstallation = getPathRelativeToPack(customPack, namespacedKey, assetType).withNewParentPath(customPack.getPathOfOldPack());
/*            Path oldFilePath = Path.of(pathOfAssetFromLastInstallation.toPath() + fileEnding);
            if(!pathToCopyTo.toFile().exists() || !oldFilePath.toFile().exists() || !areFilesEqual(oldFilePath, pathToCopyTo)) {
                //System.out.println(savePath.toPath() + " has changed!");
            }*/

            return Path.of(savePath.toPath() + fileEnding).toFile();
        }
    }

    public static <C extends CustomPack<C>> AssetPath getPathRelativeToPack(C customPack, NamespacedKey namespacedKey, AssetType<C> assetType) {
        return AssetPath.buildPath(customPack.mainFolder())
                        .concatPath(namespacedKey.namespace())
                        .concatPath(assetType.resourceTypePath())
                        .concatPath(namespacedKey.getKey());
    }

    private boolean areFilesEqual(Path current, Path old) throws IOException {
        if (!Files.exists(current) || !Files.exists(old)) {
            return false;
        }

        try (InputStream is1 = Files.newInputStream(current);
             InputStream is2 = Files.newInputStream(old)) {

            int byte1 = 0, byte2 = 0;
            while ((byte1 = is1.read()) != -1 && (byte2 = is2.read()) != -1) {
                if (byte1 != byte2) {
                    return false;
                }
            }
            // Check if the second file has more bytes
            return is2.read() == -1;
        }
    }
}

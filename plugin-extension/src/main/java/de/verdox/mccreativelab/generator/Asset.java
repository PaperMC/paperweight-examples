package de.verdox.mccreativelab.generator;

import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record Asset<C extends CustomPack<C>>(Supplier<InputStream> assetInputStream, @Nullable Consumer<File> installationCallback) {
    public Asset(Supplier<InputStream> assetInputStream) {
        this(assetInputStream, null);
    }

    public Asset(File assetFile,  @Nullable Consumer<File> installationCallback) {
        this(() -> {
            try {
                return new FileInputStream(assetFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }, installationCallback);
    }

    public Asset(File assetFile){
        this(assetFile, null);
    }

    public Asset<C> withCallback(@org.jetbrains.annotations.Nullable Consumer<File> installCallback) {
        return new Asset<>(assetInputStream, installCallback);
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
        if(!fileEnding.contains("."))
            fileEnding = "."+fileEnding;
        if (!isInputStreamValid())
            throw new IllegalStateException("InputStream of asset is not valid");
        try (InputStream stream = assetInputStream.get()) {

            if (stream == null)
                return null;

            AssetPath savePath = getPathRelativeToPack(customPack, namespacedKey, assetType).withNewParentPath(customPack.getPathToSavePackDataTo());
            savePath.toPath().getParent().toFile().mkdirs();
            Files.copy(stream, Path.of(savePath.toPath() + fileEnding), StandardCopyOption.REPLACE_EXISTING);
            if (installationCallback != null)
                installationCallback.accept(savePath.toPath().toFile());
            return Path.of(savePath.toPath() + fileEnding).toFile();
        }
    }

    public static<C extends CustomPack<C>> AssetPath getPathRelativeToPack(C customPack, NamespacedKey namespacedKey, AssetType<C> assetType){
        return AssetPath.buildPath(customPack.mainFolder())
            .concatPath(namespacedKey.namespace())
            .concatPath(assetType.resourceTypePath())
            .concatPath(namespacedKey.getKey());
    }
}

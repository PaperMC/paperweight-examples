package de.verdox.mccreativelab.util.io;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetType;
import de.verdox.mccreativelab.generator.CustomPack;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import org.bukkit.NamespacedKey;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class AssetUtil {
    public static<C extends CustomPack<C>> void createJsonAssetAndInstall(JsonObject jsonObject, C customPack, NamespacedKey namespacedKey, AssetType<C> assetType){
        try {
            File tempFile = Files.createTempFile(UUID.randomUUID().toString(), "json").toFile();
            JsonUtil.writeJsonObjectToFile(jsonObject, tempFile);

            Asset<C> modelAsset = new Asset<>(tempFile);
            modelAsset.installAsset(customPack, namespacedKey, assetType, "json");
            tempFile.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <C extends CustomPack<C>> void installJsonFileAssetAndEdit(Asset<C> asset, C customPack, NamespacedKey namespacedKey, AssetType<C> assetType, Consumer<JsonObject> editor) throws IOException {
        File installedFile = asset.installAsset(customPack, namespacedKey, assetType, "json");
        JsonObject jsonObject = JsonUtil.readJsonFromFile(installedFile);
        editor.accept(jsonObject);
        JsonUtil.writeJsonObjectToFile(jsonObject, installedFile);
    }
    public static List<Asset<CustomResourcePack>> createPartlyVisibleCopys(Asset<CustomResourcePack> asset, int amountParts) throws IOException {
        var fileList = new LinkedList<Asset<CustomResourcePack>>();

        try(InputStream stream = asset.assetInputStream().get()){
            if(stream == null)
                throw new IOException("InputStream supplier did not contain any InputStream");
            var split = ImageUtil.splitImage(stream, amountParts);

            for (java.awt.image.BufferedImage partImage : split) {
                var byteStream = new ByteArrayOutputStream();
                ImageIO.write(partImage, "png", byteStream);

                Asset<CustomResourcePack> partImageAsset = new Asset<CustomResourcePack>(() -> new ByteArrayInputStream(byteStream.toByteArray()));
                fileList.add(partImageAsset);
            }
            return fileList;
        }
    }

    public static int getPixelHeightIfImage(Asset<?> asset) {
        try {
            return ImageIO.read(asset.assetInputStream().get()).getHeight();
        } catch (IOException e) {
            return -1;
        }
    }

    public static boolean isImage(Asset<?> asset) {
        try {
            var image = ImageIO.read(asset.assetInputStream().get());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    public static int getPixelWidthIfImage(Asset<?> asset) {
        try {
            return ImageIO.read(asset.assetInputStream().get()).getHeight();
        } catch (IOException e) {
            return -1;
        }
    }
}

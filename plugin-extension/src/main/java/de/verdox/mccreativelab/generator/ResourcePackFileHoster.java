package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.util.io.ZipUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ResourcePackFileHoster implements Handler<HttpServerRequest> {
    private final HttpServer httpServer;
    private final Map<String, ResourcePackInfo> availableResourcePacks = new HashMap<>();
    private final String hostname;
    private final int port;
    private ResourcePackInfo requiredResourcePack;
    public ResourcePackFileHoster(String hostname, int port) {
        Bukkit.getLogger().warning("Starting ResourcePackFileHoster");
        this.hostname = hostname;
        this.port = port;
        this.httpServer = Vertx.vertx().createHttpServer();
        this.httpServer.exceptionHandler(event -> Bukkit.getLogger().warning("Exception happened in ResourcePackFileHoster"));
        this.httpServer.requestHandler(this);
        this.httpServer.listen(port, hostname);
    }

    public void closeAndWait() throws InterruptedException {
        Future.await(this.httpServer.close());
    }

    @Override
    public void handle(HttpServerRequest event) {
        var split = event.absoluteURI().split("/");
        if(split.length == 0)
            return;
        String hash = split[split.length - 1];
        ResourcePackInfo resourcePackInfo = availableResourcePacks.getOrDefault(hash, null);
        if (resourcePackInfo == null)
            return;
        event.response().sendFile(resourcePackInfo.file.getAbsolutePath());
    }

    //TODO: Do this on server reload aswell. Makes quick changes possible
    public void createResourcePackZipFiles() throws IOException {
        deleteZipFiles();
        Files.walk(CustomResourcePack.resourcePacksFolder.toPath(), 1).forEach(path -> {
            if (path.equals(CustomResourcePack.resourcePacksFolder.toPath()))
                return;
            if (!path.toFile().isDirectory())
                return;
            File resourcePackParentFolder = path.toFile();
            String resourcePackName = resourcePackParentFolder.getName();
            boolean required = resourcePackName.equals("MCCreativeLabResourcePack");
            File zipFile = Path.of(resourcePackParentFolder.getPath() + ".zip").toFile();

            ZipUtil.zipFolder(resourcePackParentFolder.toPath(), Path.of(resourcePackParentFolder.getPath() + ".zip"));
            try {
                String hash = calculateSHA1(zipFile.getPath());
                ResourcePackInfo resourcePackInfo = new ResourcePackInfo(resourcePackName, zipFile, createDownloadUrl(hash), hash, required, null);
                availableResourcePacks.put(hash, resourcePackInfo);
                if(required)
                    requiredResourcePack = resourcePackInfo;
                Bukkit.getLogger().info("MCCreativeLab: Found ResourcePack " + zipFile.getName());
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String createDownloadUrl(String hash) {
        return "http://" + this.hostname + ":" + this.port + "/" + hash;
    }

    private void deleteZipFiles() throws IOException {
        availableResourcePacks.clear();
        Files.walk(CustomResourcePack.resourcePacksFolder.toPath(), 1).forEach(path -> {
            if (path.equals(CustomResourcePack.resourcePacksFolder.toPath()))
                return;
            if (!FileUtils.extension(path.toFile().getName()).equals("zip"))
                return;
            path.toFile().delete();
        });
    }

    private String calculateSHA1(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (FileInputStream fis = new FileInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(fis, digest)) {

            // Leere den Stream, um den Hash zu berechnen
            while (dis.read() != -1) {
                // Nichts zu tun hier; liest einfach den Stream und aktualisiert den Digest
            }
        }

        // Konvertiere den Hash zu einem Hex-String
        byte[] hashBytes = digest.digest();
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte hashByte : hashBytes) {
            hexStringBuilder.append(String.format("%02x", hashByte));
        }

        return hexStringBuilder.toString();
    }

    public record ResourcePackInfo(String resourcePackName, File file, String url, String hash, boolean isRequired, @javax.annotation.Nullable Component prompt) {
    }

    public @Nullable ResourcePackInfo getRequiredResourcePack() {
        return requiredResourcePack;
    }
}

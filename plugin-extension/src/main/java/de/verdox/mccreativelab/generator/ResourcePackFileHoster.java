package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.util.io.ZipUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ResourcePackFileHoster implements Handler<HttpServerRequest>, Listener {
    private final HttpServer httpServer;
    private final Map<String, ResourcePackInfo> availableResourcePacks = new HashMap<>();
    private final String hostname;
    private final int port;
    private final boolean requireResourcePack;

    public ResourcePackFileHoster() throws IOException, InvalidConfigurationException {

        File configFile = new File(MCCreativeLabExtension.getInstance().getDataFolder()+"/config.yml");
        configFile.getParentFile().mkdirs();
        FileConfiguration config = MCCreativeLabExtension.getInstance().getConfig();

        if(configFile.isFile())
            config.load(configFile);

        config.options().copyDefaults(true);
        config.addDefault("hostName", "0.0.0.0");
        config.addDefault("port", 8080);
        config.addDefault("require", true);
        config.save(configFile);

        this.hostname = config.getString("hostName", "0.0.0.0");
        this.port = config.getInt("port", 8080);
        this.requireResourcePack = config.getBoolean("require");
        Bukkit.getLogger().info("Starting ResourcePackFileHoster on " + hostname + ":" + port);
        this.httpServer = Vertx.vertx().createHttpServer();
        this.httpServer.exceptionHandler(event -> Bukkit.getLogger().warning("Exception happened in ResourcePackFileHoster: " + event.getLocalizedMessage()));
        this.httpServer.requestHandler(this);
        this.httpServer.listen(port, hostname);
    }

    public void closeAndWait() throws InterruptedException {
        this.httpServer.close().result();
    }


    @Override
    public void handle(HttpServerRequest event) {
        try{
            var split = event.absoluteURI().split("/");
            if (split.length == 0) {
                event.response().end();
                return;
            }

            String hash = split[split.length - 1];
            if(!isValidHex(hash)){
                Bukkit.getLogger().warning("Received a request from "+event.remoteAddress()+ " that does not seem to be a hash: "+hash);
                event.response().end();
                return;
            }
            ResourcePackInfo resourcePackInfo = availableResourcePacks.getOrDefault(hash, null);
            if (resourcePackInfo == null) {
                Bukkit.getLogger().warning("Someone requested a resource pack with hash " + hash + " that does not exist");
                event.response().end();
                return;
            }
            event.response().sendFile(resourcePackInfo.file.getAbsolutePath());
            Bukkit.getLogger().info("Sending resource pack with hash " + hash+ " to "+event.remoteAddress());
        }
        finally {

        }
    }

    private static boolean isValidHex(String input) {
        // Regular expression for hexadecimal string
        String hexPattern = "^[0-9A-Fa-f]+$";

        // Compile the pattern
        Pattern pattern = Pattern.compile(hexPattern);

        // Match the input against the pattern
        Matcher matcher = pattern.matcher(input);

        // Return true if the input matches the pattern, false otherwise
        return matcher.matches();
    }

    public void createResourcePackZipFiles() throws IOException {
        deleteZipFiles();

        try (Stream<Path> files = Files.walk(CustomResourcePack.resourcePacksFolder.toPath(), 1)) {
            files.parallel().forEach(path -> {
                if (path.equals(CustomResourcePack.resourcePacksFolder.toPath()))
                    return;
                if (!path.toFile().isDirectory())
                    return;
                File resourcePackParentFolder = path.toFile();
                String resourcePackName = resourcePackParentFolder.getName();
                File zipFile = Path.of(resourcePackParentFolder.getPath() + ".zip").toFile();
                if (!resourcePackName.equals("MCCreativeLab"))
                    return;

                Path zipPath = Path.of(resourcePackParentFolder.getPath() + ".zip");
                long start = System.currentTimeMillis();
                ZipUtil.zipFolder(resourcePackParentFolder.toPath(), zipPath);
                long end = System.currentTimeMillis() - start;
                Bukkit.getLogger().info("Created Zip file "+zipFile+" in "+end+" ms");
                try {
                    Bukkit.getLogger().info("Calculating sha1 hash of resource pack");
                    start = System.currentTimeMillis();
                    byte[] hashBytes = calculateSHA1(zipFile.getPath());
                    String hash = calculateSHA1String(hashBytes);
                    end = System.currentTimeMillis() - start;
                    Bukkit.getLogger().info("Took "+end+" ms");

                    ResourcePackInfo resourcePackInfo = new ResourcePackInfo(resourcePackName, zipFile, createDownloadUrl(hash), hash, hashBytes, true, null);
                    availableResourcePacks.put(hash, resourcePackInfo);
                    Bukkit.getLogger()
                          .info("MCCreativeLab: Hosting ResourcePack " + zipFile.getName() + " with hash: " + hash);
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            });
        }
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
            Bukkit.getLogger().info("Deleting ResourcePack file " + path.toFile().getName());
            long start = System.currentTimeMillis();
            path.toFile().delete();
            long end = System.currentTimeMillis() - start;
            Bukkit.getLogger().info("ResourcePack file " + path.toFile().getName() + " deleted in " + end + " ms");
        });
    }

    private String calculateSHA1String(byte[] hashBytes) throws IOException, NoSuchAlgorithmException {
        // Konvertiere den Hash zu einem Hex-String
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte hashByte : hashBytes) {
            hexStringBuilder.append(String.format("%02x", hashByte));
        }

        return hexStringBuilder.toString();
    }

    private byte[] calculateSHA1(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (FileInputStream fis = new FileInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(fis, digest)) {

            // Leere den Stream, um den Hash zu berechnen
            while (dis.read() != -1) {
                // Nichts zu tun hier; liest einfach den Stream und aktualisiert den Digest
            }
        }

        // Konvertiere den Hash zu einem Hex-String
        return digest.digest();
    }

    public record ResourcePackInfo(String resourcePackName, File file, String url, String hash, byte[] hashBytes,
                                   boolean isRequired, @javax.annotation.Nullable Component prompt) {
        public UUID getUUID() {
            return UUID.nameUUIDFromBytes(resourcePackName.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void sendDefaultResourcePackToPlayer(Player player) {
        availableResourcePacks.forEach((s, resourcePackInfo) -> {
            if (resourcePackInfo.isRequired) sendResourcePackToPlayer(player, resourcePackInfo);
        });
    }

    public void sendDefaultResourcePackToPlayers(Collection<? extends Player> players) {
        players.forEach(this::sendDefaultResourcePackToPlayer);
    }

    public void sendResourcePackToPlayer(Player player, ResourcePackFileHoster.ResourcePackInfo packInfo) {
        String downloadURL = MCCreativeLabExtension.getResourcePackFileHoster().createDownloadUrl(packInfo.hash());

        player.setResourcePack(packInfo.getUUID(), downloadURL, packInfo.hashBytes(), (Component) null, requireResourcePack);
    }

    @EventHandler
    public void kickPlayersIfResourcePackWasNotAppliedButIsRequired(PlayerResourcePackStatusEvent e) {
        switch (e.getStatus()) {
            case DECLINED, FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL, DISCARDED -> {
                if(requireResourcePack) e.getPlayer().kick(null, PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
            }
        }
    }

    @EventHandler
    public void applyRequiredResourcePackOnJoin(PlayerJoinEvent e) {
        Bukkit.getLogger().info("Sending resource pack to "+e.getPlayer());
        sendDefaultResourcePackToPlayer(e.getPlayer());
    }
}

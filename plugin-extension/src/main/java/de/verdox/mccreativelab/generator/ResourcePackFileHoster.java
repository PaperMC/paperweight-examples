package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.config.ConfigValue;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.util.io.ZipUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import net.kyori.adventure.text.Component;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.packs.ResourcePack;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ResourcePackFileHoster implements Listener {
    public static final boolean WITH_HASHES = false;
    private HttpServer httpServer;
    private final Map<String, ResourcePackInfo> availableResourcePacks = new HashMap<>();

    private final ConfigValue.Enum<Mode> mode;
    private final InternalWebServerSettings internalWebServerSettings;
    private final SshUploadSettings sshUploadSettings;
    private final ConfigValue.Boolean requireResourcePack;
    private final ConfigValue.Boolean useHttps;
    private final ConfigValue.String downloadUrl;
    private WebServerHandler webServerHandler;
    private SshResourcePackUpload sshResourcePackUpload;

    public ResourcePackFileHoster() throws IOException, InvalidConfigurationException {
        File configFile = new File(MCCreativeLabExtension.getInstance().getDataFolder() + "/config.yml");
        configFile.getParentFile().mkdirs();
        FileConfiguration config = MCCreativeLabExtension.getInstance().getConfig();

        if (configFile.isFile())
            config.load(configFile);

        config.options().copyDefaults(true);

        mode = new ConfigValue.Enum<>(config, Mode.class, "mode", Mode.INTERNAL_WEBSERVER);
        internalWebServerSettings = new InternalWebServerSettings(config);
        sshUploadSettings = new SshUploadSettings(config);

        this.downloadUrl = new ConfigValue.String(config, "download.url", "0.0.0.0:8080");
        this.useHttps = new ConfigValue.Boolean(config, "download.https", false);
        this.requireResourcePack = new ConfigValue.Boolean(config, "requireResourcePackOnJoin", true);

        config.save(configFile);

        if (mode.read().equals(Mode.INTERNAL_WEBSERVER)) {
            Bukkit.getLogger().info("Starting ResourcePackFileHoster on " + internalWebServerSettings.hostName.read() + ":" + internalWebServerSettings.port.read());
            this.httpServer = Vertx.vertx().createHttpServer();
            this.httpServer.exceptionHandler(event -> Bukkit.getLogger().warning("Exception happened in ResourcePackFileHoster: " + event.getLocalizedMessage()));
            webServerHandler = new WebServerHandler();
            this.httpServer.requestHandler(webServerHandler);
            this.httpServer.listen(internalWebServerSettings.port.read(), internalWebServerSettings.hostName.read());
        } else {
            this.sshResourcePackUpload = new SshResourcePackUpload();
        }
    }

    public void registerListener(){
        Bukkit.getPluginManager().registerEvents(new ExtensionResourcePackApply(), MCCreativeLabExtension.getInstance());
    }

    public void closeAndWait() throws InterruptedException {
        if (mode.read().equals(Mode.INTERNAL_WEBSERVER))
            this.httpServer.close().result();
    }

    public void sendDefaultResourcePackToPlayers(Collection<? extends Player> players) {
        players.forEach(this::sendDefaultResourcePackToPlayer);
    }

    public void sendDefaultResourcePackToPlayer(Player player) {
        availableResourcePacks.forEach((s, resourcePackInfo) -> {
            if (resourcePackInfo.isRequired) sendResourcePackToPlayer(player, resourcePackInfo);
        });
    }

    public void sendResourcePackToPlayer(Player player, ResourcePackFileHoster.ResourcePackInfo packInfo) {
        String downloadURL = MCCreativeLabExtension.getResourcePackFileHoster().createDownloadUrl(packInfo.hash());

        player.setResourcePack(packInfo.getUUID(), downloadURL, WITH_HASHES ? packInfo.hashBytes() : null, (Component) null, requireResourcePack.read());
        Bukkit.getLogger().info("Sending resource pack with url " + downloadURL + " to " + player.getUniqueId());
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
                Bukkit.getLogger().info("Created Zip file " + zipFile + " in " + end + " ms");
                try {
                    Bukkit.getLogger().info("Calculating sha1 hash of resource pack");
                    byte[] hashBytes = null;
                    String hash = "mcclab";
                    if(WITH_HASHES){
                        start = System.currentTimeMillis();
                        hashBytes = calculateSHA1(zipFile.getPath());
                        hash = calculateSHA1String(hashBytes);
                        end = System.currentTimeMillis() - start;
                        Bukkit.getLogger().info("Took " + end + " ms");
                    }

                    ResourcePackInfo resourcePackInfo = new ResourcePackInfo(resourcePackName, zipFile, createDownloadUrl(hash), hash, hashBytes, true, null);
                    availableResourcePacks.put(hash, resourcePackInfo);
                    if (mode.read().equals(Mode.INTERNAL_WEBSERVER)) {
                        if (WITH_HASHES)
                            Bukkit.getLogger().info("MCCreativeLab: Hosting ResourcePack " + zipFile.getName() + " with hash: " + hash);
                        else
                            Bukkit.getLogger().info("MCCreativeLab: Hosting ResourcePack " + zipFile.getName());
                    }
                    else if (mode.read().equals(Mode.SSH_UPLOAD)) {
                        this.sshResourcePackUpload.upload();
                    }
                    if(MCCreativeLabExtension.isServerSoftware()){
                        Bukkit.getServer().setServerResourcePack(resourcePackInfo);
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public String createDownloadUrl(String hash) {
        String http = useHttps.read() ? "https" : "http";

        if (mode.read().equals(Mode.INTERNAL_WEBSERVER))
            return http + "://" + this.downloadUrl.read() + "/" + hash;
        else if (mode.read().equals(Mode.SSH_UPLOAD))
            return http + "://" + this.downloadUrl.read();
        throw new IllegalStateException("Mode not found: " + mode.read().name());
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

    public record ResourcePackInfo(String resourcePackName, File file, String url, String hash, byte[] hashBytes, boolean isRequired, @javax.annotation.Nullable Component prompt) implements ResourcePack{
        public UUID getUUID() {
            return UUID.nameUUIDFromBytes(resourcePackName.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public @NotNull UUID getId() {
            return getUUID();
        }

        @Override
        public @NotNull String getUrl() {
            return url;
        }

        @Override
        public @Nullable String getHash() {
            return hash;
        }

        @Override
        public @Nullable Component getPrompt() {
            return prompt;
        }
    }

    private static class InternalWebServerSettings {
        private final FileConfiguration fileConfiguration;
        public final ConfigValue.String hostName;
        public final ConfigValue.Integer port;

        public InternalWebServerSettings(FileConfiguration fileConfiguration) {
            this.fileConfiguration = fileConfiguration;
            this.hostName = new ConfigValue.String(fileConfiguration, "webserver.hostName", "0.0.0.0");
            this.port = new ConfigValue.Integer(fileConfiguration, "webserver.port", 8080);
        }
    }

    private static class SshUploadSettings {
        private final FileConfiguration fileConfiguration;
        public final ConfigValue.String address;
        public final ConfigValue.String user;
        public final ConfigValue.String keyFilePath;
        public final ConfigValue.String remotePath;
        public final ConfigValue.String remoteFingerPrintEd25519;

        public SshUploadSettings(FileConfiguration fileConfiguration) {
            this.fileConfiguration = fileConfiguration;
            this.address = new ConfigValue.String(fileConfiguration, "sshUpload.address", "localhost");
            this.remoteFingerPrintEd25519 = new ConfigValue.String(fileConfiguration, "sshUpload.remoteFingerprint.ed25519", "");
            this.user = new ConfigValue.String(fileConfiguration, "sshUpload.user", "root");
            this.keyFilePath = new ConfigValue.String(fileConfiguration, "sshUpload.keyFilePath", "privateKey");
            this.remotePath = new ConfigValue.String(fileConfiguration, "sshUpload.remotePath", "/resourcePacks/");
        }
    }

    private class WebServerHandler implements Handler<HttpServerRequest> {

        @Override
        public void handle(HttpServerRequest event) {
            try {
                var split = event.absoluteURI().split("/");
                if (split.length == 0) {
                    event.response().end();
                    return;
                }
                String hash = split[split.length - 1];
/*                if (!isValidHex(hash)) {
                    event.response().end();
                    return;
                }*/
                ResourcePackInfo resourcePackInfo = availableResourcePacks.getOrDefault(hash, null);
                if (resourcePackInfo == null) {
                    Bukkit.getLogger().warning("Someone requested a resource pack with hash " + hash + " that does not exist");
                    event.response().end();
                    return;
                }
                event.response().sendFile(resourcePackInfo.file.getAbsolutePath());
                Bukkit.getLogger().info("Sending resource pack with hash " + hash + " to " + event.remoteAddress());
            } finally {

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
    }

    private class SshResourcePackUpload {
        private final SSHClient ssh = new SSHClient();
        private final KeyProvider keyProvider;

        public SshResourcePackUpload() throws IOException {
            //ssh.loadKnownHosts();
            keyProvider = ssh.loadKeys(sshUploadSettings.keyFilePath.read());
            String fingerprint = sshUploadSettings.remoteFingerPrintEd25519.read();
            if (fingerprint.isEmpty())
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
            else
                ssh.addHostKeyVerifier(fingerprint);


            //ssh.authPublickey(sshUploadSettings.user.read(), keyProvider);
        }

        public void upload() throws IOException {
            ssh.connect(sshUploadSettings.address.read());
            try {
                ssh.authPublickey("root", keyProvider);
                for (Map.Entry<String, ResourcePackInfo> stringResourcePackInfoEntry : availableResourcePacks.entrySet()) {
                    ResourcePackInfo resourcePackInfo = stringResourcePackInfoEntry.getValue();
                    Bukkit.getLogger().info("Uploading ResourcePack " + resourcePackInfo.file.getAbsolutePath() + " to " + sshUploadSettings.remotePath.read() + resourcePackInfo.file.getName());
                    ssh.newSCPFileTransfer().upload(resourcePackInfo.file.getAbsolutePath(), sshUploadSettings.remotePath.read() + resourcePackInfo.file.getName());
                    Bukkit.getLogger().info("Done...");

                }
            } finally {
                ssh.disconnect();
            }
        }
    }

    private enum Mode {
        INTERNAL_WEBSERVER,
        SSH_UPLOAD
    }

    private class ServerSoftwareResourcePackApply implements Listener {

    }

    private class ExtensionResourcePackApply implements Listener {
        @EventHandler
        public void applyRequiredResourcePackOnJoin(PlayerJoinEvent e) {
            Bukkit.getLogger().info("Sending resource pack on join to player "+e.getPlayer().getName());
            if(!e.getPlayer().hasResourcePack())
                sendDefaultResourcePackToPlayer(e.getPlayer());
        }

        @EventHandler
        public void playerQuit(PlayerQuitEvent e){
            e.getPlayer().removeResourcePacks();
        }

        @EventHandler
        public void kickPlayersIfResourcePackWasNotAppliedButIsRequired(PlayerResourcePackStatusEvent e) {
            switch (e.getStatus()) {
                case DECLINED, FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL, DISCARDED -> {
                    if (requireResourcePack.read())
                        e.getPlayer().kick(Component.text("Resource pack could not be loaded!"), PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
                }
            }
        }
    }
}

package de.verdox.mccreativelab.util.ffmpeg;

import de.verdox.mccreativelab.util.io.ZipUtil;
import org.jetbrains.annotations.Nullable;
import ws.schild.jave.Encoder;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;


public class FFMPEGDownloader {

    private static ProcessLocator locator;

    @Nullable
    public static ProcessLocator getOrCreateFFMPEGEncoder(){
        if(locator != null)
            return locator;
        String os = System.getProperty("os.name").toLowerCase();
        File extractedArchiveWithBinaries;
        if(os.contains("linux")){
            extractedArchiveWithBinaries = ZipUtil.extractFilesFromZipFileResource("/ffmpeg/linux/ffmpeg.zip", "ffmpeg");
        }
        else {
            File downloadedArchive = downloadFFMPEG();
            if (downloadedArchive == null)
                return null;
            extractedArchiveWithBinaries = FileExtractor.extractFiles(downloadedArchive);
            if (extractedArchiveWithBinaries == null)
                return null;
        }
        Optional<Path> ffmpegBinaryPath = findFFmpegBinary(extractedArchiveWithBinaries);
        if (ffmpegBinaryPath.isEmpty())
            return null;
        File file = ffmpegBinaryPath.get().toFile();
        System.out.println("FFMPEG installed to: " + file.getAbsolutePath());
        locator = createCustomFFmpegLocator(file);
        return locator;
    }

    public static void main(String[] args) {
        getOrCreateFFMPEGEncoder();
    }

    /**
     * Erstellt einen Encoder, der eine spezifische ffmpeg-Binary verwendet.
     *
     * @param file Die ffmpeg-Binary Datei.
     * @return Ein Encoder-Objekt, das die angegebene ffmpeg-Binary verwendet.
     */
    private static ProcessLocator createCustomFFmpegLocator(File file) {
        // Erstelle einen eigenen FFMPEGLocator, der den Pfad zur ffmpeg-Binary verwendet

        return new CustomFFMPEGLocator(file);
    }

    private static @Nullable File downloadFFMPEG() {
        String os = System.getProperty("os.name").toLowerCase();
        String ffmpegUrl = "";

        // Bestimmen der URL basierend auf dem Betriebssystem
        if (os.contains("win")) {
            ffmpegUrl = "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"; // URL für Windows
        } else if (os.contains("mac")) {
            ffmpegUrl = "https://evermeet.cx/ffmpeg/ffmpeg-115034-ge09164940e.zip"; // URL für macOS
        } else if (os.contains("linux")) {
            ffmpegUrl = "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"; // URL für Linux
        } else {
            System.out.println("Unsupported operating system.");
            return null;
        }

        try {
            return downloadFFmpeg(ffmpegUrl);
        } catch (IOException e) {
            System.out.println("Failed to download ffmpeg: " + e.getMessage());
            return null;
        }
    }

    private static File downloadFFmpeg(String urlString) throws IOException {
        URL url = new URL(urlString);
        String fileName = extractFileName(urlString);
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            System.out.println("Download completed: " + Paths.get(fileName).toAbsolutePath());
            return new File(Paths.get(fileName).toAbsolutePath().toUri());
        }
    }

    private static String extractFileName(String urlString) {
        return urlString.substring(urlString.lastIndexOf('/') + 1);
    }

    /**
     * Sucht rekursiv nach der ffmpeg Binary in einem gegebenen Verzeichnis.
     *
     * @param directory Das Verzeichnis, in dem gesucht werden soll.
     * @return Ein Optional, das den Pfad zur ffmpeg Binary enthält, wenn gefunden.
     */
    public static Optional<Path> findFFmpegBinary(File directory) {
        Path dirPath = directory.getAbsoluteFile().toPath();

        // Stelle sicher, dass der Pfad existiert und ein Verzeichnis ist
        if (!Files.isDirectory(dirPath)) {
            System.out.println("Der angegebene Pfad ist kein Verzeichnis.");
            return Optional.empty();
        }

        try (Stream<Path> paths = Files.walk(dirPath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().matches("ffmpeg(.exe)?"))
                .findFirst();
        } catch (IOException e) {
            System.out.println("Fehler beim Durchsuchen des Verzeichnisses: " + e.getMessage());
            return Optional.empty();
        }
    }
}

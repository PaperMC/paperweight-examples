package de.verdox.mccreativelab.util.ffmpeg;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

public class FileExtractor {

    public static @Nullable File extractFiles(File fileToExtract){
        try {
            String filePath = fileToExtract.getPath();
            String folderPath = filePath.substring(0, filePath.lastIndexOf('.'));
            File folder = new File(folderPath);

            if (!folder.exists()) {
                folder.mkdirs(); // Erstelle den Ordner mit dem gleichen Namen wie die Datei (ohne Erweiterung)
            }

            if (filePath.endsWith(".zip")) {
                unzip(filePath, folderPath);
            } else if (filePath.endsWith(".tar.xz")) {
                untarXZ(filePath, folderPath);
            }

            Files.delete(fileToExtract.toPath()); // Lösche die heruntergeladene Archivdatei
            System.out.println("Extraction complete and original file deleted.");
            return folder;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void unzip(String zipFilePath, String destDir) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry = zis.getNextEntry();
        byte[] buffer = new byte[1024];

        while (zipEntry != null) {
            File newFile = newFile(new File(destDir), zipEntry);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private static void untarXZ(String inputFile, String outputDir) throws IOException {
        InputStream fi = Files.newInputStream(Paths.get(inputFile));
        BufferedInputStream bi = new BufferedInputStream(fi);
        XZCompressorInputStream xzi = new XZCompressorInputStream(bi);
        TarArchiveInputStream tis = new TarArchiveInputStream(xzi);

        org.apache.commons.compress.archivers.tar.TarArchiveEntry entry;
        while ((entry = tis.getNextTarEntry()) != null) {
            File outputFile = newFile(new File(outputDir), entry);
            if (entry.isDirectory()) {
                outputFile.mkdirs();
            } else {
                new File(outputFile.getParent()).mkdirs();
                try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(tis, outputFileStream);
                }
            }
        }
        tis.close();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private static File newFile(File destinationDir, org.apache.commons.compress.archivers.tar.TarArchiveEntry tarEntry) throws IOException {
        File destFile = new File(destinationDir, tarEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + tarEntry.getName());
        }
        return destFile;
    }
}

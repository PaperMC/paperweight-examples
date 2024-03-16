package de.verdox.mccreativelab.util.io;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public static void zipFolder(Path sourceFolderPath, Path zipFilePath) {
        File existingZipFile = new File(zipFilePath.toUri());
        if (existingZipFile.exists()) {
            existingZipFile.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            File sourceFolder = new File(sourceFolderPath.toUri());

            // Füge alle Dateien im Quellordner zum Zip-Archiv hinzu
            addFilesToZip(sourceFolder, "", zos);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addFilesToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFilesToZip(file, parentFolder + file.getName() + "/", zos);
                continue;
            }

            FileInputStream fis = new FileInputStream(file);

            // Erstelle eine neue ZipEntry
            ZipEntry zipEntry = new ZipEntry(parentFolder + file.getName());
            zos.putNextEntry(zipEntry);

            // Kopiere die Dateiinhalte in das Zip-Archiv
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            fis.close();
        }
    }
    public static File extractFilesFromZipFileResource(String zipResourcePath, String targetFolderPath) {
        return extractFilesFromZipFileResource(() -> ZipUtil.class.getResourceAsStream(zipResourcePath), targetFolderPath);
    }

    public static File extractFilesFromZipFileResource(Supplier<InputStream> zipFile, String targetFolderPath) {
        try (InputStream inputStream = zipFile.get()) {
            if (inputStream == null) {
                System.err.println("Unable to find the specified zip file");
                return null;
            }

            File targetFolder = new File(targetFolderPath);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry;

                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    File entryFile = new File(targetFolder, entryName);

                    if (entry.isDirectory()) {
                        entryFile.mkdirs();
                    } else {
                        try (OutputStream outputStream = new FileOutputStream(entryFile)) {
                            byte[] buffer = new byte[1024];
                            int length;

                            while ((length = zipInputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                        }
                    }

                    zipInputStream.closeEntry();
                }
            }
            return targetFolder;
        } catch (IOException e) {
            System.err.println("Error during extraction: " + e.getMessage());
            return null;
        }
    }
}

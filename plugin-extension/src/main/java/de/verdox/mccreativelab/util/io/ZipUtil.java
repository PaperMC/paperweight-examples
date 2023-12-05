package de.verdox.mccreativelab.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
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
}

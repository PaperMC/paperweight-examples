package de.verdox.mccreativelab.util.io;

import de.verdox.mccreativelab.generator.AssetPath;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties loadProperties(AssetPath assetPath) {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(assetPath.toPath().toFile())) {
            // .properties-Datei laden
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static void saveProperties(AssetPath assetPath, Properties properties) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(assetPath.toPath().toFile())) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

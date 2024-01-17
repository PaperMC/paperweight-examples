package de.verdox.mccreativelab.util;

import java.io.*;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties loadPropertiesFile(String filePath) {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace(); // Hier sollte eine angemessene Fehlerbehandlung erfolgen
        }

        return properties;
    }


    public static void savePropertiesToFile(Properties properties, String filePath) {
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

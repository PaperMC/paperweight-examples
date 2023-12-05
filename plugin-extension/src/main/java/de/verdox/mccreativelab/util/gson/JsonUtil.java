package de.verdox.mccreativelab.util.gson;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

public class JsonUtil {
    private static final Gson GSON_INSTANCE = new Gson();

    public static JsonObject readJsonFromFile(File file) {
        Objects.requireNonNull(file);
        if (!file.exists())
            return new JsonObject();
        JsonObject jsonObject;
        try (Reader reader = new FileReader(file)) {
            jsonObject = GSON_INSTANCE.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }

    public static JsonObject readJsonInputStream(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        JsonObject jsonObject;
        try (Reader reader = new InputStreamReader(inputStream)) {
            jsonObject = GSON_INSTANCE.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }

    public static void createFolderStructure(Path path) {
        if (path.toFile().getParentFile() != null)
            path.toFile().getParentFile().mkdirs();
    }

    public static void writeJsonObjectToFile(JsonObject jsonObject, File file) {
        Objects.requireNonNull(jsonObject);
        Objects.requireNonNull(file);
        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Writer fileWriter = new FileWriter(file)) {
            GSON_INSTANCE.toJson(jsonObject, fileWriter);
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

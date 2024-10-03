package de.verdox.mccreativelab.registry;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import de.verdox.mccreativelab.wrapper.JsonSerializer;
import org.bukkit.Keyed;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JsonRegistryBootstrapper<T extends Keyed> {
    private final File registryFolder;
    private final OpenRegistry<T> registry;
    private final JsonSerializer<T> serializer;

    public JsonRegistryBootstrapper(File registryFolder, OpenRegistry<T> registry, JsonSerializer<T> serializer) {
        this.registryFolder = registryFolder;
        this.registry = registry;
        this.serializer = serializer;
    }

    public void bootstrap(T exampleValue) throws IOException {
        File file = new File(registryFolder + "/example.json");
        JsonUtil.writeJsonObjectToFile(serializer.toJson(exampleValue).getAsJsonObject(), file);

        try (Stream<Path> stream = Files.walk(registryFolder.toPath(), 1).skip(1)) {
            stream.filter(path -> FileUtils.extension(path.toFile().getName()).equals("json"))
                .forEach(path -> {
                    try {
                        JsonObject jsonObject = JsonUtil.readJsonFromFile(path.toFile());
                        T deserialized = serializer.fromJson(jsonObject);
                        if(deserialized == null)
                            return;
                        registry.register(deserialized.getKey(), deserialized);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }

}

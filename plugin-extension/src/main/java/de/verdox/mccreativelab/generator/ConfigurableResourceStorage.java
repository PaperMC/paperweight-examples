package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;

public class ConfigurableResourceStorage<C extends CustomPack<C>> {

    void loadResourceFromStorage(ConfigurableResource<C> configurableResource) throws IOException {
        File serialized = getFileOfResource(configurableResource);
        if (!fileExists(configurableResource))
            JsonUtil.writeJsonObjectToFile(configurableResource.serializeToJson(), serialized);
        else
            configurableResource.deserializeFromJson(JsonUtil.readJsonFromFile(serialized));
    }

    private boolean fileExists(ConfigurableResource<C> configurableResource) {
        return getFileOfResource(configurableResource).exists();
    }

    private File getFileOfResource(ConfigurableResource<C> configurableResource) {
        File parentFolder = MCCreativeLabExtension.getInstance().getDataFolder();
        String type = configurableResource.getClass().getSimpleName();
        NamespacedKey namespacedKey = configurableResource.getKey();
        return new File(parentFolder, type + "/" + namespacedKey.getNamespace() + "/" + namespacedKey.getKey() + ".json");
    }
}

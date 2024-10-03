package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;

public class ConfigurableResourceStorage<C extends CustomPack<C>> {

    static void deleteTemplateFolder() throws IOException {
        File parentFolder = new File(MCCreativeLabExtension.getInstance().getDataFolder()+"/template");
        if(parentFolder.isDirectory())
            FileUtils.deleteDirectory(parentFolder);
    }

    void loadResourceFromStorage(ConfigurableResource<C> configurableResource) throws IOException {
        File serialized = getFileOfResource(configurableResource);

        JsonUtil.writeJsonObjectToFile(configurableResource.serializeToJson(), getTemplateFileOfResource(configurableResource));

        if(fileExists(configurableResource))
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

    private File getTemplateFileOfResource(ConfigurableResource<C> configurableResource){
        File parentFolder = new File(MCCreativeLabExtension.getInstance().getDataFolder()+"/template");
        String type = configurableResource.getClass().getSimpleName();
        NamespacedKey namespacedKey = configurableResource.getKey();
        return new File(parentFolder, type + "/" + namespacedKey.getNamespace() + "/" + namespacedKey.getKey() + ".json");

    }
}

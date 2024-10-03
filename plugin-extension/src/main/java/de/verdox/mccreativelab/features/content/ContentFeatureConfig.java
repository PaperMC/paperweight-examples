package de.verdox.mccreativelab.features.content;

import de.verdox.mccreativelab.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ContentFeatureConfig {
    private final YamlConfiguration yamlConfiguration;
    private final File file;

    public ContentFeatureConfig(ContentFeatureList contentFeatureList, BootstrapContext bootstrapContext) throws IOException {
        file = new File(bootstrapContext.getDataDirectory() + "/features.yml");
        yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.options().copyDefaults(true);

        Set<String> existingFeatures = new HashSet<>();
        for (ContentFeature feature : contentFeatureList.getFeatures()) {
            yamlConfiguration.addDefault(feature.getClass().getSimpleName(), false);
            existingFeatures.add(feature.getClass().getSimpleName());
        }

        for (String key : yamlConfiguration.getKeys(false)) {
            if (!existingFeatures.contains(key))
                yamlConfiguration.set(key, null);
        }
        yamlConfiguration.save(file);
    }

    public boolean isDisabled(ContentFeature contentFeature){
        return !yamlConfiguration.getBoolean(contentFeature.getClass().getSimpleName());
    }
}

package de.verdox.mccreativelab.features.content;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ContentFeatureList implements Listener {
    private final List<ContentFeature> features = new LinkedList<>();
    private final String projectName;
    private JavaPlugin javaPlugin;

    public ContentFeatureList(String projectName){
        this.projectName = projectName;
    }

    public <T extends ContentFeature> T feature(T feature) {
        if (feature == null)
            return null;
        getFeatures().add(feature);
        feature.onAdd();
        return feature;
    }

    public List<ContentFeature> getFeatures() {
        return features;
    }

    public void onBootstrap(PluginBootstrap pluginBootstrap) throws Exception {
        System.out.println("Running "+projectName+" Content Bootstrapper");
        for (ContentFeature contentFeature : getFeatures()) {
            contentFeature.bootstrap(pluginBootstrap);
        }
    }

    public void onLoad(JavaPlugin javaPlugin) throws Exception{
        if(this.javaPlugin != null && !this.javaPlugin.equals(javaPlugin))
            throw new IllegalArgumentException("An other plugin is trying to execute onLoad");
        this.javaPlugin = javaPlugin;
        Bukkit.getLogger().info("Running "+projectName+" plugin loader");
        for (ContentFeature contentFeature : features) {
            contentFeature.dataSetup(javaPlugin);
        }
    }

    public void onEnable(JavaPlugin javaPlugin) throws Exception{
        if(this.javaPlugin != null && !this.javaPlugin.equals(javaPlugin))
            throw new IllegalArgumentException("An other plugin is trying to execute onLoad");
        this.javaPlugin = javaPlugin;
        Bukkit.getLogger().info("Running "+projectName+" plugin enable logic");
        for (ContentFeature contentFeature : features) {
            Bukkit.getPluginManager().registerEvents(contentFeature, javaPlugin);
            contentFeature.onEnable(javaPlugin);
        }
        Bukkit.getPluginManager().registerEvents(this, javaPlugin);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e){
        if(!e.getType().equals(ServerLoadEvent.LoadType.STARTUP) || this.javaPlugin == null)
            return;
        try{
            for (ContentFeature feature : features) {
                feature.onStartupComplete(this.javaPlugin);
            }
        }
        catch (Exception exception){
            Bukkit.getLogger().log(Level.SEVERE, "An exception occured while loading the features of "+projectName, exception);
            Bukkit.shutdown();
            return;
        }
        Bukkit.getLogger().info(projectName+" features successfully loaded");
    }
}

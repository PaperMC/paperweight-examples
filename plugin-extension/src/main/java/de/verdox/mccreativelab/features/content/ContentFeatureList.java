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
    private Status status = Status.BOOTSTRAP;

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
            System.out.println("Bootstrap - "+contentFeature.getClass().getSimpleName());
            contentFeature.bootstrap(pluginBootstrap);
        }
    }

    public void onLoad(JavaPlugin javaPlugin) throws Exception{
        if(this.javaPlugin != null && !this.javaPlugin.equals(javaPlugin))
            throw new IllegalArgumentException("An other plugin is trying to execute onLoad");
        status = Status.SERVER_LOAD;
        this.javaPlugin = javaPlugin;
        Bukkit.getLogger().info("Running "+projectName+" plugin loader");
        for (ContentFeature contentFeature : features) {
            Bukkit.getLogger().info("DataSetup - "+contentFeature.getClass().getSimpleName());
            contentFeature.dataSetup(javaPlugin);
        }
    }

    public void onEnable(JavaPlugin javaPlugin) throws Exception{
        if(this.javaPlugin != null && !this.javaPlugin.equals(javaPlugin))
            throw new IllegalArgumentException("An other plugin is trying to execute onLoad");
        status = Status.ENABLE_PLUGIN;
        this.javaPlugin = javaPlugin;
        Bukkit.getLogger().info("Running "+projectName+" plugin enable logic");
        for (ContentFeature contentFeature : features) {
            Bukkit.getLogger().info("Enabling - "+contentFeature.getClass().getSimpleName());
            Bukkit.getPluginManager().registerEvents(contentFeature, javaPlugin);
            contentFeature.onEnable(javaPlugin);
        }
        Bukkit.getPluginManager().registerEvents(this, javaPlugin);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e){
        if(!e.getType().equals(ServerLoadEvent.LoadType.STARTUP) || this.javaPlugin == null)
            return;
        status = Status.SERVER_LOAD;
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

    public Status getStatus() {
        return status;
    }

    public enum Status{
        BOOTSTRAP,
        DATA_SETUP,
        ENABLE_PLUGIN,
        SERVER_LOAD,
    }
}

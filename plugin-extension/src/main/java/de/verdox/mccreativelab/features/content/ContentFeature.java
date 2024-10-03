package de.verdox.mccreativelab.features.content;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public interface ContentFeature extends Listener {
    default void onAdd(){}
    void bootstrap(PluginBootstrap pluginBootstrap, BootstrapContext bootstrapContext) throws Exception;
    void dataSetup(JavaPlugin javaPlugin) throws Exception;
    void onEnable(JavaPlugin javaPlugin) throws Exception;
    default void onDisable(JavaPlugin javaPlugin) throws Exception{}
    default void onStartupComplete(JavaPlugin javaPlugin) throws Exception {}
}

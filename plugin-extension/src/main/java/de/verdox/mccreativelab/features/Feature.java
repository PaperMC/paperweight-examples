package de.verdox.mccreativelab.features;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Feature implements Listener {
    private static final Set<Feature> enabledFeatures = new HashSet<>();
    public static void disable(){
        enabledFeatures.forEach(Feature::onDisable);
    }
    public final void enable() {
        Bukkit.getLogger().info("Enabling Feature " + getClass().getSimpleName());
        Bukkit.getPluginManager().registerEvents(this, MCCreativeLabExtension.getInstance());
        onEnable();
        enabledFeatures.add(this);
    }

    public void onDisable(){

    }

    protected abstract void onEnable();
}

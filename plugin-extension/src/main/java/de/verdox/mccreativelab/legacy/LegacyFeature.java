package de.verdox.mccreativelab.legacy;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.recipe.CustomItemData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class LegacyFeature implements Listener {
    public final void enable() {
        Bukkit.getLogger().info("Enabling Legacy Feature " + getClass().getSimpleName());
        Bukkit.getPluginManager().registerEvents(this, MCCreativeLabExtension.getInstance());
        onEnable();


    }

    protected abstract void onEnable();
}

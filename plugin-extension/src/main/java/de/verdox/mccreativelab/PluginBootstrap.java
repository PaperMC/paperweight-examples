package de.verdox.mccreativelab;

import de.verdox.mccreativelab.data.DataPackInterceptor;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PluginBootstrap implements io.papermc.paper.plugin.bootstrap.PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {

        MCCreativeLab.getVanillaPackGenerator().exclude(DataPackInterceptor.PackAssetType.RECIPES);

        MCCreativeLab.getVanillaPackGenerator().onInstall(dataPackAsset -> {
            System.out.println("Installing: "+dataPackAsset.packAssetType()+" | "+dataPackAsset.key());
        });
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return io.papermc.paper.plugin.bootstrap.PluginBootstrap.super.createPlugin(context);
    }
}

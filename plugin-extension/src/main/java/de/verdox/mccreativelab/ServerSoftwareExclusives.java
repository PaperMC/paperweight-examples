package de.verdox.mccreativelab;

import de.verdox.itemformat.BasicItemFormat;
import de.verdox.mccreativelab.event.MCCreativeLabReloadEvent;
import de.verdox.mccreativelab.features.legacy.LegacyFeatures;
import de.verdox.mccreativelab.world.block.FakeBlockRegistry;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.world.block.replaced.ReplacedBlocks;
import de.verdox.mccreativelab.world.item.FakeItemRegistry;
import de.verdox.mccreativelab.world.item.data.ItemDataContainer;
import de.verdox.mccreativelab.world.sound.ReplacedSoundGroups;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;

public class ServerSoftwareExclusives implements Listener {
    private static FakeBlockRegistry FAKE_BLOCK_REGISTRY;
    private static FakeItemRegistry FAKE_ITEM_REGISTRY;
    private static FakeBlockStorage fakeBlockStorage;
    private static LegacyFeatures legacyFeatures;

    private ReplacedSoundGroups replacedSoundGroups;

    public void onLoad(){
        FAKE_BLOCK_REGISTRY = new FakeBlockRegistry();
        FAKE_ITEM_REGISTRY = new FakeItemRegistry();
        replacedSoundGroups  = new ReplacedSoundGroups();
        fakeBlockStorage = new FakeBlockStorage();
        legacyFeatures = new LegacyFeatures();
        FakeBlockRegistry.setupFakeBlocks();
        ReplacedBlocks.setup();
        //BasicItemFormat.BASIC_FORMAT.setBehaviour(ItemDataContainer::from);
    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public void onServerLoad(ServerLoadEvent.LoadType loadType){
        if(loadType.equals(ServerLoadEvent.LoadType.STARTUP)){
            if(MCCreativeLabExtension.getFakeBlockRegistry().hasReusedAnyBlockStates()){
                if(FakeBlockRegistry.USE_ALTERNATE_FAKE_BLOCK_ENGINE) {
                    replacedSoundGroups.replaceGlassSoundGroup();
                }
                replacedSoundGroups.replaceWoodSoundGroup();
            }
            Bukkit.getLogger().info("ServerSoftware exclusive features started");
        }
    }

    public static FakeBlockRegistry getFakeBlockRegistry() {
        return FAKE_BLOCK_REGISTRY;
    }

    public static FakeItemRegistry getFakeItemRegistry() {
        return FAKE_ITEM_REGISTRY;
    }

    public static FakeBlockStorage getFakeBlockStorage() {
        return fakeBlockStorage;
    }

    public static LegacyFeatures getLegacyFeatures() {
        return legacyFeatures;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void buildPackOnServerLoad(ServerLoadEvent e) {
        onServerLoad(e.getType());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginReload(MCCreativeLabReloadEvent ignored) {
        onServerLoad(ServerLoadEvent.LoadType.RELOAD);
    }

    public ReplacedSoundGroups getReplacedSoundGroups() {
        return replacedSoundGroups;
    }
}

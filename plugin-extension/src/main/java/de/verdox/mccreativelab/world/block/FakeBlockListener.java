package de.verdox.mccreativelab.world.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class FakeBlockListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFakeBlockPlace(BlockPlaceEvent e) {
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(e.getBlock()))
            return;
        //TODO
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getBlock().getLocation(), false);

/*        FakeBlockSoundManager.simulateBlockPlaceSound(e.getPlayer(), e.getBlock(), fakeBlockState);*/
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void preventNotePlayWhenIsFakeBlockEvent(NotePlayEvent e){
        Block block = e.getBlock();
        if(FakeBlockStorage.getFakeBlock(block.getLocation(), false) != null)
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void blockDestroy(BlockDestroyEvent e){
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getBlock().getLocation(), false);
        if(fakeBlockState == null)
            return;
        fakeBlockState.getFakeBlock().remove(e.getBlock().getLocation(), e.playEffect(), e.willDrop(), null, null, true);
        e.setWillDrop(false);
        e.setPlayEffect(false);
        e.setExpToDrop(0);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void preventBukkitFertilizationLogic(BlockFertilizeEvent e){
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getBlock().getLocation(), false);
        if(fakeBlockState == null)
            return;
        if(fakeBlockState.getBlockedEventsByDefault().contains(e.getClass()))
            e.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void doNotDropVanillaLootForFakeBlocks(BlockBreakEvent e) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getBlock()
                                                                                             .getLocation(), false);
        if (fakeBlockState == null)
            return;
        if(e.isDropItems() && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            fakeBlockState.getFakeBlock().dropBlockLoot(e.getBlock().getLocation(), fakeBlockState, e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand(), false);
            fakeBlockState.getFakeBlock().dropBlockExperience(e.getBlock().getLocation(), fakeBlockState, e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand(), false);
            e.setDropItems(false);
        }
    }
}

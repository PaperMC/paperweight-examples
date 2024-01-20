package de.verdox.mccreativelab.world.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import javax.annotation.Nullable;

public class FakeBlockListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockPlace(BlockPlaceEvent e) {
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(e.getBlock()))
            return;
        //TODO
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getBlock().getLocation(), false);

        FakeBlockSoundManager.simulateBlockPlaceSound(e.getPlayer(), e.getBlock(), fakeBlockState);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void blockDestroy(BlockDestroyEvent e){
        FakeBlockStorage.setFakeBlock(e.getBlock().getLocation(), null, false);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void doNotDropVanillaLootForFakeBlocks(BlockBreakEvent e) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getBlock()
                                                                                             .getLocation(), false);
        if (fakeBlockState == null)
            return;
        e.setDropItems(false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void fakeBlockDropItems(BlockDropItemEvent e) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getBlock()
                                                                                             .getLocation(), false);
        if (fakeBlockState == null)
            return;
        e.getItems().clear();
        fakeBlockState.getFakeBlock().drawLoot();
    }

    @Nullable
    private static FakeBlock.FakeBlockState getFakeBlockState(Block block) {
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(block))
            return null;
        return FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
    }
}

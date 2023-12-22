package de.verdox.mccreativelab.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import javax.annotation.Nullable;

public class FakeBlockListener implements Listener {


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockPlace(BlockPlaceEvent e) {
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(e.getBlock()))
            return;
        //TODO
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getBlock()
                                                                                             .getLocation(), false);

        FakeBlockSoundManager.simulateBlockPlaceSound(e.getPlayer(), e.getBlock(), fakeBlockState);
    }

/*    @EventHandler
    public void fakeBlockPistonExtend(BlockPistonExtendEvent e) {
        List<Block> blocks = List.copyOf(e.getBlocks());

        Lists.reverse(blocks);

        for (Block block : blocks) {
            FakeBlock.FakeBlockState fakeBlockState = getFakeBlockState(block);
            if (fakeBlockState == null)
                continue;
            PistonMoveReaction pistonMoveReaction = fakeBlockState.getFakeBlock().getPistonMoveReaction();

            switch (pistonMoveReaction) {
                case BLOCK, IGNORE -> e.setCancelled(true);
                case PUSH_ONLY, MOVE -> {
                    //TODO: Push fake block display with it - Maybe transform?
                    Vector pushDirection = e.getDirection().getDirection();
                    ItemDisplay itemDisplay = FakeBlockVisualCache.getLinkedItemDisplay(block);
                    if (itemDisplay == null)
                        continue;

                    FakeBlockUtil.moveBlock(block.getLocation(), itemDisplay, fakeBlockState, pushDirection);
                }
                case BREAK -> {
                    FakeBlockUtil.removeFakeBlockIfPossible(block);
                    FakeBlockUtil.simulateBlockBreakWithParticlesAndSound(fakeBlockState, block);
                }
            }
        }
    }

    @EventHandler
    public void fakeBlockPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            FakeBlock.FakeBlockState fakeBlockState = getFakeBlockState(block);
            if (fakeBlockState == null)
                continue;
            PistonMoveReaction pistonMoveReaction = fakeBlockState.getFakeBlock().getPistonMoveReaction();

            switch (pistonMoveReaction) {
                case BLOCK, IGNORE, PUSH_ONLY -> e.setCancelled(true);
                case MOVE -> {
                    //TODO: Push fake block display with it - Maybe transform?
                    Vector pushDirection = e.getDirection().getDirection();
                    ItemDisplay itemDisplay = FakeBlockVisualCache.getLinkedItemDisplay(block);
                    if (itemDisplay == null)
                        continue;

                    FakeBlockUtil.moveBlock(block.getLocation(), itemDisplay, fakeBlockState, pushDirection);
                }
                case BREAK -> {
                    FakeBlockUtil.removeFakeBlockIfPossible(block);
                    FakeBlockUtil.simulateBlockBreakWithParticlesAndSound(fakeBlockState, block);
                }
            }
        }
    }*/

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockBreak(BlockDestroyEvent e) {
        Bukkit.getScheduler()
              .runTask(MCCreativeLabExtension.getInstance(), () -> FakeBlockUtil.removeFakeBlockIfPossible(e.getBlock()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockBreak(BlockExplodeEvent e) {
        Bukkit.getScheduler()
              .runTask(MCCreativeLabExtension.getInstance(), () -> FakeBlockUtil.removeFakeBlockIfPossible(e.getBlock()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockBreak(BlockBreakEvent e) {
        Bukkit.getScheduler()
              .runTask(MCCreativeLabExtension.getInstance(), () -> FakeBlockUtil.removeFakeBlockIfPossible(e.getBlock()));
    }

    @Nullable
    private static FakeBlock.FakeBlockState getFakeBlockState(Block block) {
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(block))
            return null;
        return FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
    }
}

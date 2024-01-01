package de.verdox.mccreativelab.block;

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
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getBlock()
                                                                                             .getLocation(), false);

        FakeBlockSoundManager.simulateBlockPlaceSound(e.getPlayer(), e.getBlock(), fakeBlockState);
    }

/*    @EventHandler
    public void soundEvent(WorldSoundEvent e) {
        e.setExcept(null);
        if (e.getSound().getKey().contains("block"))
            Bukkit.getLogger()
                  .info("Playing sound: " + e.getSound() + " at pos " + e.getSoundLocation().toBlock() + " (" + e
                      .getSoundLocation().getBlock().getState().getBlockData() + ")");
    }

    @EventHandler
    public void worldEvent(WorldEffectEvent e) {
        Bukkit.getLogger().info("Effect: " + e.getEffect().name());
        if(e.getEffect().equals(Effect.STEP_SOUND))
            e.setCancelled(true);
    }*/

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

    @Nullable
    private static FakeBlock.FakeBlockState getFakeBlockState(Block block) {
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(block))
            return null;
        return FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
    }
}

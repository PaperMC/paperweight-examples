package de.verdox.mccreativelab.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.google.common.collect.Lists;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.events.ChunkDataCreateEvent;
import de.verdox.mccreativelab.events.ChunkDataLoadEvent;
import de.verdox.mccreativelab.events.ChunkDataSaveEvent;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

public class FakeBlockListener implements Listener {

    @EventHandler
    public void worldSave(WorldSaveEvent e){
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().save(e.getWorld());
    }
    @EventHandler
    public void onChunkDataCreate(ChunkDataCreateEvent e) {
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().createData(e.getWorld(), Chunk.getChunkKey(e.getChunkPos().x(), e.getChunkPos().z()));
    }

    @EventHandler
    public void onChunkDataLoad(ChunkDataLoadEvent e) {
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().loadChunk(e.getChunk(), e.getPersistentDataContainer());
    }

    @EventHandler
    public void onChunkDataSave(ChunkDataSaveEvent e) {
        if (e.getChunk() == null)
            return;
        MCCreativeLabExtension.getInstance().getFakeBlockStorage().saveChunk(e.getChunk(), e.getPersistentDataContainer(), e.isUnloaded());
    }

    @EventHandler
    public void linkDisplayEntitiesToFakeBlocks(EntityAddToWorldEvent e){
        if(!(e.getEntity() instanceof ItemDisplay itemDisplay))
            return;
        FakeBlockStorage.linkDisplayEntityToFakeBlock(itemDisplay);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockPlace(BlockPlaceEvent e){
        if(!FakeBlockSoundManager.isBlockWithoutStandardSound(e.getBlock()))
            return;
        //TODO
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getBlock().getLocation(), false);
        FakeBlockSoundManager.simulateBlockPlaceSound(e.getPlayer(), e.getBlock(), fakeBlockState);
    }

    @EventHandler
    public void fakeBlockPistonExtend(BlockPistonExtendEvent e){

        List<Block> blocks = List.copyOf(e.getBlocks());

        Lists.reverse(blocks);

        for (Block block : blocks) {
            FakeBlock.FakeBlockState fakeBlockState = getFakeBlockState(block);
            if(fakeBlockState == null)
                continue;
            PistonMoveReaction pistonMoveReaction = fakeBlockState.getFakeBlock().getPistonMoveReaction();

            switch (pistonMoveReaction){
                case BLOCK, IGNORE -> e.setCancelled(true);
                case PUSH_ONLY,MOVE -> {
                    //TODO: Push fake block display with it - Maybe transform?
                    Vector pushDirection = e.getDirection().getDirection();
                    ItemDisplay itemDisplay = FakeBlockStorage.getLinkedDisplayEntity(block);
                    if(itemDisplay == null)
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
    public void fakeBlockPistonRetract(BlockPistonRetractEvent e){
        for (Block block : e.getBlocks()) {
            FakeBlock.FakeBlockState fakeBlockState = getFakeBlockState(block);
            if(fakeBlockState == null)
                continue;
            PistonMoveReaction pistonMoveReaction = fakeBlockState.getFakeBlock().getPistonMoveReaction();

            switch (pistonMoveReaction){
                case BLOCK, IGNORE,PUSH_ONLY -> e.setCancelled(true);
                case MOVE -> {
                    //TODO: Push fake block display with it - Maybe transform?
                    Vector pushDirection = e.getDirection().getDirection();
                    ItemDisplay itemDisplay = FakeBlockStorage.getLinkedDisplayEntity(block);
                    if(itemDisplay == null)
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockBreak(BlockDestroyEvent e){
        FakeBlockUtil.removeFakeBlockIfPossible(e.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockBreak(BlockExplodeEvent e){
        FakeBlockUtil.removeFakeBlockIfPossible(e.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeBlockBreak(BlockBreakEvent e){
        FakeBlockUtil.removeFakeBlockIfPossible(e.getBlock());
    }

    @Nullable
    private static FakeBlock.FakeBlockState getFakeBlockState(Block block){
        if(!FakeBlockSoundManager.isBlockWithoutStandardSound(block))
            return null;
        return FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
    }
}

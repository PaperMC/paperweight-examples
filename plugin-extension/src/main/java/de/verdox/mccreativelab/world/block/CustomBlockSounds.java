package de.verdox.mccreativelab.world.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.util.PlayerUtil;
import de.verdox.mccreativelab.world.block.customhardness.BlockBreakSpeedModifier;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.world.WorldEffectEvent;
import io.papermc.paper.event.world.WorldSoundEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;

public class CustomBlockSounds implements Listener {
    @EventHandler
    public void gameEvent(GenericGameEvent genericGameEvent) {
        if (!(genericGameEvent.getEntity() instanceof Player))
            return;
        Block block = genericGameEvent.getLocation().getBlock();
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(block))
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (genericGameEvent.getEvent().equals(GameEvent.BLOCK_PLACE))
            FakeBlockSoundManager.simulateBlockPlaceSound(block, fakeBlockState);

        if (genericGameEvent.getEvent().equals(GameEvent.BLOCK_DESTROY))
            FakeBlockSoundManager.simulateBreakSound(block, fakeBlockState);
    }


    // The upper method will not be called when the block break was cancelled. However, the client will predict a block break and needs a sound anyway.
    @EventHandler(priority = EventPriority.MONITOR)
    public void playBreakSoundEvenWhenBreakCancelled(BlockBreakEvent e) {
        if (!e.isCancelled())
            return;
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(e.getBrokenState().getBlockData()))
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getBlock().getLocation(), false);
        FakeBlockSoundManager.simulateBreakSound(e.getBrokenState(), fakeBlockState);
    }

    @EventHandler
    public void storeLastPlayerInteraction(PlayerInteractEvent e) {
        e.getPlayer().setMetadata("last_click_action", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), e.getAction()));
    }

    @EventHandler
    public void simulateDiggingSoundOnArmSwing(PlayerArmSwingEvent e) {
        Player player = e.getPlayer();
        if (!e.getHand().isHand())
            return;
        if (e.getPlayer().hasMetadata("last_click_action")) {

            Action action = (Action) e.getPlayer().getMetadata("last_click_action").get(0).value();
            if (action != null && action.isRightClick())
                return;

        }

        Block rayTracedBlock = PlayerUtil.getTargetBlock(e.getPlayer());
        if (rayTracedBlock == null)
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(rayTracedBlock.getLocation(), false);
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(rayTracedBlock))
            return;
        FakeBlockSoundManager.simulateDiggingSound(player, rayTracedBlock, fakeBlockState);
    }

    @EventHandler
    public void replaceSounds(WorldSoundEvent e) {
        if (!e.getSound().namespace().equals("minecraft"))
            return;

        String key = e.getSound().getKey();
        if (!key.contains("block") || !key.contains("step"))
            return;
        if (e.getExcept() == null)
            return;

        Location blockLoc = e.getSoundLocation().clone().add(0, -1, 0);
        if (!FakeBlockSoundManager.isBlockWithoutStandardSound(blockLoc.getBlock()))
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(blockLoc, false);
        Wrappers.SoundGroup soundGroup = FakeBlockSoundManager.getSoundGroup(blockLoc.getBlock(), fakeBlockState);
        e.setSound(soundGroup.getStepSound().getKey());
        e.setExcept(null);
    }

    @EventHandler
    public void replaceEffect(WorldEffectEvent e) {
        if (e.getExcept() == null)
            return;

        if (e.getEffect().equals(Effect.STEP_SOUND)) {
            FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getSoundLocation()
                .toBlockLocation(), false);
            if (fakeBlockState != null) {
                e.setExcept(null);
                BlockData blockData = fakeBlockState.getFakeBlockDisplay().getDestroyParticleData();
                e.setData(blockData);
            }
        }
    }

}

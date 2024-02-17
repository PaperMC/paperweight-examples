package de.verdox.mccreativelab.world.block;

import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import de.verdox.mccreativelab.world.sound.ReplacedSoundGroups;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.world.WorldEffectEvent;
import io.papermc.paper.event.world.WorldSoundEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.world.GenericGameEvent;

public class CustomBlockSounds implements Listener {
    @EventHandler
    public void gameEvent(GenericGameEvent genericGameEvent) {
        if (!(genericGameEvent.getEntity() instanceof Player player))
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

    @EventHandler
    public void armSwingWhileDigging(PlayerArmSwingEvent e) {
        if (!e.getAnimationType().equals(PlayerAnimationType.ARM_SWING))
            return;

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

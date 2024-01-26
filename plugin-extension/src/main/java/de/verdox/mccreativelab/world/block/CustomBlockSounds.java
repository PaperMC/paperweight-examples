package de.verdox.mccreativelab.world.block;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.world.WorldEffectEvent;
import io.papermc.paper.event.world.WorldSoundEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.world.GenericGameEvent;

public class CustomBlockSounds implements Listener {

    @EventHandler
    public void gameEvent(GenericGameEvent genericGameEvent){
        if(!(genericGameEvent.getEntity() instanceof Player player))
            return;
        if(genericGameEvent.getEvent().equals(GameEvent.BLOCK_PLACE)){
            System.out.println("Block place: "+genericGameEvent.getLocation().getBlock().getType());
        }
        if(genericGameEvent.getEvent().equals(GameEvent.STEP)){
            System.out.println("Block step: "+genericGameEvent.getLocation().getBlock().getType());
        }
        if(genericGameEvent.getEvent().equals(GameEvent.BLOCK_DESTROY)){
            System.out.println("Block destroy: "+genericGameEvent.getLocation().getBlock().getType());
        }
    }

    @EventHandler
    public void armSwingWhileDigging(PlayerArmSwingEvent e){
        if(!e.getAnimationType().equals(PlayerAnimationType.ARM_SWING))
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

        Location blockLoc = e.getSoundLocation().clone().add(0,-1,0);
        System.out.println("Step Sound [" + e.getSound() + "] at " + blockLoc.getBlock().getType());
    }

    @EventHandler
    public void replaceEffect(WorldEffectEvent e) {
        if (e.getExcept() == null)
            return;

        if (e.getEffect().equals(Effect.STEP_SOUND)) {
            FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getSoundLocation().toBlockLocation(), false);
            if(fakeBlockState != null){
                e.setExcept(null);
                BlockData blockData = fakeBlockState.getFakeBlockDisplay().getDestroyParticleData();
                e.setData(blockData);
            }
        }
    }

}

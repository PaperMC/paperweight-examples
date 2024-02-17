package de.verdox.mccreativelab.world.block;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.world.WorldEffectEvent;
import io.papermc.paper.event.world.WorldSoundEvent;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeBlockSoundManager implements Listener {
    public static final EntityMetadataPredicate.TickDelay DIGGING_SOUND_DELAY = new EntityMetadataPredicate.TickDelay("DiggingSoundDelay", 4);
    public static final EntityMetadataPredicate.DistanceTravelled STEP_DISTANCE_SOUND_DELAY = new EntityMetadataPredicate.DistanceTravelled("StepSoundDelay", 2.2);
    public static final @NotNull SoundGroup DEFAULT_SOUND_GROUP = Bukkit.createBlockData(Material.STONE)
                                                                        .getSoundGroup();

    /**
     * This method is used to determine whether a block has no sound due to resource pack patching.
     * Normally this returns true for glass or ice blocks since we use them to create the HitBoxes for our fake blocks.
     *
     * @return true if it has no sound
     */
    public static boolean isBlockWithoutStandardSound(Block block) {
        return isBlockWithoutStandardSound(block.getBlockData());
    }

    public static boolean isBlockWithoutStandardSound(BlockData data) {
        return MCCreativeLabExtension.getReplacedSoundGroups().wasSoundReplaced(data.getSoundGroup());
    }

    @EventHandler
    public void simulateFakeBlockWalkSound(EntityMoveEvent e) {
        simulateWalkSound(e.getEntity(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void simulateFakeBlockWalkSound(PlayerMoveEvent e) {
        simulateWalkSound(e.getPlayer(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void simulateFakeBlockWalkSound(PlayerJumpEvent e) {
        simulateWalkSound(e.getPlayer(), e.getFrom(), e.getTo(), false);
    }

    @EventHandler
    public void resetDiggingDelayOnBlockBreak(BlockBreakEvent e) {
        DIGGING_SOUND_DELAY.reset(e.getPlayer());
    }

    @EventHandler
    public void replaceSounds(WorldSoundEvent e){

    }

    @EventHandler
    public void replaceEffect(WorldEffectEvent e){

    }

/*    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void simulateBlockBreakWhenCreativeOrForBlocksWithoutStandardSounds(BlockBreakEvent e) {
        if (!isBlockWithoutStandardSound(e.getBlock()))
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(e.getPlayer()
                                                                                             .getLocation(), false);
        if(e.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            simulateBreakSound(e.getBlock(), fakeBlockState);
        }
        else if(fakeBlockState == null)
            simulateBreakSound(e.getBlock(), null);

    }*/

    public static void simulateDiggingSound(Player player, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        if (!DIGGING_SOUND_DELAY.isAllowed(player))
            return;
        if(!FakeBlockSoundManager.isBlockWithoutStandardSound(block) && fakeBlockState == null)
            return;
        Wrappers.SoundGroup soundGroup = getSoundGroup(block, fakeBlockState);
        net.kyori.adventure.sound.Sound sound = soundGroup.getStepSound().asSound(net.kyori.adventure.sound.Sound.Source.BLOCK, 0.15f, soundGroup.getPitch() * 0.3F);
        player.playSound(sound, block.getX(), block.getY(), block.getZ());
        DIGGING_SOUND_DELAY.reset(player);
    }

    public static void simulateBreakSound(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        Wrappers.SoundGroup soundGroup = getSoundGroup(block, fakeBlockState);
        net.kyori.adventure.sound.Sound sound = soundGroup.getBreakSound()
                                                          .asSound(net.kyori.adventure.sound.Sound.Source.BLOCK, (soundGroup.getVolume() + 1.0F) / 2.0F, soundGroup.getPitch() * 0.8F);
        block.getWorld().playSound(sound, block.getX(), block.getY(), block.getZ());
    }

    public static void simulateBlockPlaceSound(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        Wrappers.SoundGroup soundGroup = getSoundGroup(block, fakeBlockState);
        net.kyori.adventure.sound.Sound sound = soundGroup.getPlaceSound().asSound(net.kyori.adventure.sound.Sound.Source.BLOCK, (soundGroup.getVolume() + 1.0F) / 2.0F, soundGroup.getPitch() * 0.8F);
        block.getWorld().playSound(sound, block.getX(), block.getY(), block.getZ());
    }

    public void simulateWalkSound(Entity walkingEntity, Location from, Location walkingTo) {
/*        simulateWalkSound(walkingEntity, from, walkingTo, true);*/
    }

    public void simulateWalkSound(Entity walkingEntity, Location from, Location walkingTo, boolean checkForDelay) {
/*        if (!STEP_DISTANCE_SOUND_DELAY.isAllowed(walkingEntity) && checkForDelay)
            return;
        Block block = from.clone().add(0, -1, 0).getBlock();

        //TODO: Der Spieler kann über die Kante eines Blockes hinaus stehen und steht damit sozusagen in der Luft. Er kann maximal um 0.3 aus dem Block heraus.
        // Darauf muss extra geprüft werden.
        // Ist unter dem Spieler AIR?
        // Wenn ja: Prüfe über den Koordinaten Offset je nachdem auf welchem relative block er stehen muss. Dann schaue welchen sound dieser block hat.


        Sound.Source soundCategory = walkingEntity instanceof Player ? Sound.Source.PLAYER : walkingEntity instanceof Enemy ? Sound.Source.HOSTILE : Sound.Source.NEUTRAL;
        float pitch = walkingEntity instanceof Player ? 1 : 0.5f;
        if (isBlockWithoutStandardSound(block) && walkingEntity.getLocation().getY() == walkingEntity.getLocation()
                                                                                                     .blockY() && walkingEntity.isOnGround() && !(walkingEntity instanceof Player player && player.isSneaking())) {
            FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
            net.kyori.adventure.sound.Sound sound = getSoundGroup(block, fakeBlockState).getStepSound().asSound(soundCategory, 0.15F, 1.0F);
            block.getWorld().playSound(sound, block.getX(), block.getY(), block.getZ());
        }
        STEP_DISTANCE_SOUND_DELAY.reset(walkingEntity);*/
    }

    public static Wrappers.SoundGroup getSoundGroup(@NotNull Block block, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
        if(fakeBlockState != null && fakeBlockState.getFakeBlockSoundGroup() != null)
            return fakeBlockState.getFakeBlockSoundGroup().asSoundGroup();
        else
            return MCCreativeLabExtension.getReplacedSoundGroups().getSoundGroup(block.getBlockData());
    }
}

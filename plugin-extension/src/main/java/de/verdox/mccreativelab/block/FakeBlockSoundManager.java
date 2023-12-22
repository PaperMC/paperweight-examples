package de.verdox.mccreativelab.block;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.sound.ReplacedSoundGroups;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.block.Block;
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
    //TODO: Abstrahieren, sodass man theoretisch für jede Block Gruppe sowas machen kann
    private static final EntityMetadataPredicate.TickDelay DIGGING_SOUND_DELAY = new EntityMetadataPredicate.TickDelay("DiggingSoundDelay", 4);
    private static final EntityMetadataPredicate.DistanceTravelled STEP_DISTANCE_SOUND_DELAY = new EntityMetadataPredicate.DistanceTravelled("StepSoundDelay", 2.2);
    public static final @NotNull SoundGroup DEFAULT_SOUND_GROUP = Bukkit.createBlockData(Material.STONE)
                                                                        .getSoundGroup();

    /**
     * This method is used to determine whether a block has no sound due to resource pack patching.
     * Normally this returns true for glass or ice blocks since we use them to create the HitBoxes for our fake blocks.
     *
     * @return true if it has no sound
     */
    public static boolean isBlockWithoutStandardSound(Block block) {
        //TODO: Use BlockData of FakeHitBoxes for this?
        return ReplacedSoundGroups.wasSoundReplaced(block.getBlockSoundGroup());
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void simulateBlockBreakWhenCreative(BlockBreakEvent e) {
        if (!isBlockWithoutStandardSound(e.getBlock()))
            return;
        if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(e.getPlayer()
                                                                                             .getLocation(), false);
        simulateBreakSound(e.getBlock(), fakeBlockState);
    }

    public static void simulateDiggingSound(Player player, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        if (!DIGGING_SOUND_DELAY.isAllowed(player))
            return;
        net.kyori.adventure.sound.Sound sound = getSoundGroup(block, fakeBlockState).getStepSound()
                                                                                    .asSound(net.kyori.adventure.sound.Sound.Source.BLOCK, 0.25f, 0.7f);
        block.getWorld().playSound(sound);
        DIGGING_SOUND_DELAY.reset(player);
    }

    public static void simulateBreakSound(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        net.kyori.adventure.sound.Sound sound = getSoundGroup(block, fakeBlockState).getBreakSound()
                                                                                    .asSound(net.kyori.adventure.sound.Sound.Source.BLOCK);
        block.getWorld().playSound(sound);
    }

    public static void simulateBlockPlaceSound(Player player, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        net.kyori.adventure.sound.Sound sound = getSoundGroup(block, fakeBlockState).getPlaceSound()
                                                                                    .asSound(net.kyori.adventure.sound.Sound.Source.BLOCK, 0.7f);
        block.getWorld().playSound(sound);
    }

    private void simulateWalkSound(Entity walkingEntity, Location from, Location walkingTo) {
        simulateWalkSound(walkingEntity, from, walkingTo, true);
    }

    private void simulateWalkSound(Entity walkingEntity, Location from, Location walkingTo, boolean checkForDelay) {
        if (!STEP_DISTANCE_SOUND_DELAY.isAllowed(walkingEntity) && checkForDelay)
            return;
        Block block = from.clone().add(0, -1, 0).getBlock();

        //TODO: Der Spieler kann über die Kante eines Blockes hinaus stehen und steht damit sozusagen in der Luft. Er kann maximal um 0.3 aus dem Block heraus.
        // Darauf muss extra geprüft werden.
        // Ist unter dem Spieler AIR?
        // Wenn ja: Prüfe über den Koordinaten Offset je nachdem auf welchem relative block er stehen muss. Dann schaue welchen sound dieser block hat.


        Sound.Source soundCategory = walkingEntity instanceof Player ? Sound.Source.PLAYER : walkingEntity instanceof Enemy ? Sound.Source.HOSTILE : Sound.Source.NEUTRAL;
        float pitch = walkingEntity instanceof Player ? 1 : 0.5f;
        if (isBlockWithoutStandardSound(block) && walkingEntity.getLocation().getY() == walkingEntity.getLocation()
                                                                                                     .blockY() && !(walkingEntity instanceof Player player && player.isSneaking())) {
            FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
            net.kyori.adventure.sound.Sound sound = getSoundGroup(block, fakeBlockState).getStepSound()
                                                                                        .asSound(soundCategory, 0.25f, pitch);
            block.getWorld().playSound(sound);
        }
        STEP_DISTANCE_SOUND_DELAY.reset(walkingEntity);
    }

    private static Wrappers.SoundGroup getSoundGroup(@NotNull Block block, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState == null)
            return ReplacedSoundGroups.getSoundGroup(block.getBlockData());
        else {
            if (fakeBlockState.getFakeBlockSoundGroup() == null)
                return Wrappers.of(DEFAULT_SOUND_GROUP);
            return fakeBlockState.getFakeBlockSoundGroup().asSoundGroup();
        }
    }
}

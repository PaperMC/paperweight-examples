package de.verdox.mccreativelab.block;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FakeBlockSoundManager implements Listener {
    //TODO: Abstrahieren, sodass man theoretisch für jede Block Gruppe sowas machen kann
    private static final EntityMetadataPredicate.TickDelay DIGGING_SOUND_DELAY = new EntityMetadataPredicate.TickDelay("DiggingSoundDelay", 4);
    private static final EntityMetadataPredicate.DistanceTravelled STEP_DISTANCE_SOUND_DELAY = new EntityMetadataPredicate.DistanceTravelled("StepSoundDelay", 2.2);

    /**
     * This method is used to determine whether a block has no sound due to resource pack patching.
     * Normally this returns true for glass or ice blocks since we use them to create the HitBoxes for our fake blocks.
     *
     * @return true if it has no sound
     */
    public static boolean isBlockWithoutStandardSound(Block block) {
        return block.getBlockSoundGroup().getStepSound().equals(Sound.BLOCK_GLASS_STEP);
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
    public void simulateFakeBlockWalkSound(PlayerJumpEvent e){
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
        if (fakeBlockState != null) {
            FakeBlock.FakeBlockSoundGroup fakeBlockSoundGroup = fakeBlockState.getFakeBlockSoundGroup();
            String soundToPlayKey = fakeBlockSoundGroup != null ? fakeBlockSoundGroup.getDigSound().key().toString() : Sound.BLOCK_STONE_STEP.getKey().toString();
            block.getWorld().playSound(block.getLocation(), soundToPlayKey, SoundCategory.BLOCKS, 0.25f, 0.5f);
        } else
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_STEP, SoundCategory.BLOCKS, 0.25f, 0.5f);
        DIGGING_SOUND_DELAY.reset(player);
    }

    public static void simulateBreakSound(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState != null) {
            FakeBlock.FakeBlockSoundGroup fakeBlockSoundGroup = fakeBlockState.getFakeBlockSoundGroup();
            String soundToPlayKey = fakeBlockSoundGroup != null ? fakeBlockSoundGroup.getBreakSound().key()
                                                                                     .toString() : Sound.BLOCK_STONE_BREAK
                .getKey().toString();
            block.getWorld()
                 .playSound(block.getLocation(), soundToPlayKey, SoundCategory.BLOCKS, block
                     .getBlockSoundGroup().getVolume(), block.getBlockSoundGroup().getPitch());
        } else
            block.getWorld()
                 .playSound(block.getLocation(), "minecraft:block.glass.custom.break", SoundCategory.BLOCKS, block
                     .getBlockSoundGroup().getVolume(), block.getBlockSoundGroup().getPitch());
    }

    public static void simulateBlockPlaceSound(Player player, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState != null) {
            FakeBlock.FakeBlockSoundGroup fakeBlockSoundGroup = fakeBlockState.getFakeBlockSoundGroup();
            String soundToPlayKey = fakeBlockSoundGroup != null ? fakeBlockSoundGroup.getPlaceSound().key()
                                                                                     .toString() : Sound.BLOCK_STONE_PLACE
                .getKey().toString();
            block.getWorld()
                 .playSound(block.getLocation(), soundToPlayKey, SoundCategory.BLOCKS, block.getBlockSoundGroup()
                                                                                            .getVolume(), 0.7f);
        } else
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, block
                .getBlockSoundGroup().getVolume(), 0.7f);
    }

    private void simulateWalkSound(Entity walkingEntity, Location from, Location walkingTo){
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


        SoundCategory soundCategory = walkingEntity instanceof Player ? SoundCategory.PLAYERS : walkingEntity instanceof Enemy ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
        float pitch = walkingEntity instanceof Player ? 1 : 0.5f;
        if (isBlockWithoutStandardSound(block) && walkingEntity.getLocation().getY() == walkingEntity.getLocation()
                                                                                                     .blockY() && !(walkingEntity instanceof Player player && player.isSneaking())) {

            FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
            if (fakeBlockState != null) {
                FakeBlock.FakeBlockSoundGroup fakeBlockSoundGroup = fakeBlockState.getFakeBlockSoundGroup();
                String soundToPlayKey = fakeBlockSoundGroup != null ? fakeBlockSoundGroup.getPlaceSound().key().toString() : Sound.BLOCK_STONE_STEP.getKey().toString();
                walkingTo.getWorld().playSound(walkingTo, soundToPlayKey, soundCategory, 0.25f, pitch);
            } else
                walkingTo.getWorld().playSound(walkingTo, Sound.BLOCK_STONE_STEP, soundCategory, 0.25f, pitch);
        }
        STEP_DISTANCE_SOUND_DELAY.reset(walkingEntity);
    }
}

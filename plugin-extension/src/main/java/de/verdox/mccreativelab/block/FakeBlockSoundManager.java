package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class FakeBlockSoundManager implements Listener {

    private static final EntityMetadataPredicate.TickDelay DIGGING_SOUND_DELAY = new EntityMetadataPredicate.TickDelay("DiggingSoundDelay", 4);
    private static final EntityMetadataPredicate.DistanceTravelled STEP_DISTANCE_SOUND_DELAY = new EntityMetadataPredicate.DistanceTravelled("StepSoundDelay", 2);

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

    public static void simulateDiggingSound(Player player, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        if(!DIGGING_SOUND_DELAY.isAllowed(player))
            return;
        if (fakeBlockState != null) {
            //TODO: Play custom digging sound
        } else
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_STEP, SoundCategory.BLOCKS, 0.25f, 0.5f);
        DIGGING_SOUND_DELAY.reset(player);
    }

    public static void simulateBreakSound(Player player, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState != null) {
            //TODO: Play custom break sound
        } else
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.25f, 0.7f);
    }

    private void simulateWalkSound(Entity walkingEntity, Location from, Location walkingTo) {
        if(!STEP_DISTANCE_SOUND_DELAY.isAllowed(walkingEntity))
            return;
        Block block = from.clone().add(0, -1, 0).getBlock();

        //TODO: Der Spieler kann über die Kante eines Blockes hinaus stehen und steht damit sozusagen in der Luft. Er kann maximal um 0.3 aus dem Block heraus.
        // Darauf muss extra geprüft werden.
        // Ist unter dem Spieler AIR?
        // Wenn ja: Prüfe über den Koordinaten Offset je nachdem auf welchem relative block er stehen muss. Dann schaue welchen sound dieser block hat.


        SoundCategory soundCategory = walkingEntity instanceof Player ? SoundCategory.PLAYERS : walkingEntity instanceof Enemy ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
        float pitch = walkingEntity instanceof Player ? 1 : 0.5f;

        if (walkingEntity instanceof Player player) {
            System.out.println(walkingEntity.getLocation().getX() + " " + walkingEntity.getLocation()
                                                                                       .getY() + " " + walkingEntity
                .getLocation().getZ());
        }
        if (isBlockWithoutStandardSound(block) && walkingEntity.getLocation().getY() == walkingEntity.getLocation()
                                                                                                     .blockY() && !(walkingEntity instanceof Player player && player.isSneaking())) {
            walkingTo.getWorld().playSound(walkingTo, Sound.BLOCK_STONE_STEP, soundCategory, 0.25f, pitch);
        }
        STEP_DISTANCE_SOUND_DELAY.reset(walkingEntity);
    }
}

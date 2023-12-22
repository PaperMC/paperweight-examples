package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class FakeBlockUtil {
    public static void simulateBlockBreakWithParticlesAndSound(@Nullable FakeBlock.FakeBlockState fakeBlockState, Block block) {
        if (fakeBlockState != null) {
            block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().clone()
                                                                      .add(0.5, 0.5, 0.5), 40, 0.1, 0.1, 0.1, fakeBlockState
                .getFakeBlockDisplay().getDestroyParticles());
            sendBlockDestruction(block.getLocation(), fakeBlockState.getFakeBlockDisplay().getDestroyParticles());
        } else
            sendBlockDestruction(block.getLocation(), block.getBlockData());

        if (FakeBlockSoundManager.isBlockWithoutStandardSound(block))
            FakeBlockSoundManager.simulateBreakSound(block, fakeBlockState);
    }

    public static void moveBlock(Location location, ItemDisplay itemDisplay, FakeBlock.FakeBlockState fakeBlockState, Vector moveDirection) {
        ItemDisplay animationDisplay = (ItemDisplay) location.getWorld()
                                                             .spawnEntity(itemDisplay.getLocation(), EntityType.ITEM_DISPLAY);
        animationDisplay.setItemStack(itemDisplay.getItemStack().clone());

        Transformation transformation = animationDisplay.getTransformation();
        transformation.getTranslation()
                      .add(new Vector3f(moveDirection.getBlockX(), moveDirection.getBlockY(), moveDirection.getBlockZ()));
        animationDisplay.setInterpolationDuration(4);
        animationDisplay.setInterpolationDelay(-1);
        animationDisplay.setTransformation(transformation);
        Bukkit.getScheduler().runTaskLater(MCCreativeLabExtension.getInstance(), animationDisplay::remove, 4L);

        FakeBlockStorage.setFakeBlockState(location, null, false);
        Location newBlockLocation = location.clone().add(moveDirection);
        FakeBlockStorage.setFakeBlockState(newBlockLocation, fakeBlockState, false);
    }

    @ApiStatus.Experimental
    public static void sendBlockDestruction(Location location, BlockData fakeBukkitParticles) {
        Block block = location.getBlock();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (playerNotInEffectRange(onlinePlayer, location.getBlock())) continue;

            onlinePlayer.sendBlockChange(location, fakeBukkitParticles);
            if (block.getType().equals(Material.FIRE))
                block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, block.getBlockData());
            else
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
        }
    }

    public static boolean playerNotInEffectRange(Player onlinePlayer, Block block) {
        if (!onlinePlayer.getWorld().equals(block.getWorld()))
            return true;

        double xDistance = (double) block.getX() - onlinePlayer.getX();
        double yDistance = (double) block.getY() - onlinePlayer.getY();
        double zDistance = (double) block.getZ() - onlinePlayer.getZ();

        return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance >= 1024.0D;
    }

    public static void spawnDiggingParticles(Block block, FakeBlock.FakeBlockState fakeBlockState, Vector normalOfBlockFace) {
        float xOffset = 0;
        float yOffset = 0;
        float zOffset = 0;

        float xPos = 0;
        float yPos = 0;
        float zPos = 0;

        float offset = 0.15f;
        float pos = 0.5f;

        if (normalOfBlockFace.getBlockX() != 0) {
            yOffset = offset;
            zOffset = offset;

            xPos = pos * normalOfBlockFace.getBlockX();
        } else if (normalOfBlockFace.getBlockY() != 0) {
            xOffset = offset;
            zOffset = offset;

            yPos = pos * normalOfBlockFace.getBlockY();
        } else if (normalOfBlockFace.getBlockZ() != 0) {
            xOffset = offset;
            yOffset = offset;

            zPos = pos * normalOfBlockFace.getBlockZ();
        }

        block.getWorld()
             .spawnParticle(Particle.BLOCK_DUST,
                 block.getLocation().clone()
                      .add(0.5, 0.5, 0.5)
                      .add(xPos, yPos, zPos)
                      .add(normalOfBlockFace.multiply(0.05)), 1, xOffset, yOffset, zOffset, 0.01, fakeBlockState
                     .getFakeBlockDisplay().getDestroyParticles());
    }

    public static void removeFakeBlockIfPossible(Block block) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
        if (fakeBlockState == null)
            return;
        FakeBlockStorage.setFakeBlockState(block.getLocation(), null, false);
    }
}

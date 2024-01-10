package de.verdox.mccreativelab.blockbreak;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.block.*;
import de.verdox.mccreativelab.util.BlockUtil;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBreakSpeedModifier implements Listener {
    private static final EntityMetadataPredicate.TickDelay DELAY_BETWEEN_BREAK_PARTICLES = new EntityMetadataPredicate.TickDelay("DiggingParticlesDelay", 2);
    private static final EntityMetadataPredicate.TickDelay DELAY_BETWEEN_BLOCK_BREAKS = new EntityMetadataPredicate.TickDelay("BlockBreakDelay", 6);
    private static final Map<Player, BlockBreakProgress> map = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getAction().isRightClick())
            stopBlockBreakAction(e.getPlayer());
        if(e.getClickedBlock() != null && e.getAction().isLeftClick())
            startBlockBreakAction(e.getPlayer(), e.getClickedBlock(), e.getBlockFace(), e);
    }

    @EventHandler
    public void onStartDigging(BlockDamageEvent e) {
        startBlockBreakAction(e.getPlayer(), e.getBlock(), e.getBlockFace(), e);
    }

    @EventHandler
    public void onPlayerDigAnimation(PlayerArmSwingEvent e){
        Player player = e.getPlayer();
        if (map.containsKey(player)){
            BlockBreakProgress blockBreakProgress = map.get(player);
            FakeBlockSoundManager.simulateDiggingSound(player, blockBreakProgress.block, blockBreakProgress.fakeBlockState);
        }
        else if(player.hasMetadata("isBreakingNormalBlock")){
            Block block = (Block) player.getMetadata("isBreakingNormalBlock").get(0).value();
            if(block != null && FakeBlockSoundManager.isBlockWithoutStandardSound(block))
                FakeBlockSoundManager.simulateDiggingSound(player, block, null);
        }

    }

    @EventHandler
    public void onStopDigging(BlockDamageAbortEvent e) {
        stopBlockBreakAction(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        stopBlockBreakAction(e.getPlayer());
    }

    @EventHandler
    public void stopDiggingOnQuit(PlayerQuitEvent e) {
        stopBlockBreakAction(e.getPlayer());
    }

    @EventHandler
    public void tickPlayers(ServerTickEndEvent e) {
        Bukkit.getOnlinePlayers().forEach(BlockBreakSpeedModifier::tick);
    }

    public static void startBlockBreakAction(Player player, Block block, BlockFace blockFace, Cancellable cancellable) {
        if (map.containsKey(player))
            stopBlockBreakAction(player);

        Material bukkitMaterial = block.getType();
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block
            .getLocation(), false);
        float customBlockHardness = -1;

        if (fakeBlockState != null)
            customBlockHardness = fakeBlockState.getProperties().getHardness();
        else if (MCCreativeLabExtension.getBlockBreakSpeedSettings().hasCustomBlockHardness(bukkitMaterial))
            customBlockHardness = MCCreativeLabExtension.getBlockBreakSpeedSettings().getCustomBlockHardness(block.getType());

        if (customBlockHardness == -1) {
            player.setMetadata("isBreakingNormalBlock", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), block));
            return;
        }

        cancellable.setCancelled(true);
        map.put(player, new BlockBreakProgress(player, block, customBlockHardness, blockFace, fakeBlockState));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2, -1, false, false, false));
        FakeBlockRegistry.fakeBlockDamage.sendBlockDamage(block, 0);
        FakeBlockSoundManager.simulateDiggingSound(player, block, fakeBlockState);
    }

    public static void stopBlockBreakAction(Player player) {
        if (player.hasMetadata("isBreakingNormalBlock"))
            player.removeMetadata("isBreakingNormalBlock", MCCreativeLabExtension.getInstance());
        if (!map.containsKey(player))
            return;
        map.remove(player).resetBlockDamage();
    }

    public static void tick(Player player) {
        if (player.getInventory().getItemInMainHand().getType().name().contains("AXE") && !player.hasMetadata("isBreakingNormalBlock"))
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 1, -1, false, false, false));

        if(player.hasMetadata("isBreakingNormalBlock")){
            Block block = (Block) player.getMetadata("isBreakingNormalBlock").get(0).value();
            if(block != null && FakeBlockSoundManager.isBlockWithoutStandardSound(block))
                FakeBlockSoundManager.simulateDiggingSound(player, block, null);
        }

        if (!map.containsKey(player))
            return;
        var data = map.get(player);
        data.incrementTicks();
    }

    public static class BlockBreakProgress {
        private final Player player;
        private final Block block;
        private final float hardness;
        private final boolean usesVanillaHardness;
        private BlockFace blockFace;
        @Nullable
        private final FakeBlock.FakeBlockState fakeBlockState;
        private float damageTaken;
        private int lastStage = -1;
        private final int[] idsPerStage = new int[10];

        public BlockBreakProgress(Player player, Block block, float hardness, BlockFace blockFace, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
            this.player = player;
            this.block = block;
            this.hardness = hardness;
            this.blockFace = blockFace;
            this.fakeBlockState = fakeBlockState;
            this.usesVanillaHardness = this.hardness == block.getType().getHardness();
        }

        public void incrementTicks() {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 1, -1, false, false, false));
            if(!DELAY_BETWEEN_BLOCK_BREAKS.isAllowed(player))
                return;

            var damageThisTick = BlockUtil.getBlockDestroyProgress(player, block.getState(), fakeBlockState);
            damageTaken += damageThisTick;
            damageTaken = Math.min(1, damageTaken);

            int stage = (int) (damageTaken * 10) - 1;

            if (stage != lastStage) {
                if (stage < 0 || stage > 9) {
                    return;
                }

                for (int i = lastStage + 1; i <= stage; i++) {
                    var entityID = getDestructionID(i);
                    sendBlockDamage(i, entityID);
                }
                lastStage = stage;
            }


            if (stage == 9) {
                FakeBlockUtil.simulateBlockBreakWithParticlesAndSound(fakeBlockState, block);
                player.breakBlock(block);
                stopBlockBreakAction(player);
                DELAY_BETWEEN_BLOCK_BREAKS.reset(player);
            } else if (FakeBlockSoundManager.isBlockWithoutStandardSound(block)) {
                FakeBlockSoundManager.simulateDiggingSound(player, block, fakeBlockState);
                if (fakeBlockState != null && DELAY_BETWEEN_BREAK_PARTICLES.isAllowed(player)) {

                    RayTraceResult rayTraceResult = player.rayTraceBlocks(7);
                    BlockFace faceToSpawnParticles = this.blockFace;
                    if (rayTraceResult != null && block.equals(rayTraceResult.getHitBlock()) && rayTraceResult.getHitBlockFace() != null)
                        faceToSpawnParticles = rayTraceResult.getHitBlockFace();
                    Vector normalOfBlockFace = faceToSpawnParticles.getDirection();

                    if (!fakeBlockState.getFakeBlockDisplay().isReusingMinecraftBlockstate())
                        FakeBlockUtil.spawnDiggingParticles(player, block, fakeBlockState, normalOfBlockFace);

                    DELAY_BETWEEN_BREAK_PARTICLES.reset(player);
                }
            }
        }

        public void resetBlockDamage() {
            for (int id : idsPerStage) {
                sendBlockDamage(-1, id);
            }
        }

        public int getDestructionID(int stage) {
            if (idsPerStage[stage] == 0)
                idsPerStage[stage] = ThreadLocalRandom.current().nextInt(1000);
            return idsPerStage[stage];
        }

        private void sendBlockDamage(int stage, int entityId) {
            float progress;
            if (stage == -1)
                progress = 0;
            else
                progress = stage * (1f / 9);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                if (onlinePlayer.getEntityId() == entityId)
                    continue;
                if (FakeBlockUtil.playerNotInEffectRange(onlinePlayer, block)) continue;

                player.sendBlockDamage(block.getLocation(), progress, entityId);
            }
            if (fakeBlockState != null)
                FakeBlockRegistry.fakeBlockDamage.sendBlockDamage(block, stage);
        }

        // This formula is taken from
        // https://minecraft.fandom.com/wiki/Breaking
        public float calculateBreakTime() {
            double multiplier = 1.0D;
            float breakTime = hardness;
            ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);
            BlockState blockState = block.getState();
            BlockData blockData = blockState.getBlockData();

            boolean isPreferredTool = block.isPreferredTool(hand);

            double blockDamage;
            if (usesVanillaHardness) {
                return blockState.getBlock().getBreakSpeed(player);
            } else {
                boolean requiresCorrectToolsForDrops = blockData.requiresCorrectToolForDrops();

                multiplier = block.getDestroySpeed(hand, false);
                if (fakeBlockState != null) {
                    requiresCorrectToolsForDrops = fakeBlockState.getProperties().isRequiresCorrectToolForDrops();
                    isPreferredTool = fakeBlockState.getFakeBlock()
                                                    .isPreferredTool(fakeBlockState, block, player, hand);
                    multiplier = fakeBlockState.getFakeBlock().getDestroySpeed(fakeBlockState, block, hand);
                }


                if (isPreferredTool) {
                    // canHarvest
                    if (requiresCorrectToolsForDrops) {
                        int efficiencyLevel = getEnchantmentLevel(player, Enchantment.DIG_SPEED);
                        if (efficiencyLevel > 0)
                            multiplier += (efficiencyLevel ^ 2) + 1;
                    } else
                        multiplier = 1.0;
                }

                // Haste effect
                if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING))
                    multiplier *= 0.2D * player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier() + 1.0D;
                // water check
                if (player.isInWater() && hasEnchantmentLevel(player, Enchantment.WATER_WORKER))
                    multiplier /= 5.0D;
                // in air check
                if (!player.isOnGround())
                    multiplier /= 5.0D;

                blockDamage = multiplier / breakTime;

                if (isPreferredTool)
                    blockDamage /= 30.0;
                else
                    blockDamage /= 100.0;
            }
            breakTime = blockDamage >= 1 ? 0 : (int) Math.ceil(1.0D / blockDamage);
            return (1 / breakTime);
        }
    }

    private static boolean hasEnchantmentLevel(Player player, Enchantment enchantment) {
        return getEnchantmentLevel(player, enchantment) > 0;
    }

    private static int getEnchantmentLevel(Player player, Enchantment enchantment) {
        int level = 0;
        for (EquipmentSlot activeSlot : enchantment.getActiveSlots()) {

            ItemStack stack = player.getInventory().getItem(activeSlot);
            int foundLevel = stack.getEnchantmentLevel(enchantment);
            if (foundLevel > level)
                level = foundLevel;
        }
        return level;
    }
}

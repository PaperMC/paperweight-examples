package de.verdox.mccreativelab.world.block.customhardness;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.registry.Reference;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockRegistry;
import de.verdox.mccreativelab.world.block.FakeBlockSoundManager;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.world.block.event.StartBlockBreakEvent;
import de.verdox.mccreativelab.world.block.util.FakeBlockUtil;
import de.verdox.mccreativelab.util.BlockUtil;
import de.verdox.mccreativelab.util.EntityMetadataPredicate;
import de.verdox.mccreativelab.world.item.FakeItem;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class BlockBreakSpeedModifier implements Listener {
    private static final EntityMetadataPredicate.TickDelay DELAY_BETWEEN_BREAK_PARTICLES = new EntityMetadataPredicate.TickDelay("DiggingParticlesDelay", 2);
    private static final EntityMetadataPredicate.TickDelay DELAY_BETWEEN_BLOCK_BREAKS = new EntityMetadataPredicate.TickDelay("BlockBreakDelay", 5);
    private static final EntityMetadataPredicate.TickDelay DELAY_ARM_SWING_DETECTION = new EntityMetadataPredicate.TickDelay("ArmSwingDetection", 1);
    private static final Map<Player, BlockBreakProgress> map = new HashMap<>();
    private static final Map<Block, Set<Player>> blockBrokenToPlayerMapping = new HashMap<>();
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("oneblock", "fake_block_break_effect");
    private static final AttributeModifier NO_BLOCK_BREAK_MODIFIER = new AttributeModifier(MODIFIER_KEY, -1, AttributeModifier.Operation.ADD_NUMBER);

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getAction().isRightClick())
            stopBlockBreakAction(e.getPlayer());
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            return;
        if (e.getClickedBlock() != null && e.getAction().isLeftClick())
            startBlockBreakAction(e.getPlayer(), e.getClickedBlock(), e.getBlockFace(), e);
    }

/*    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cachePlayerInteractionsToPreventAnimationFalsePositives(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        player.setMetadata("lastInteraction", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), e.getAction()));
    }*/

/*    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void detectBlockBreakOnArmSwing(PlayerArmSwingEvent e) {
        Player player = e.getPlayer();

        if (!player.hasMetadata("lastInteraction"))
            return;
        Action action = (Action) player.getMetadata("lastInteraction").get(0).value();
        if (!Action.LEFT_CLICK_BLOCK.equals(action)) {
            return;
        }

        RayTraceResult rayTraceResult = e.getPlayer().rayTraceBlocks(7);
        if (rayTraceResult == null) {
            stopBlockBreakAction(e.getPlayer());
            return;
        }



        Block targetBlock = rayTraceResult.getHitBlock();
        BlockFace hitBlockFace = rayTraceResult.getHitBlockFace();

        if (map.containsKey(e.getPlayer()) || targetBlock == null || !e.getHand().equals(EquipmentSlot.HAND))
            return;

        if(!DELAY_ARM_SWING_DETECTION.isAllowed(e.getPlayer()))
            return;
        DELAY_ARM_SWING_DETECTION.reset(player);

        startBlockBreakAction(e.getPlayer(), targetBlock, hitBlockFace, e);
    }*/

    @EventHandler
    public void onStartDigging(BlockDamageEvent e) {
        startBlockBreakAction(e.getPlayer(), e.getBlock(), e.getBlockFace(), e);
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
    public void blockDamageProgress(BlockBreakProgressUpdateEvent e) {

    }

    @EventHandler
    public void tickPlayers(ServerTickEndEvent e) {
        Bukkit.getOnlinePlayers().forEach(BlockBreakSpeedModifier::tick);
    }

    public static void startBlockBreakAction(Player player, Block block, BlockFace blockFace, Cancellable cancellable) {
        if (map.containsKey(player))
            stopBlockBreakAction(player);

        Material bukkitMaterial = block.getType();
        ItemStack diggingItem = player.getInventory().getItemInMainHand();
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(CustomItemData.fromItemStack(diggingItem));
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block
            .getLocation(), false);
        float customBlockHardness = -1;

        if (fakeBlockState != null)
            customBlockHardness = fakeBlockState.getProperties().getHardness();
        else if (MCCreativeLabExtension.getBlockBreakSpeedSettings().hasCustomBlockHardness(bukkitMaterial))
            customBlockHardness = MCCreativeLabExtension.getBlockBreakSpeedSettings().getCustomBlockHardness(block.getType());
        else if (fakeItemReference != null && block.getBlockData().getDestroySpeed(diggingItem) != fakeItemReference.unwrapValue().getDestroySpeed(diggingItem, block, fakeBlockState)) {
            customBlockHardness = block.getBlockData().getMaterial().getHardness();
        }

        StartBlockBreakEvent startBlockBreakEvent = new StartBlockBreakEvent(player, fakeBlockState, block, customBlockHardness);
        startBlockBreakEvent.callEvent();
        customBlockHardness = startBlockBreakEvent.getHardness();

        if (customBlockHardness == -1) {
            player.setMetadata("isBreakingNormalBlock", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), block));
            return;
        }

        cancellable.setCancelled(true);
        map.put(player, new BlockBreakProgress(player, block, customBlockHardness, blockFace, fakeBlockState));
        blockBrokenToPlayerMapping.computeIfAbsent(block, block1 -> new HashSet<>()).add(player);

        applyBlockBreakModifier(player);
    }

    private static void applyBlockBreakModifier(Player player) {
        if (player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).getModifier(MODIFIER_KEY) == null) {
            player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).addTransientModifier(NO_BLOCK_BREAK_MODIFIER);
        }
    }

    private static void removeBlockModifier(Player player) {
        if (player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).getModifier(MODIFIER_KEY) != null) {
            player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).removeModifier(NO_BLOCK_BREAK_MODIFIER);
        }
    }

    public static void stopBlockBreakAtBlock(Block block) {
        if (!blockBrokenToPlayerMapping.containsKey(block))
            return;
        for (Player player : blockBrokenToPlayerMapping.get(block)) {
            stopBlockBreakAction(player);
        }
    }

    public static void stopBlockBreakAction(Player player) {
        if (player.hasMetadata("isBreakingNormalBlock"))
            player.removeMetadata("isBreakingNormalBlock", MCCreativeLabExtension.getInstance());
        removeBlockModifier(player);
        if (!map.containsKey(player))
            return;
        BlockBreakProgress blockBreakProgress = map.remove(player);
        blockBreakProgress.resetBlockDamage();
        if (blockBrokenToPlayerMapping.containsKey(blockBreakProgress.block)) {
            Set<Player> playersBreakingBlock = blockBrokenToPlayerMapping.get(blockBreakProgress.block);
            playersBreakingBlock.remove(player);
            if (playersBreakingBlock.isEmpty())
                blockBrokenToPlayerMapping.remove(blockBreakProgress.block);
        }

        blockBrokenToPlayerMapping.remove(blockBreakProgress.block);
        // We reset the arm swing detection here to reduce false positive detection
        DELAY_ARM_SWING_DETECTION.reset(player);
    }

    private static final Predicate<ItemStack> IS_TOOL = stack -> stack.getType().name().contains("AXE") || stack.getType().name().contains("SHOVEL") || stack.getType().name().contains("HOE") || stack.getType().name().contains("SWORD") || stack.getType().equals(Material.SHEARS);

    public static void tick(Player player) {
        if (IS_TOOL.test(player.getInventory().getItemInMainHand()) && !player.hasMetadata("isBreakingNormalBlock"))
            applyBlockBreakModifier(player);
        else if (!map.containsKey(player))
            removeBlockModifier(player);

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
            sendBlockDamage(0, getDestructionID(0));
        }

        public void incrementTicks() {
            //applyBlockBreakModifier(player);
            if (!DELAY_BETWEEN_BLOCK_BREAKS.isAllowed(player))
                return;


            var damageThisTick = BlockUtil.getBlockDestroyProgress(player, hardness, block.getState(), fakeBlockState);
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
                //FakeBlockSoundManager.simulateDiggingSound(player, block, fakeBlockState);
                if (fakeBlockState != null && DELAY_BETWEEN_BREAK_PARTICLES.isAllowed(player)) {

                    RayTraceResult rayTraceResult = player.rayTraceBlocks(7);
                    BlockFace faceToSpawnParticles = this.blockFace;
                    if (rayTraceResult != null && block.equals(rayTraceResult.getHitBlock()) && rayTraceResult.getHitBlockFace() != null)
                        faceToSpawnParticles = rayTraceResult.getHitBlockFace();
                    Vector normalOfBlockFace = faceToSpawnParticles.getDirection();

                    if (fakeBlockState.getFakeBlockDisplay().simulateDiggingParticles())
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

            if (progress > 0) {
                BlockBreakProgressUpdateEvent blockBreakProgressUpdateEvent = new BlockBreakProgressUpdateEvent(block, progress, player);
                blockBreakProgressUpdateEvent.callEvent();
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                if (onlinePlayer.getEntityId() == entityId)
                    continue;
                if (FakeBlockUtil.playerNotInEffectRange(onlinePlayer, block)) continue;

                player.sendBlockDamage(block.getLocation(), progress, entityId);
            }
        }
    }
}

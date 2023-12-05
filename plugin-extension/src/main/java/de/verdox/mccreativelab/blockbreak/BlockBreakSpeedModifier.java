package de.verdox.mccreativelab.blockbreak;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBreakSpeedModifier implements Listener {
    private static final Map<Player, BlockBreakProgress> map = new HashMap<>();

    @EventHandler
    public void onStartDigging(BlockDamageEvent e) {
        Player player = e.getPlayer();
        if (map.containsKey(player))
            stopBlockBreakAction(player);

        Material bukkitMaterial = e.getBlock().getType();
        if (!BlockBreakSpeedSettings.hasCustomBlockHardness(bukkitMaterial))
            return;
        map.put(player, new BlockBreakProgress(player, e.getBlock(), BlockBreakSpeedSettings.getCustomBlockHardness(e
                .getBlock().getType())));
    }

    @EventHandler
    public void onStopDigging(BlockDamageAbortEvent e) {
        stopBlockBreakAction(e.getPlayer());
    }

    @EventHandler
    public void stopDiggingOnQuit(PlayerQuitEvent e){
        stopBlockBreakAction(e.getPlayer());
    }

    @EventHandler
    public void tickPlayers(ServerTickEndEvent e){
        Bukkit.getOnlinePlayers().forEach(BlockBreakSpeedModifier::tick);
    }

    public static void stopBlockBreakAction(Player player) {
        if (!map.containsKey(player))
            return;
        map.remove(player).resetBlockDamage();
    }

    public static void tick(Player player) {
        if (!map.containsKey(player))
            return;
        var data = map.get(player);
        data.incrementTicks();
    }

    public static class BlockBreakProgress {
        private final Player player;
        private final Block block;
        private final float hardness;
        private float damageTaken;
        private int lastStage = -1;
        private final int[] idsPerStage = new int[10];

        public BlockBreakProgress(Player player, Block block, float hardness) {
            this.player = player;
            this.block = block;
            this.hardness = hardness;
        }

        public void incrementTicks() {
            var totalTimeInTicks = calculateBreakTime();
            damageTaken += (1f / totalTimeInTicks);
            damageTaken = Math.min(1, damageTaken);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 3, -1, false, false, false));

            int stage = (int) (damageTaken * 10) - 1;

            if (stage != lastStage) {
                if (stage < 0 || stage > 9) {
                    System.out.println("ERROR stage is " + stage + " because damageTaken is " + damageTaken + " because totalTimeInTicks is " + totalTimeInTicks);
                    return;
                }

                for (int i = lastStage + 1; i <= stage; i++) {
                    var entityID = getDestructionID(i);
                    sendBlockDamage(i, entityID);
                }
                lastStage = stage;
            }

            if (stage == 9) {
                block.breakNaturally(player.getActiveItem(), true, true);
                stopBlockBreakAction(player);
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
            player.sendBlockDamage(block.getLocation(), stage, entityId);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                if (onlinePlayer.getEntityId() == entityId)
                    continue;
                if (!onlinePlayer.getWorld().equals(block.getWorld()))
                    continue;
                double xDistance = (double) block.getX() - onlinePlayer.getX();
                double yDistance = (double) block.getY() - onlinePlayer.getY();
                double zDistance = (double) block.getZ() - onlinePlayer.getZ();

                if (xDistance * xDistance + yDistance * yDistance + zDistance * zDistance >= 1024.0D)
                    continue;
                player.sendBlockDamage(block.getLocation(), stage, entityId);
            }
        }

        // This formula is taken from
        // https://minecraft.fandom.com/wiki/Breaking
        public int calculateBreakTime() {
            double multiplier = 1.0D;
            ItemStack hand = player.getInventory().getItem(EquipmentSlot.HAND);
            BlockState blockState = block.getState();
            BlockData blockData = blockState.getBlockData();

            float breakTime = hardness;
            boolean isPreferredTool = block.isPreferredTool(hand);

            if (isPreferredTool) {
                multiplier = block.getDestroySpeed(hand, false);

                // canHarvest
                if (blockData.requiresCorrectToolForDrops()) {
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

            double blockDamage = multiplier / breakTime;

            if (isPreferredTool)
                blockDamage /= 30.0;
            else
                blockDamage /= 100.0;
            breakTime = blockDamage >= 1 ? 0 : (int) Math.ceil(1.0D / blockDamage);
            return (int) breakTime;
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

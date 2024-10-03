package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.registry.Reference;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.item.FakeItem;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class BlockUtil {

    public static float getBlockDestroyProgress(Player player, float hardness, BlockState blockState, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
        ItemStack hand = player.getInventory().getItemInMainHand();

        boolean hasCorrectToolForDrops;
        if (fakeBlockState != null)
            hasCorrectToolForDrops = fakeBlockState.getFakeBlock().isPreferredTool(fakeBlockState, blockState.getBlock(), player, hand);
        else
            hasCorrectToolForDrops = blockState.getBlockData().isPreferredTool(player.getInventory().getItemInMainHand());
        return getDestroySpeed(player, blockState, fakeBlockState) / hardness / (hasCorrectToolForDrops ? 30f : 100f);
    }

    private static float getDestroySpeed(Player player, BlockState blockState, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        float destroySpeed = 1;
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(hand);
        if(fakeItemReference != null)
            destroySpeed = fakeItemReference.unwrapValue().getDestroySpeed(hand, blockState.getBlock(), fakeBlockState);
        if(fakeBlockState != null && destroySpeed == 1)
            destroySpeed = fakeBlockState.getFakeBlock().getDestroySpeed(fakeBlockState, player, hand);

        if (fakeBlockState != null) {

            //destroySpeed = fakeBlockState.getFakeBlock().getDestroySpeed(fakeBlockState, blockState.getBlock(), hand);

            if (destroySpeed > 1) {
                int enchantLevel = getEnchantmentLevel(player, Enchantment.EFFICIENCY);
                if (enchantLevel > 0)
                    destroySpeed += enchantLevel * (enchantLevel + 1);
            }
        } else
            destroySpeed = blockState.getBlock().getDestroySpeed(hand, true);

        // Haste effect
        if (player.hasPotionEffect(PotionEffectType.HASTE))
            destroySpeed *= 0.2D * player.getPotionEffect(PotionEffectType.HASTE).getAmplifier() + 1.0D;
        // water check
        if (player.isInWater() && hasEnchantmentLevel(player, Enchantment.AQUA_AFFINITY))
            destroySpeed /= 5.0D;
        // in air check
        if (!player.isOnGround())
            destroySpeed /= 5.0D;

        return destroySpeed;
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

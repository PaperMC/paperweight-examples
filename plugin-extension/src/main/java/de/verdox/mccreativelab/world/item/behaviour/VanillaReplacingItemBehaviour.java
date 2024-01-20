package de.verdox.mccreativelab.world.item.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface VanillaReplacingItemBehaviour extends ItemBehaviour {
    @Override
    default BehaviourResult.Object<ItemStack> finishUsingItem(LivingEntity livingEntity, ItemStack usedItem) {
        return new BehaviourResult.Object<>(usedItem, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Object<Integer> getMaxStackSize(ItemStack stack) {
        return new BehaviourResult.Object<>(stack.getType().getMaxStackSize(), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Object<Integer> getMaxDamage(ItemStack stack) {
        return new BehaviourResult.Object<>(0, BehaviourResult.Object.Type.USE_VANILLA);
    }

    @Override
    default BehaviourResult.Bool mineBlock(ItemStack stack, Block block, Player miner) {
        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Bool isCorrectToolForDrops(ItemStack stack, BlockData blockData) {
        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Void onCraftedBy(ItemStack stack, Player player, int amount) {
        return new BehaviourResult.Void(BehaviourResult.Void.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Void onDestroyed(ItemStack stack, Item item) {
        return new BehaviourResult.Void(BehaviourResult.Void.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Bool isEdible(ItemStack stack) {
        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Object<InteractionResult> interactLivingEntity(ItemStack stack, Player player, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        return new BehaviourResult.Object<>(InteractionResult.PASS, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }
}

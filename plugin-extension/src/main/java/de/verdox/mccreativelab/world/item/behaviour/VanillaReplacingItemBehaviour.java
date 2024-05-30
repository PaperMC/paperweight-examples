package de.verdox.mccreativelab.world.item.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.behaviour.interaction.ItemStackInteraction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

public interface VanillaReplacingItemBehaviour extends ItemBehaviour {
    @Override
    default BehaviourResult.Object<ItemStack> finishUsingItem(LivingEntity livingEntity, ItemStack usedItem) {
        return new BehaviourResult.Object<>(usedItem, BehaviourResult.Object.Type.REPLACE_VANILLA);
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
    default BehaviourResult.Object<ItemStackInteraction> use(ItemStack stack, Player player, EquipmentSlot equipmentSlot) {
        return new BehaviourResult.Object<>(new ItemStackInteraction(InteractionResult.PASS, new ItemStack(Material.AIR)), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Object<InteractionResult> useOn(ItemStack stack, Player player, EquipmentSlot equipmentSlot, RayTraceResult rayTraceResult) {
        return new BehaviourResult.Object<>(InteractionResult.PASS, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Object<ItemStack> getCraftRemainingItem(ItemStack stack) {
        return new BehaviourResult.Object<>(new ItemStack(Material.AIR), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Bool canFitInsideContainerItems(ItemStack stack) {
        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Bool isEnchantable(ItemStack stack) {
        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Object<InteractionResult> interactLivingEntity(ItemStack stack, Player player, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        return new BehaviourResult.Object<>(InteractionResult.PASS, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }
}

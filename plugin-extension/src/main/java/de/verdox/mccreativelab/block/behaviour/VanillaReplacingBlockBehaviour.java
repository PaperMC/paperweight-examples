package de.verdox.mccreativelab.block.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;

public interface VanillaReplacingBlockBehaviour extends BlockBehaviour {

    BehaviourResult.Void VOID_DEFAULT = new BehaviourResult.Void(BehaviourResult.Void.Type.REPLACE_VANILLA);

    @Override
    default BehaviourResult.Object<Float> getExplosionResistance(Block block, BlockData blockData) {
        return BlockBehaviour.super.getExplosionResistance(block, blockData);
    }

    @Override
    default BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        return new BehaviourResult.Object<>(blockData, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Void onNeighbourBlockUpdate(Block block, Block sourceBlock, boolean notify) {
        return VOID_DEFAULT;
    }

    @Override
    default BehaviourResult.Void stepOn(Block block, BlockData blockData, Entity entity) {
        return VOID_DEFAULT;
    }

    @Override
    default BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        return VOID_DEFAULT;
    }

    @Override
    default BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return BlockBehaviour.super.isBlockDataRandomlyTicking(blockData);
    }

    @Override
    default BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        return BlockBehaviour.super.isBlockRandomlyTicking(block, blockData);
    }

    @Override
    default BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify) {
        return BlockBehaviour.super.onPlace(location, newBlockData, oldBlockData, notify);
    }

    @Override
    default BehaviourResult.Callback onPlayerPlace(Player player, Location location, BlockData thePlacedState) {
        return BlockBehaviour.super.onPlayerPlace(player, location, thePlacedState);
    }

    @Override
    default BehaviourResult.Callback onPlayerBreak(Player player, Location location, BlockData brokenState) {
        return BlockBehaviour.super.onPlayerBreak(player, location, brokenState);
    }

    @Override
    default BehaviourResult.Callback onRemove(Location location, BlockData newBlockData, BlockData oldBlockData, boolean moved) {
        return BlockBehaviour.super.onRemove(location, newBlockData, oldBlockData, moved);
    }

    @Override
    default BehaviourResult.Callback onUse(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        return BlockBehaviour.super.onUse(block, player, hand, rayTraceResult);
    }

    @Override
    default BehaviourResult.Object<InteractionResult> use(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        return new BehaviourResult.Object<>(InteractionResult.SUCCESS, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    default BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        return VOID_DEFAULT;
    }

    @Override
    default BehaviourResult.Bool canSurvive(Block block, World world) {
        return BlockBehaviour.super.canSurvive(block, world);
    }

    @Override
    default BehaviourResult.Void attack(Block block, Player player) {
        return VOID_DEFAULT;
    }
}

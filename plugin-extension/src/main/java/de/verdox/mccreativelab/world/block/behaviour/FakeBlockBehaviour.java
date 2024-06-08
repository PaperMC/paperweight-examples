package de.verdox.mccreativelab.world.block.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.ItemInteractionResult;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.world.block.FakeBlockRegistry;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.world.block.entity.FakeBlockEntity;
import de.verdox.mccreativelab.world.block.entity.FakeBlockEntityStorage;
import de.verdox.mccreativelab.world.block.util.FakeBlockUtil;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class FakeBlockBehaviour implements BlockBehaviour {

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, ItemStack stackUsedToPlaceBlock, Location location, BlockData thePlacedState) {
        setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), thePlacedState);

        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if(fakeBlockState != null && fakeBlockState.getFakeBlock().hasBlockEntity()) {
            FakeBlockEntity fakeBlockEntity = FakeBlockEntityStorage.createFakeBlockEntity(fakeBlockState, location);
            FakeBlockEntityStorage.getFakeBlockEntityDataFromItemStack(fakeBlockEntity, stackUsedToPlaceBlock);
        }

        return BlockBehaviour.super.onPlayerPlace(player, stackUsedToPlaceBlock, location, thePlacedState);
    }

    @Override
    public BehaviourResult.Callback onPlayerBreak(Player player, Location location, BlockData brokenState) {

        FakeBlockUtil.removeFakeBlockIfPossible(location.getBlock());
        FakeBlockEntityStorage.removeFakeBlockEntityAt(location);

        return BlockBehaviour.super.onPlayerBreak(player, location, brokenState);
    }

    @Override
    public BehaviourResult.Callback onPistonMoveBlock(BlockData blockDataMoved, Location positionBeforeMove, Location positionAfterMove, Block piston, Vector moveDirection) {

        FakeBlockEntity fakeBlockEntity = FakeBlockEntityStorage.getFakeBlockEntityAt(positionBeforeMove.getBlock());
        if(fakeBlockEntity == null)
            return done();
        fakeBlockEntity.changePosition(positionAfterMove);
        return done();
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify, boolean isProcessingBlockPlaceEvent) {
        if (!isProcessingBlockPlaceEvent)
            setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), newBlockData);

        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (!notify && !newBlockData.equals(oldBlockData) && fakeBlockState != null  && fakeBlockState.getFakeBlock().hasBlockEntity())
            FakeBlockEntityStorage.createFakeBlockEntity(fakeBlockState, location);

        return BlockBehaviour.super.onPlace(location, newBlockData, oldBlockData, notify, isProcessingBlockPlaceEvent);
    }

    @Override
    public BehaviourResult.Callback onRemove(Location location, BlockData newBlockData, BlockData oldBlockData, boolean moved) {
        if (!moved && !newBlockData.equals(oldBlockData)) FakeBlockEntityStorage.removeFakeBlockEntityAt(location);

        if (!newBlockData.getMaterial().isAir())
            return done();
        return BlockBehaviour.super.onRemove(location, newBlockData, oldBlockData, moved);
    }

    @Override
    public BehaviourResult.Callback onDestroy(Location location, boolean drop, @Nullable Entity destroyingEntity, int maxUpdateDepth) {
        FakeBlockEntityStorage.removeFakeBlockEntityAt(location);
        return BlockBehaviour.super.onDestroy(location, drop, destroyingEntity, maxUpdateDepth);
    }

    @Override
    public BehaviourResult.Object<Float> getExplosionResistance(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState == null)
            return BlockBehaviour.super.getExplosionResistance(block, blockData);

        return new BehaviourResult.Object<>(fakeBlockState.getProperties().getExplosionResistance(), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool fertilizeAction(Block block, ItemStack stack) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState == null)
            return BlockBehaviour.super.fertilizeAction(block, stack);

        return fakeBlockState.getFakeBlock().fertilizeAction(block, stack);
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return new BehaviourResult.Bool(true, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, blockData);

        if (fakeBlockState != null)
            return new BehaviourResult.Bool(fakeBlockState.getProperties().isRandomlyTicking(), BehaviourResult.Bool.Type.REPLACE_VANILLA);

        return BlockBehaviour.super.isBlockRandomlyTicking(block, blockData);
    }

    @Override
    public BehaviourResult.Bool canSurvive(Block block, World world) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, block.getBlockData());

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().canSurvive(block, world);

        return BlockBehaviour.super.canSurvive(block, world);
    }

    @Override
    public BehaviourResult.Void stepOn(Block block, BlockData blockData, Entity entity) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, blockData);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().stepOn(block, blockData, entity);

        return BlockBehaviour.super.stepOn(block, blockData, entity);
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, block.getBlockData());

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().randomTick(block, vanillaRandomSource);

        return BlockBehaviour.super.randomTick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, block.getBlockData());

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().tick(block, vanillaRandomSource);

        return BlockBehaviour.super.tick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), blockData);

        if (fakeBlockState != null) {
            BehaviourResult.Object<BlockData> result = fakeBlockState.getFakeBlock().blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
            fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy()
                .blockUpdate(location.getBlock(), fakeBlockState, direction, neighbourBlockData);
            return result;
        }
        return BlockBehaviour.super.blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
    }

    @Override
    public BehaviourResult.Void onNeighbourBlockUpdate(Block block, Block sourceBlock, boolean notify) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, block.getBlockData());

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().onNeighbourBlockUpdate(block, sourceBlock, notify);

        return BlockBehaviour.super.onNeighbourBlockUpdate(block, sourceBlock, notify);
    }

    @Override
    public BehaviourResult.Void attack(Block block, Player player) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        if (fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReusesVanillaBlockState(block, block.getBlockData());

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().attack(block, player);

        return BlockBehaviour.super.attack(block, player);
    }

    @Override
    public BehaviourResult.Object<ItemInteractionResult> use(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().use(block, player, hand, rayTraceResult);
        return BlockBehaviour.super.use(block, player, hand, rayTraceResult);
    }

    @Override
    public BehaviourResult.Callback onUseCallback(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult, InteractionResult interactionResult) {
        setFakeBlockStateIfReusesVanillaBlockState(block, block.getBlockData());
        return BlockBehaviour.super.onUseCallback(block, player, hand, rayTraceResult, interactionResult);
    }

    @Nullable
    public static FakeBlock.FakeBlockState setFakeBlockStateIfReusesVanillaBlockState(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockRegistry.getFakeBlockStateFromBlockData(blockData);
        if (fakeBlockState == null)
            return null;
        FakeBlockStorage.setFakeBlockState(block.getLocation(), fakeBlockState, false, false);
        return fakeBlockState;
    }
}

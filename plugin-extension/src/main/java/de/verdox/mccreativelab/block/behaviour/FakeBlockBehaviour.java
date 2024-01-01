package de.verdox.mccreativelab.block.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.block.CustomBlockRegistry;
import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.block.FakeBlockStorage;
import de.verdox.mccreativelab.block.FakeBlockUtil;
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
import org.jetbrains.annotations.Nullable;

public class FakeBlockBehaviour implements BlockBehaviour {
    @Override
    public BehaviourResult.Object<Float> getExplosionResistance(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
        if (fakeBlockState == null)
            return BlockBehaviour.super.getExplosionResistance(block, blockData);

        return new BehaviourResult.Object<>(fakeBlockState.getProperties().getExplosionResistance(), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return new BehaviourResult.Bool(true, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return new BehaviourResult.Bool(fakeBlockState.getProperties().isRandomlyTicking(), BehaviourResult.Bool.Type.REPLACE_VANILLA);

        return BlockBehaviour.super.isBlockRandomlyTicking(block, blockData);
    }

    @Override
    public BehaviourResult.Bool canSurvive(Block block, World world) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().canSurvive(block, world);

        return BlockBehaviour.super.canSurvive(block, world);
    }

    @Override
    public BehaviourResult.Void stepOn(Block block, BlockData blockData, Entity entity) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().stepOn(block, blockData, entity);

        return BlockBehaviour.super.stepOn(block, blockData, entity);
    }

    @Override
    public BehaviourResult.Callback onRemove(Location location, BlockData newBlockData, BlockData oldBlockData, boolean moved) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);

        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlock().remove(location, !moved);
            FakeBlockUtil.removeFakeBlockIfPossible(location.getBlock());
        }

        return BlockBehaviour.super.onRemove(location, newBlockData, oldBlockData, moved);
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().randomTick(block, vanillaRandomSource);

        return BlockBehaviour.super.randomTick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().tick(block, vanillaRandomSource);

        return BlockBehaviour.super.tick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(location.getBlock());

        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy()
                          .blockUpdate(location.getBlock(), fakeBlockState, direction, neighbourBlockData);

            return fakeBlockState.getFakeBlock().blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
        }
        return BlockBehaviour.super.blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
    }

    @Override
    public BehaviourResult.Void onNeighbourBlockUpdate(Block block, Block sourceBlock, boolean notify) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().onNeighbourBlockUpdate(block, sourceBlock, notify);

        return BlockBehaviour.super.onNeighbourBlockUpdate(block, sourceBlock, notify);
    }

    @Override
    public BehaviourResult.Void attack(Block block, Player player) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if(fakeBlockState == null)
            fakeBlockState = setFakeBlockStateIfReplacesVanillaBlockState(block);

        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().attack(block, player);

        return BlockBehaviour.super.attack(block, player);
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify) {
        setFakeBlockStateIfReplacesVanillaBlockState(location.getBlock());
        return BlockBehaviour.super.onPlace(location, newBlockData, oldBlockData, notify);
    }

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, Location location, BlockData thePlacedState) {
        setFakeBlockStateIfReplacesVanillaBlockState(location.getBlock());
        return BlockBehaviour.super.onPlayerPlace(player, location, thePlacedState);
    }

    @Override
    public BehaviourResult.Callback onPlayerBreak(Player player, Location location, BlockData brokenState) {

        FakeBlockUtil.removeFakeBlockIfPossible(location.getBlock());

        return BlockBehaviour.super.onPlayerBreak(player, location, brokenState);
    }

    @Override
    public BehaviourResult.Object<InteractionResult> use(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        System.out.println("use");
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
        if (fakeBlockState != null)
            return fakeBlockState.getFakeBlock().use(block, player, hand, rayTraceResult);
        System.out.println("Using vanilla");
        return BlockBehaviour.super.use(block, player, hand, rayTraceResult);
    }

    @Override
    public BehaviourResult.Callback onUse(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        setFakeBlockStateIfReplacesVanillaBlockState(block);
        return BlockBehaviour.super.onUse(block, player, hand, rayTraceResult);
    }

    @Nullable
    private FakeBlock.FakeBlockState setFakeBlockStateIfReplacesVanillaBlockState(Block block){
        FakeBlock.FakeBlockState fakeBlockState = CustomBlockRegistry.getFakeBlockStateFromBlockData(block.getBlockData());
        if(fakeBlockState == null)
            return null;
        FakeBlockStorage.setFakeBlockState(block.getLocation(), fakeBlockState, false);
        return fakeBlockState;
    }
}

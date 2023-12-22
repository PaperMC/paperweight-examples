package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;

import java.util.function.Supplier;

public class ReplacingFakeBlockBehaviour implements BlockBehaviour {
    private final Material replacedVanillaBlock;
    private final NamespacedKey fakeBlockKey;
    private Supplier<BlockBehaviour> replacedVanillaBehaviour;

    public ReplacingFakeBlockBehaviour(Material replacedVanillaBlock, NamespacedKey fakeBlockKey, Supplier<BlockBehaviour> replacedVanillaBehaviour) {
        this.replacedVanillaBlock = replacedVanillaBlock;
        this.fakeBlockKey = fakeBlockKey;
        this.replacedVanillaBehaviour = replacedVanillaBehaviour;
    }

    private BlockBehaviour getReplacedVanillaBehaviour() {
        return replacedVanillaBehaviour.get();
    }

    @Override
    public BehaviourResult.Object<Float> getExplosionResistance(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().getExplosionResistance(block, blockData);

        if (fakeBlockState == null)
            return BlockBehaviour.super.getExplosionResistance(block, blockData);

        return new BehaviourResult.Object<>(fakeBlockState.getProperties()
                                                          .getExplosionResistance(), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return new BehaviourResult.Bool(true, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().isBlockRandomlyTicking(block, blockData);

        if (fakeBlockState != null) {

            if (fakeBlockState.getFakeBlock().getKey().equals(fakeBlockKey))

                return new BehaviourResult.Bool(fakeBlockState.getProperties()
                                                              .isRandomlyTicking(), BehaviourResult.Bool.Type.REPLACE_VANILLA);
        }
        return BlockBehaviour.super.isBlockRandomlyTicking(block, blockData);
    }

    @Override
    public BehaviourResult.Callback onPlayerBreak(Player player, Location location, BlockData brokenState) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);

        CustomBlockRegistry.fakeBlockDamage.sendBlockDamage(location.getBlock(), -1);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().onPlayerBreak(player, location, brokenState);

        if (fakeBlockState == null)
            replaceVanillaWithFakeBlock(location.getBlock());


        return BlockBehaviour.super.onPlayerBreak(player, location, brokenState);
    }

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, Location location, BlockData thePlacedState) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);

        CustomBlockRegistry.fakeBlockDamage.sendBlockDamage(location.getBlock(), -1);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().onPlayerPlace(player, location, thePlacedState);

        if (fakeBlockState == null)
            replaceVanillaWithFakeBlock(location.getBlock());

        return BlockBehaviour.super.onPlayerPlace(player, location, thePlacedState);
    }

    @Override
    public BehaviourResult.Bool canSurvive(Block block, World world) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().canSurvive(block, world);

        if (fakeBlockState != null) {
            return new BehaviourResult.Bool(fakeBlockState.getFakeBlock()
                                                          .canSurvive(fakeBlockState, block), BehaviourResult.Bool.Type.REPLACE_VANILLA);
        }
        return BlockBehaviour.super.canSurvive(block, world);
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);

        CustomBlockRegistry.fakeBlockDamage.sendBlockDamage(location.getBlock(), -1);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().onPlace(location, newBlockData, oldBlockData, notify);

        if (fakeBlockState == null)
            replaceVanillaWithFakeBlock(location.getBlock());
        return BlockBehaviour.super.onPlace(location, newBlockData, oldBlockData, notify);
    }

    @Override
    public BehaviourResult.Void stepOn(Block block, BlockData blockData, Entity entity) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().stepOn(block, blockData, entity);

        if (fakeBlockState != null)
            fakeBlockState.getFakeBlock().stepOn(fakeBlockState, block, entity);
        else replaceVanillaWithFakeBlock(block);


        return BlockBehaviour.super.stepOn(block, blockData, entity);
    }

    @Override
    public BehaviourResult.Callback onRemove(Location location, BlockData newBlockData, BlockData oldBlockData, boolean moved) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);

        CustomBlockRegistry.fakeBlockDamage.sendBlockDamage(location.getBlock(), -1);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().onRemove(location, newBlockData, oldBlockData, moved);

        if (fakeBlockState != null)
            fakeBlockState.getFakeBlock().remove(location, !moved);

        return BlockBehaviour.super.onRemove(location, newBlockData, oldBlockData, moved);
    }

    @Override
    public BehaviourResult.Callback onUse(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().onUse(block, player, hand, rayTraceResult);

        if (fakeBlockState == null)
            replaceVanillaWithFakeBlock(block);
        return BlockBehaviour.super.onUse(block, player, hand, rayTraceResult);
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().randomTick(block, vanillaRandomSource);

        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlock().randomTick(fakeBlockState, block, vanillaRandomSource);
            return new BehaviourResult.Void(BehaviourResult.Void.Type.REPLACE_VANILLA_LOGIC);
        } else
            replaceVanillaWithFakeBlock(block);
        return BlockBehaviour.super.randomTick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);

        if (isReplacedVanillaBlock(fakeBlockState))
            return getReplacedVanillaBehaviour().tick(block, vanillaRandomSource);

        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlock().tick(fakeBlockState, block, vanillaRandomSource);
            return new BehaviourResult.Void(BehaviourResult.Void.Type.REPLACE_VANILLA_LOGIC);
        } else
            replaceVanillaWithFakeBlock(block);
        return BlockBehaviour.super.tick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);
        if (fakeBlockState != null) {
            fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy().blockUpdate(location.getBlock(), fakeBlockState, direction, neighbourBlockData);
            if (isReplacedVanillaBlock(fakeBlockState))
                return getReplacedVanillaBehaviour().blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);

            return new BehaviourResult.Object<>(fakeBlockState.getFakeBlock()
                                                              .blockUpdate(fakeBlockState, location.getBlock(), direction, neighbourBlockData, neighbourLocation), BehaviourResult.Object.Type.REPLACE_VANILLA);
        } else
            replaceVanillaWithFakeBlock(location.getBlock());
        return BlockBehaviour.super.blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
    }


    private void replaceVanillaWithFakeBlock(Block block) {
        FakeBlock fakeBlock = MCCreativeLabExtension.getCustomBlockRegistry().get(fakeBlockKey);
        if (fakeBlock == null)
            return;
        FakeBlockStorage.setFakeBlock(block.getLocation(), fakeBlock, false);
    }

    private boolean isReplacedVanillaBlock(FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState == null)
            return false;
        return fakeBlockState.getFakeBlock().getKey().equals(fakeBlockKey);
    }

}

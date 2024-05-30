package de.verdox.mccreativelab.world.block.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.ItemInteractionResult;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * This behaviour is used for reused block states like note blocks.
 */
public class ReusedStateBehaviour extends FakeBlockBehaviour {
    private final Material replacedVanillaBlock;
    private final NamespacedKey fakeBlockKey;
    private Supplier<BlockBehaviour> replacedVanillaBehaviour;

    public ReusedStateBehaviour(Material replacedVanillaBlock, @Nullable NamespacedKey fakeBlockKey, @Nullable Supplier<BlockBehaviour> replacedVanillaBehaviour) {
        this.replacedVanillaBlock = replacedVanillaBlock;
        this.fakeBlockKey = fakeBlockKey;
        this.replacedVanillaBehaviour = replacedVanillaBehaviour != null ? replacedVanillaBehaviour : () -> new BlockBehaviour() {
        };
    }

    public ReusedStateBehaviour(Material replacedVanillaBlock, @Nullable NamespacedKey fakeBlockKey, @Nullable BlockBehaviour replacedVanillaBehaviour) {
        this(replacedVanillaBlock, fakeBlockKey, replacedVanillaBehaviour == null ? null : () -> replacedVanillaBehaviour);
    }

    public ReusedStateBehaviour(Material replacedVanillaBlock) {
        this(replacedVanillaBlock, null, (BlockBehaviour) null);
    }

    private BlockBehaviour getReplacedVanillaBehaviour() {
        return replacedVanillaBehaviour.get();
    }

    @Override
    public BehaviourResult.Object<Float> getExplosionResistance(Block block, BlockData blockData) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().getExplosionResistance(block, blockData);
        return super.getExplosionResistance(block, blockData);
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().isBlockRandomlyTicking(block, blockData);
        return super.isBlockRandomlyTicking(block, blockData);
    }

    @Override
    public BehaviourResult.Callback onPlayerBreak(Player player, Location location, BlockData brokenState) {
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onPlayerBreak(player, location, brokenState);
        return super.onPlayerBreak(player, location, brokenState);
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify, boolean isProcessingBlockPlaceEvent) {
        //System.out.println("onPlaceByServer: " + oldBlockData.getAsString() + " -> " + newBlockData + " (notify = " + notify + ")");
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onPlace(location, newBlockData, oldBlockData, notify, isProcessingBlockPlaceEvent);
        return super.onPlace(location, newBlockData, oldBlockData, notify, isProcessingBlockPlaceEvent);
    }

    @Override
    public BehaviourResult.Callback onRemove(Location location, BlockData newBlockData, BlockData oldBlockData, boolean moved) {
        //System.out.println("onRemove: " + oldBlockData.getAsString() + " ---> " + newBlockData.getAsString() + " (moved = " + moved + ")");
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onRemove(location, newBlockData, oldBlockData, moved);
        return super.onRemove(location, newBlockData, oldBlockData, moved);
    }

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, ItemStack stackUsedToPlaceBlock, Location location, BlockData thePlacedState) {
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onPlayerPlace(player, stackUsedToPlaceBlock, location, thePlacedState);
        replaceVanillaWithFakeBlock(location.getBlock());
        return super.onPlayerPlace(player, stackUsedToPlaceBlock, location, thePlacedState);
    }

    @Override
    public BehaviourResult.Bool canSurvive(Block block, World world) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().canSurvive(block, world);
        return super.canSurvive(block, world);
    }

    @Override
    public BehaviourResult.Void stepOn(Block block, BlockData blockData, Entity entity) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().stepOn(block, blockData, entity);
        return super.stepOn(block, blockData, entity);
    }

    @Override
    public BehaviourResult.Callback onUseCallback(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult, InteractionResult interactionResult) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().onUseCallback(block, player, hand, rayTraceResult, interactionResult);
        replaceVanillaWithFakeBlock(block);
        return super.onUseCallback(block, player, hand, rayTraceResult, interactionResult);
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().randomTick(block, vanillaRandomSource);
        replaceVanillaWithFakeBlock(block);
        return super.randomTick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().tick(block, vanillaRandomSource);
        replaceVanillaWithFakeBlock(block);
        return super.tick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Object<ItemInteractionResult> use(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().use(block, player, hand, rayTraceResult);
        replaceVanillaWithFakeBlock(block);
        return super.use(block, player, hand, rayTraceResult);
    }

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
        replaceVanillaWithFakeBlock(location.getBlock());
        return super.blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
    }

    @Override
    public BehaviourResult.Void onNeighbourBlockUpdate(Block block, Block sourceBlock, boolean notify) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().onNeighbourBlockUpdate(block, sourceBlock, notify);
        replaceVanillaWithFakeBlock(block);
        return super.onNeighbourBlockUpdate(block, sourceBlock, notify);
    }

    @Override
    public BehaviourResult.Void attack(Block block, Player player) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().attack(block, player);
        replaceVanillaWithFakeBlock(block);
        return super.attack(block, player);
    }

    private void replaceVanillaWithFakeBlock(Block block) {
        if (fakeBlockKey == null)
            return;
        FakeBlock fakeBlock = MCCreativeLabExtension.getFakeBlockRegistry().get(fakeBlockKey);
        if (fakeBlock == null)
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState != null)
            return;
        FakeBlockStorage.setFakeBlock(block.getLocation(), fakeBlock, false, false);
    }

    private boolean isReplacedVanillaBlock(Block block) {
        if (fakeBlockKey == null)
            return false;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState == null)
            return false;
        return fakeBlockState.getFakeBlock().getKey().equals(fakeBlockKey);
    }

}

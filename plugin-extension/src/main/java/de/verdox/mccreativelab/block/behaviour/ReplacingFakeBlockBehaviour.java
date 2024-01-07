package de.verdox.mccreativelab.block.behaviour;

import de.verdox.mccreativelab.InteractionResult;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.block.FakeBlockStorage;
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
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ReplacingFakeBlockBehaviour extends FakeBlockBehaviour {
    private final Material replacedVanillaBlock;
    private final NamespacedKey fakeBlockKey;
    private Supplier<BlockBehaviour> replacedVanillaBehaviour;

    public ReplacingFakeBlockBehaviour(Material replacedVanillaBlock, @Nullable NamespacedKey fakeBlockKey, @Nullable Supplier<BlockBehaviour> replacedVanillaBehaviour) {
        this.replacedVanillaBlock = replacedVanillaBlock;
        this.fakeBlockKey = fakeBlockKey;
        this.replacedVanillaBehaviour = replacedVanillaBehaviour;
    }

    public ReplacingFakeBlockBehaviour(Material replacedVanillaBlock) {
        this(replacedVanillaBlock, null, null);
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
    public BehaviourResult.Callback onPlayerPlace(Player player, Location location, BlockData thePlacedState) {
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onPlayerPlace(player, location, thePlacedState);
        replaceVanillaWithFakeBlock(location.getBlock());
        return super.onPlayerPlace(player, location, thePlacedState);
    }

    @Override
    public BehaviourResult.Bool canSurvive(Block block, World world) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().canSurvive(block, world);
        return super.canSurvive(block, world);
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify) {
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onPlace(location, newBlockData, oldBlockData, notify);
        return super.onPlace(location, newBlockData, oldBlockData, notify);
    }

    @Override
    public BehaviourResult.Void stepOn(Block block, BlockData blockData, Entity entity) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().stepOn(block, blockData, entity);
        return super.stepOn(block, blockData, entity);
    }

    @Override
    public BehaviourResult.Callback onRemove(Location location, BlockData newBlockData, BlockData oldBlockData, boolean moved) {
        if (isReplacedVanillaBlock(location.getBlock()))
            return getReplacedVanillaBehaviour().onRemove(location, newBlockData, oldBlockData, moved);
        return super.onRemove(location, newBlockData, oldBlockData, moved);
    }

    @Override
    public BehaviourResult.Callback onUse(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
        if (isReplacedVanillaBlock(block))
            return getReplacedVanillaBehaviour().onUse(block, player, hand, rayTraceResult);
        replaceVanillaWithFakeBlock(block);
        return super.onUse(block, player, hand, rayTraceResult);
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
    public BehaviourResult.Object<InteractionResult> use(Block block, Player player, EquipmentSlot hand, RayTraceResult rayTraceResult) {
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
        if(fakeBlockKey == null)
            return;
        FakeBlock fakeBlock = MCCreativeLabExtension.getFakeBlockRegistry().get(fakeBlockKey);
        if (fakeBlock == null)
            return;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
        if(fakeBlockState != null)
            return;
        FakeBlockStorage.setFakeBlock(block.getLocation(), fakeBlock, false);
    }

    private boolean isReplacedVanillaBlock(Block block) {
        if(fakeBlockKey == null)
            return false;
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
        if (fakeBlockState == null)
            return false;
        return fakeBlockState.getFakeBlock().getKey().equals(fakeBlockKey);
    }

}

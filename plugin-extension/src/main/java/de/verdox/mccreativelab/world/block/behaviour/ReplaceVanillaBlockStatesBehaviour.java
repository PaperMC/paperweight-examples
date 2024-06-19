package de.verdox.mccreativelab.world.block.behaviour;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This behaviour is used for custom crops.
 * Methods in this behaviour are called to replace vanilla block states entirely with a fake block state.
 */
public class ReplaceVanillaBlockStatesBehaviour extends FakeBlockBehaviour {
    private static final Map<BlockData, FakeBlock.FakeBlockState> REPLACED_VISUAL_STATES = new HashMap<>();

    public ReplaceVanillaBlockStatesBehaviour(Map<BlockData, FakeBlock.FakeBlockState> replacedVisualStates) {
        REPLACED_VISUAL_STATES.putAll(replacedVisualStates);
    }

/*    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify, boolean isProcessingBlockPlaceEvent) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(newBlockData)) {
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(newBlockData), false, false);
            }
        return super.onPlace(location, newBlockData, oldBlockData, notify, isProcessingBlockPlaceEvent);
    }

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, ItemStack stackUsedToPlaceBlock, Location location, BlockData thePlacedState) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(thePlacedState))
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(thePlacedState), false,false);
        return super.onPlayerPlace(player, stackUsedToPlaceBlock, location, thePlacedState);
    }*/

    @Override
    public BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState == null)
            if (REPLACED_VISUAL_STATES.containsKey(block.getBlockData()))
                FakeBlockStorage.setFakeBlockState(block.getLocation(), REPLACED_VISUAL_STATES.get(block.getBlockData()), false, false);
        return super.tick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (REPLACED_VISUAL_STATES.containsKey(blockData))
                FakeBlockStorage.setFakeBlockState(location, REPLACED_VISUAL_STATES.get(blockData), false, false);

        return super.blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
    }

    public static Map<BlockData, FakeBlock.FakeBlockState> getReplacedVisualStates() {
        return REPLACED_VISUAL_STATES;
    }
}

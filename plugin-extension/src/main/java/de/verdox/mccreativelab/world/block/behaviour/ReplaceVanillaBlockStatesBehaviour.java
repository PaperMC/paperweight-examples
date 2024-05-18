package de.verdox.mccreativelab.world.block.behaviour;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * This behaviour is used for custom crops.
 * Methods in this behaviour are called to replace vanilla block states entirely with a fake block state.
 */
public class ReplaceVanillaBlockStatesBehaviour extends FakeBlockBehaviour {
    private final Map<BlockData, FakeBlock.FakeBlockState> replacedVisualStates;

    public ReplaceVanillaBlockStatesBehaviour(Map<BlockData, FakeBlock.FakeBlockState> replacedVisualStates) {
        this.replacedVisualStates = replacedVisualStates;
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify, boolean isProcessingBlockPlaceEvent) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null && !isProcessingBlockPlaceEvent)
            if (replacedVisualStates.containsKey(newBlockData))
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(newBlockData), false);
        return super.onPlace(location, newBlockData, oldBlockData, notify, isProcessingBlockPlaceEvent);
    }

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, ItemStack stackUsedToPlaceBlock, Location location, BlockData thePlacedState) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(thePlacedState))
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(thePlacedState), false);
        return super.onPlayerPlace(player, stackUsedToPlaceBlock, location, thePlacedState);
    }

    @Override
    public BehaviourResult.Void tick(Block block, VanillaRandomSource vanillaRandomSource) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(block.getBlockData()))
                FakeBlockStorage.setFakeBlockState(block.getLocation(), this.replacedVisualStates.get(block.getBlockData()), false);
        return super.tick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(blockData))
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(blockData), false);

        return super.blockUpdate(location, blockData, direction, neighbourBlockData, neighbourLocation);
    }
}

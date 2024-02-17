package de.verdox.mccreativelab.world.block.behaviour;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Map;

public class ReplaceBlockStatesWithFakeBlocksBehaviour extends FakeBlockBehaviour {
    private final Map<BlockData, FakeBlock.FakeBlockState> replacedVisualStates;

    public ReplaceBlockStatesWithFakeBlocksBehaviour(Map<BlockData, FakeBlock.FakeBlockState> replacedVisualStates) {
        this.replacedVisualStates = replacedVisualStates;
    }

    @Override
    public BehaviourResult.Callback onPlace(Location location, BlockData newBlockData, BlockData oldBlockData, boolean notify) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(newBlockData))
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(newBlockData), false);

        return super.onPlace(location, newBlockData, oldBlockData, notify);
    }

    @Override
    public BehaviourResult.Callback onPlayerPlace(Player player, Location location, BlockData thePlacedState) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(location, false);
        if (fakeBlockState == null)
            if (replacedVisualStates.containsKey(thePlacedState))
                FakeBlockStorage.setFakeBlockState(location, this.replacedVisualStates.get(thePlacedState), false);
        return super.onPlayerPlace(player, location, thePlacedState);
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

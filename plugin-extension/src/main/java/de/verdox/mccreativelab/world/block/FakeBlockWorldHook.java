package de.verdox.mccreativelab.world.block;

import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.behaviour.MCCWorldHook;
import de.verdox.mccreativelab.world.block.behaviour.ReplaceVanillaBlockStatesBehaviour;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FakeBlockWorldHook extends MCCWorldHook {
    @Override
    public void onBlockChange(@NotNull Block block, @NotNull BlockData oldBlockData, @NotNull BlockData newBlockData) {
        BlockContext blockContext = getBlockContext(block);
        //System.out.println(oldBlockData+"  ->  "+newBlockData);

        if (blockContext instanceof SetFakeBlockContext || blockContext instanceof RemoveFakeBlockContext || blockContext instanceof FakeBlockContext)
            return;

        // This block was replaced by a replaced visual state (Custom crops for example)
        FakeBlock.FakeBlockState fakeBlockState = ReplaceVanillaBlockStatesBehaviour.getReplacedVisualStates().get(newBlockData);

        // If the block was replaced by a reuse strategy
        if(fakeBlockState == null)
            fakeBlockState = FakeBlockRegistry.getReusedBlockStates().get(newBlockData);


        // At this stage we know that the vanilla server has changed a block without us explicitly changing it via the FakeBlockStorage
        // This could mean that the vanilla server tries to remove the block because of physics updates or something like that.
        FakeBlockStorage.setFakeBlockState(block.getLocation(), fakeBlockState, false);
    }

    @Override
    public void onBlockDrawLoot(@NotNull Block block, @Nullable Entity entity, ItemStack tool, boolean dropExperience, List<ItemStack> itemsThatWillBeDropped) {

        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);

        super.onBlockDrawLoot(block, entity, tool, dropExperience, itemsThatWillBeDropped);
    }

    public record SetFakeBlockContext(Block block, FakeBlock.FakeBlockState newFakeBlockState) implements BlockContext {
    }

    public record RemoveFakeBlockContext(Block block,
                                         FakeBlock.FakeBlockState oldFakeBlockState) implements BlockContext {
    }

    public record FakeBlockContext(Block block) implements BlockContext {
    }
}

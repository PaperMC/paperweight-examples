package de.verdox.mccreativelab.world.item.template;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.world.item.FakeItem;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FakeBlockItem extends FakeItem {
    private final BlockData blockData;

    public FakeBlockItem(BlockData blockData){
        this.blockData = blockData;
    }

    @Override
    public BehaviourResult.Object<BlockData> placeBlockAction(ItemStack stack, Player player, Location clickedPosition, BlockData vanillaBlockData) {
        return result(blockData);
    }
}

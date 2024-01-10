package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.item.FakeItem;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class DebugItem extends FakeItem {
    protected DebugItem(Material material, int customModelData) {
        super(material, customModelData);
    }

    @Override
    public BehaviourResult.Bool isCorrectToolForDrops(ItemStack stack, BlockData blockData) {
        if(blockData.getMaterial().equals(Material.STONE))
            return new BehaviourResult.Bool(true, BehaviourResult.Bool.Type.REPLACE_VANILLA);
        return super.isCorrectToolForDrops(stack, blockData);
    }
}

package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomOreBlock extends FakeBlock {
    protected CustomOreBlock(List<FakeBlockState> fakeBlockStates) {
        super(fakeBlockStates);
    }

    @Override
    public boolean isPreferredTool(@NotNull FakeBlockState fakeBlockState, @NotNull Block block, @NotNull Player player, ItemStack stack) {
        return stack != null && stack.getType().equals(Material.STICK);
    }

    @Override
    public float getDestroySpeed(@NotNull FakeBlockState fakeBlockState, @NotNull Block block, @Nullable ItemStack itemStack) {
        return itemStack != null ? itemStack.getType().equals(Material.STICK) ? 5 : 0 : 0;
    }
}

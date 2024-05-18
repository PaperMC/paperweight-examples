package de.verdox.mccreativelab.wrapper.block;

import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.wrapper.MCCWrapped;
import de.verdox.mccreativelab.wrapper.entity.MCCEntity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface MCCBlock extends MCCWrapped {

    static MCCBlock getFromBlock(Block block){
        return new Vanilla(block);
    }

    Location getLocation();

    MCCBlockData getBlockData();

    MCCBlockType getBlockType();

    Collection<ItemStack> getDrops(@Nullable ItemStack tool, @Nullable MCCEntity entity);

    class Vanilla extends MCCWrapped.Impl<Block> implements MCCBlock {
        protected Vanilla(Block handle) {
            super(handle);
        }

        @Override
        public Location getLocation() {
            return getHandle().getLocation();
        }

        @Override
        public MCCBlockData getBlockData() {
            return MCCBlockData.getFromBlock(getHandle());
        }

        @Override
        public MCCBlockType getBlockType() {
            return MCCBlockType.getFromBlock(getHandle());
        }

        @Override
        public Collection<ItemStack> getDrops(@Nullable ItemStack tool, @Nullable MCCEntity entity) {
            FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(getLocation(), false);
            Entity bukkitEntity = entity == null ? null : entity.getHandle() instanceof Entity entity1 ? entity1 : null;
            if (fakeBlockState != null)
                return fakeBlockState.getFakeBlock().drawLoot(getHandle(), fakeBlockState, bukkitEntity, tool, tool == null);
            else
                return getHandle().getDrops(tool, bukkitEntity);
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if (mccWrapped instanceof Vanilla vanilla)
                return vanilla.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getBlockType().getKey();
        }
    }

}

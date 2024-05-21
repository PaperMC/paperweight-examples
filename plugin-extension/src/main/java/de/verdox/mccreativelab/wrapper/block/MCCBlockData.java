package de.verdox.mccreativelab.wrapper.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.serialization.NBTSerializer;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.world.block.customhardness.BlockBreakSpeedModifier;
import de.verdox.mccreativelab.wrapper.MCCWrapped;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public interface MCCBlockData extends MCCWrapped {

    //TODO: applyPhysics is being used for update block data in FakeBlockStorage. This is kinda weird.

    static MCCBlockData getFromBlock(Block block){
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(block.getLocation(), false);
        if(fakeBlockState != null)
            return MCCBlockData.wrap(fakeBlockState);
        else
            return MCCBlockData.wrap(block.getBlockData());
    }

    static MCCBlockData wrap(BlockData vanillaBlockData) {
        return new Vanilla(vanillaBlockData);
    }

    static MCCBlockData wrap(Material vanillaMaterial) {
        return new Vanilla(Bukkit.createBlockData(vanillaMaterial));
    }

    static MCCBlockData wrap(de.verdox.mccreativelab.world.block.FakeBlock.FakeBlockState fakeBlockState) {
        return new FakeBlockState(fakeBlockState);
    }

    default void setBlock(Location location){
        setBlock(location, true);
    }
    void setBlock(Location location, boolean applyPhysics);

    class Vanilla extends MCCWrapped.Impl<BlockData> implements MCCBlockData {

        public static final NBTSerializer<Vanilla> INSTANCE = new NBTSerializer<>() {
            @Override
            public void serialize(Vanilla data, NBTContainer nbtContainer) {
                nbtContainer.set("block_data", data.getHandle().getAsString());
            }

            @Override
            public Vanilla deserialize(NBTContainer nbtContainer) {
                if (!nbtContainer.has("block_data"))
                    return null;
                return new Vanilla(Bukkit.createBlockData(nbtContainer.getString("block_data")));
            }
        };

        protected Vanilla(BlockData handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getMaterial().getKey();
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if(mccWrapped instanceof Vanilla vanilla)
                return vanilla.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public void setBlock(Location location, boolean applyPhysics) {
            location.getBlock().setBlockData(this.getHandle(), applyPhysics);
            FakeBlockStorage.setFakeBlock(location, null, false, false);
        }
    }

    class FakeBlockState extends MCCWrapped.Impl<de.verdox.mccreativelab.world.block.FakeBlock.FakeBlockState> implements MCCBlockData {

        public static final NBTSerializer<FakeBlockState> INSTANCE = new NBTSerializer<>() {

            @Override
            public void serialize(FakeBlockState data, NBTContainer nbtContainer) {
                nbtContainer.set("block_type", data.getHandle().getFakeBlock().getKey().asString());
                nbtContainer.set("state_id", data.getHandle().getFakeBlock().getBlockStateID(data.getHandle()));
            }

            @Override
            public FakeBlockState deserialize(NBTContainer nbtContainer) {
                if(!nbtContainer.has("block_type") || !nbtContainer.has("state_id"))
                    return null;
                NamespacedKey key = NamespacedKey.fromString(nbtContainer.getString("block_type"));
                int id = nbtContainer.getInt("state_id");
                return new FakeBlockState(MCCreativeLabExtension.getFakeBlockRegistry().get(key).getBlockState(id));
            }
        };

        protected FakeBlockState(de.verdox.mccreativelab.world.block.FakeBlock.FakeBlockState handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getFakeBlock().getKey();
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if(mccWrapped instanceof FakeBlockState fakeBlockState)
                return fakeBlockState.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public void setBlock(Location location, boolean applyPhysics) {
            FakeBlockStorage.setFakeBlockState(location, getHandle(), applyPhysics, false);
        }
    }
}

package de.verdox.mccreativelab.wrapper.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.serialization.NBTSerializer;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.wrapper.MCCWrapped;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public interface MCCBlockData extends MCCWrapped {
    static MCCBlockData wrap(BlockData vanillaBlockData) {
        return new Vanilla(vanillaBlockData);
    }

    static MCCBlockData wrap(de.verdox.mccreativelab.world.block.FakeBlock.FakeBlockState fakeBlockState) {
        return new FakeBlockState(fakeBlockState);
    }

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
    }
}

package de.verdox.mccreativelab.wrapper.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.serialization.NBTSerializer;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.wrapper.MCCWrapped;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface MCCBlock extends MCCWrapped {
    static MCCBlock wrap(Material blockMaterial) {
        assert blockMaterial.isBlock();
        return new Vanilla(blockMaterial);
    }

    List<MCCBlockData> getAllBlockStates();

    static MCCBlock wrap(de.verdox.mccreativelab.world.block.FakeBlock fakeBlock) {
        return new FakeBlock(fakeBlock);
    }

    class Vanilla extends MCCWrapped.Impl<Material> implements MCCBlock {

        public static final NBTSerializer<Vanilla> SERIALIZER = new NBTSerializer<>() {
            @Override
            public void serialize(Vanilla data, NBTContainer nbtContainer) {
                nbtContainer.set("block_type", data.getHandle());
            }

            @Override
            public Vanilla deserialize(NBTContainer nbtContainer) {
                if (nbtContainer.has("block_type"))
                    return new Vanilla(Material.getMaterial(nbtContainer.getString("block_type")));
                return null;
            }
        };

        protected Vanilla(Material handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getKey();
        }

        @Override
        public List<MCCBlockData> getAllBlockStates() {
            return List.of();
        }
    }

    class FakeBlock extends MCCWrapped.Impl<de.verdox.mccreativelab.world.block.FakeBlock> implements MCCBlock {

        public static final NBTSerializer<FakeBlock> SERIALIZER = new NBTSerializer<>() {
            @Override
            public void serialize(FakeBlock data, NBTContainer nbtContainer) {
                nbtContainer.set("block_type", data.getHandle().getKey().asString());
            }

            @Override
            public FakeBlock deserialize(NBTContainer nbtContainer) {
                if(!nbtContainer.has("block_type"))
                    return null;
                NamespacedKey namespacedKey = NamespacedKey.fromString(nbtContainer.getString("block_type"));
                return new FakeBlock(MCCreativeLabExtension.getFakeBlockRegistry().get(namespacedKey));
            }
        };

        protected FakeBlock(de.verdox.mccreativelab.world.block.FakeBlock handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getKey();
        }

        @Override
        public List<MCCBlockData> getAllBlockStates() {
            return Arrays.stream(getHandle().getFakeBlockStates()).map(MCCBlockData::wrap).toList();
        }
    }

}

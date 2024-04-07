package de.verdox.mccreativelab.wrapper;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.wrapper.block.MCCBlockData;
import de.verdox.mccreativelab.wrapper.block.MCCBlockType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface MCCWorld extends MCCWrapped {
    static MCCWorld wrap(World vanillaWorld) {
        Objects.requireNonNull(vanillaWorld);
        return new Vanilla(vanillaWorld);
    }
    MCCBlockData getBlockDataAt(int x, int y, int z);
    MCCBlockType getBlockTypeAt(int x, int y, int z);

    default MCCBlockData getBlockDataAt(Vector vector) {
        return getBlockDataAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    default MCCBlockType getBlockTypeAt(Vector vector) {
        return getBlockTypeAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    default MCCBlockData getBlockDataAt(Location location) {
        return getBlockDataAt(location.toVector());
    }

    default MCCBlockType getBlockTypeAt(Location location) {
        return getBlockTypeAt(location.toVector());
    }

    class Vanilla extends MCCWrapped.Impl<World> implements MCCWorld {
        protected Vanilla(World handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getKey();
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if (mccWrapped instanceof MCCWorld.Vanilla world)
                return world.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public MCCBlockData getBlockDataAt(int x, int y, int z) {
            if (MCCreativeLabExtension.isServerSoftware()) {
                FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockState(new Location(getHandle(), x, y, z), false);
                if (fakeBlockState != null)
                    MCCBlockData.wrap(fakeBlockState);
            }
            return MCCBlockData.wrap(getHandle().getBlockAt(x, y, z).getBlockData());
        }

        @Override
        public MCCBlockType getBlockTypeAt(int x, int y, int z) {
            if (MCCreativeLabExtension.isServerSoftware()) {
                FakeBlock fakeBlock = FakeBlockStorage.getFakeBlock(new Location(getHandle(), x, y, z), false);
                if (fakeBlock != null)
                    MCCBlockType.wrap(fakeBlock);
            }
            return MCCBlockType.wrap(getHandle().getBlockAt(x, y, z).getType());
        }
    }

}

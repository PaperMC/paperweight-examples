package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.behaviour.BlockBreakBehaviour;
import de.verdox.mccreativelab.behaviour.RandomTickBehaviour;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import de.verdox.mccreativelab.registry.CustomRegistry;
import de.verdox.mccreativelab.registry.palette.IdMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class CustomBlockRegistry extends CustomRegistry<FakeBlock> {
    public static final IdMap<FakeBlock.FakeBlockState> FAKE_BLOCK_STATE_ID_MAP = new IdMap<>();

    static {
        RandomTickBehaviour.RANDOM_TICK_BEHAVIOUR.setBehaviour(Material.PURPLE_STAINED_GLASS, new RandomTickBehaviour() {
            @Override
            public boolean isRandomlyTicking(BlockData blockData) {
                return true;
            }

            @Override
            public void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
                FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(block.getLocation(), false);
                if(fakeBlockState == null)
                    return;
                FakeBlock fakeBlock = fakeBlockState.getFakeBlock();
                fakeBlock.randomTick(fakeBlockState, block, vanillaRandomSource);
            }
        });

    }
    public <T extends FakeBlock> T register(FakeBlock.Builder<T> fakeBlockBuilder) {
        T fakeBlock = fakeBlockBuilder.buildBlock();
        register(fakeBlockBuilder.namespacedKey, fakeBlock);
        for (FakeBlock.FakeBlockState fakeBlockState : fakeBlockBuilder.blockStates) {
            fakeBlockState.linkFakeBlock(fakeBlock);
            fakeBlockState.getProperties().makeImmutable();
            FAKE_BLOCK_STATE_ID_MAP.add(fakeBlockState);
        }
        Bukkit.getLogger().info("Registering fake block " + fakeBlockBuilder.namespacedKey);
        return fakeBlock;
    }
}

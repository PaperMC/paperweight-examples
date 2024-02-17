package de.verdox.mccreativelab.world.block.display.strategy;

import de.verdox.mccreativelab.world.block.FakeBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

public class DummyBlockVisualStrategy extends FakeBlockVisualStrategy<FakeBlockVisualStrategy.FakeBlockDisplayData> {
    public static final DummyBlockVisualStrategy INSTANCE = new DummyBlockVisualStrategy();
    @Override
    public void spawnFakeBlockDisplay(Block block, FakeBlock.FakeBlockState fakeBlockState) {}

    @Override
    public void blockUpdate(Block block, FakeBlock.FakeBlockState fakeBlockState, BlockFace direction, BlockData neighbourBlockData) {}

    @Override
    protected void loadItemDisplayAsBlockDisplay(PotentialItemDisplay potentialItemDisplay) {}

    @Override
    protected FakeBlockDisplayData newData() {
        return new FakeBlockDisplayData() {
            @Override
            protected void moveTo(int x, int y, int z) {

            }

            @Override
            public void destroy() {

            }
        };
    }
}

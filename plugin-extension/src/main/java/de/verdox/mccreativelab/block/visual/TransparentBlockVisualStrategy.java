package de.verdox.mccreativelab.block.visual;

import de.verdox.mccreativelab.block.FakeBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.jetbrains.annotations.Nullable;

public class TransparentBlockVisualStrategy extends FakeBlockVisualStrategy<TransparentBlockVisualStrategy.FakeBlockFullDisplay> {
    public static final TransparentBlockVisualStrategy INSTANCE = new TransparentBlockVisualStrategy();
    @Override
    public void spawnFakeBlockDisplay(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        FakeBlockFullDisplay fakeBlockFullDisplay = getOrCreateFakeBlockDisplayData(block);

        Location blockCenter = block.getLocation().clone().add(0.5, 0.5, 0.5);

        ItemDisplay itemDisplay = (ItemDisplay) block.getWorld().spawnEntity(blockCenter, EntityType.ITEM_DISPLAY);
        setupItemDisplayNBT(itemDisplay, fakeBlockState.getFakeBlockDisplay().getFullBlockTexture(), block, fakeBlockState);
        fakeBlockFullDisplay.setStoredItemDisplay(itemDisplay);
    }

    @Override
    public void blockUpdate(Block block, FakeBlock.FakeBlockState fakeBlockState, BlockFace direction, BlockData neighbourBlockData) {}

    @Override
    protected void loadItemDisplayAsBlockDisplay(PotentialItemDisplay potentialItemDisplay) {
        Block block = potentialItemDisplay.block();
        ItemDisplay itemDisplay = potentialItemDisplay.itemDisplay();
        FakeBlock.FakeBlockState fakeBlockState = potentialItemDisplay.fakeBlockState();

        setupItemDisplayNBT(itemDisplay, fakeBlockState.getFakeBlockDisplay().getFullBlockTexture(), block, fakeBlockState);
        getOrCreateFakeBlockDisplayData(block).setStoredItemDisplay(itemDisplay);
    }

    @Override
    protected FakeBlockFullDisplay newData() {
        return new FakeBlockFullDisplay();
    }

    protected static class FakeBlockFullDisplay extends FakeBlockDisplayData{
        @Nullable
        private ItemDisplay storedItemDisplay;

        FakeBlockFullDisplay(){}

        @Nullable
        public ItemDisplay getStoredItemDisplay() {
            return storedItemDisplay;
        }

        public void setStoredItemDisplay(ItemDisplay storedItemDisplay) {
            if(this.storedItemDisplay != null && !this.storedItemDisplay.equals(storedItemDisplay))
                safelyRemoveItemDisplay(this.storedItemDisplay);
            this.storedItemDisplay = storedItemDisplay;
        }

        @Override
        protected void moveTo(int x, int y, int z) {

        }

        @Override
        public void destroy() {
            setStoredItemDisplay(null);
        }
    }
}

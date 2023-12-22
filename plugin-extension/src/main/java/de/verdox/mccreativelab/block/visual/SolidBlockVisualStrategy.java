package de.verdox.mccreativelab.block.visual;

import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This Display Strategy spawns the block faces at the neighbour block to fix lighting issues
 */
public class SolidBlockVisualStrategy extends FakeBlockVisualStrategy<SolidBlockVisualStrategy.FakeBlockFaces> {
    public static final SolidBlockVisualStrategy INSTANCE = new SolidBlockVisualStrategy();
    private static final NamespacedKey ITEM_DISPLAY_BLOCK_FACE_KEY = new NamespacedKey("mccreativelab", "linked_fake_block_face");

    @Override
    public void spawnFakeBlockDisplay(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        FakeBlockFaces fakeBlockFaces = getOrCreateFakeBlockDisplayData(block);
        for (Map.Entry<BlockFace, ItemTextureData> blockFaceItemTextureDataEntry : fakeBlockState.getFakeBlockDisplay()
                                                                                                 .getItemTextureDataPerBlockFace()
                                                                                                 .entrySet()) {
            ItemTextureData itemTextureData = blockFaceItemTextureDataEntry.getValue();
            BlockFace blockFace = blockFaceItemTextureDataEntry.getKey();
            ItemDisplay fakeBlockFace = createFakeBlockFace(blockFace, itemTextureData, block, fakeBlockState);
            fakeBlockFaces.saveBlockFaceDisplay(blockFace, fakeBlockFace);
        }
    }

    @Override
    public void blockUpdate(Block block, FakeBlock.FakeBlockState fakeBlockState, BlockFace direction, BlockData neighbourBlockData) {
        if (!neighbourBlockData.isOccluding())
            spawnFakeBlockFace(block, direction, fakeBlockState);
        else
            removeFakeBlockFace(block, direction);
    }

    @Override
    public void loadItemDisplayAsBlockDisplay(PotentialItemDisplay potentialItemDisplay) {
        Block block = potentialItemDisplay.block();
        ItemDisplay itemDisplay = potentialItemDisplay.itemDisplay();
        FakeBlock.FakeBlockState fakeBlockState = potentialItemDisplay.fakeBlockState();
        BlockFace blockFace = BlockFace.valueOf(itemDisplay.getPersistentDataContainer()
                                                           .getOrDefault(ITEM_DISPLAY_BLOCK_FACE_KEY, PersistentDataType.STRING, "SELF"));
        if (blockFace.equals(BlockFace.SELF)) {
            safelyRemoveItemDisplay(itemDisplay);
            return;
        }

        ItemTextureData blockFaceItemTextureData = fakeBlockState.getFakeBlockDisplay()
                                                                 .getItemTextureDataPerBlockFace()
                                                                 .getOrDefault(blockFace, null);

        // Current block does not have a texture for this block face. Thus, we do not need this block face display
        if (blockFaceItemTextureData == null) {
            safelyRemoveItemDisplay(itemDisplay);
            return;
        }

        setupItemDisplayNBT(itemDisplay, blockFace, blockFaceItemTextureData, block, fakeBlockState);
        getOrCreateFakeBlockDisplayData(block).saveBlockFaceDisplay(blockFace, itemDisplay);
    }

    private ItemDisplay createFakeBlockFace(BlockFace blockFace, ItemTextureData itemTextureData, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        Location blockCenter = block.getLocation().clone().add(0.5, 0.5, 0.5);
        Location spawnLocation = blockCenter.clone().add(blockFace.getDirection().clone().multiply(0.5));

        ItemDisplay itemDisplay = (ItemDisplay) block.getWorld().spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
        setupItemDisplayNBT(itemDisplay, blockFace, itemTextureData, block, fakeBlockState);

        return itemDisplay;
    }

    private void spawnFakeBlockFace(Block block, BlockFace blockFace, FakeBlock.FakeBlockState fakeBlockState) {
        ItemTextureData itemTextureData = fakeBlockState.getFakeBlockDisplay().getItemTextureDataPerBlockFace()
                                                        .getOrDefault(blockFace, null);
        if (itemTextureData == null)
            return;

        FakeBlockFaces fakeBlockFaces = getOrCreateFakeBlockDisplayData(block);
        if (fakeBlockFaces.getBlockFaceDisplay(blockFace) != null)
            return;
        ItemDisplay fakeBlockFace = createFakeBlockFace(blockFace, itemTextureData, block, fakeBlockState);
        fakeBlockFaces.saveBlockFaceDisplay(blockFace, fakeBlockFace);
    }

    private void removeFakeBlockFace(Block block, BlockFace blockFace) {
        FakeBlockFaces fakeBlockFaces = getFakeBlockDisplayData(block, false);
        if (fakeBlockFaces == null)
            return;
        fakeBlockFaces.removeBlockFaceAndDeSpawn(blockFace);
    }

    private void setupItemDisplayNBT(ItemDisplay itemDisplay, BlockFace blockFace, ItemTextureData itemTextureData, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        setupItemDisplayNBT(itemDisplay, itemTextureData, block, fakeBlockState);
        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_BLOCK_FACE_KEY, PersistentDataType.STRING, blockFace.name());
    }

    protected static class FakeBlockFaces extends FakeBlockDisplayData{
        private final Map<BlockFace, ItemDisplay> blockFaces = new HashMap<>();

        FakeBlockFaces() {
        }

        @Override
        protected void moveTo(int x, int y, int z) {
            blockFaces.forEach((blockFace, itemDisplay) -> itemDisplay.teleportAsync(itemDisplay.getLocation().clone().add(x, y, z)));
        }

        @Override
        public void destroy() {
            blockFaces.forEach((blockFace, itemDisplay) -> itemDisplay.remove());
            blockFaces.clear();
        }

        @Nullable
        public ItemDisplay getBlockFaceDisplay(BlockFace blockFace) {
            Objects.requireNonNull(blockFace);
            return blockFaces.getOrDefault(blockFace, null);
        }

        void saveBlockFaceDisplay(BlockFace blockFace, ItemDisplay itemDisplay) {
            Objects.requireNonNull(blockFace);
            Objects.requireNonNull(itemDisplay);
            if (blockFaces.containsKey(blockFace))
                removeBlockFaceAndDeSpawn(blockFace);
            blockFaces.put(blockFace, itemDisplay);
        }

        private void removeBlockFaceAndDeSpawn(BlockFace blockFace) {
            ItemDisplay itemDisplay = blockFaces.remove(blockFace);
            if (itemDisplay == null)
                return;
            safelyRemoveItemDisplay(itemDisplay);
        }
    }
}

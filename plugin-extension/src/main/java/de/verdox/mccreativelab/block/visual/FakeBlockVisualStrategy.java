package de.verdox.mccreativelab.block.visual;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.block.FakeBlock;
import de.verdox.mccreativelab.block.FakeBlockStorage;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class FakeBlockVisualStrategy<T extends FakeBlockVisualStrategy.FakeBlockDisplayData> {
    protected static final NamespacedKey ITEM_DISPLAY_LINKED_X = new NamespacedKey("mccreativelab", "linked_fake_block_x");
    protected static final NamespacedKey ITEM_DISPLAY_LINKED_Y = new NamespacedKey("mccreativelab", "linked_fake_block_y");
    protected static final NamespacedKey ITEM_DISPLAY_LINKED_Z = new NamespacedKey("mccreativelab", "linked_fake_block_z");
    protected static final NamespacedKey ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID = new NamespacedKey("mccreativelab", "linked_fake_block_id");
    protected static final NamespacedKey ITEM_DISPLAY_LINKED_FAKE_BLOCK_STATE_ID = new NamespacedKey("mccreativelab", "linked_fake_block_state_id");
    protected static final String FAKE_BLOCK_FACE_LINKING_KEY = "fakeBlockFaces";

    public static boolean isItemDisplayLinked(ItemDisplay itemDisplay) {
        return getLinkedFakeBlockID(itemDisplay) != -1;
    }

    @Nullable
    public static PotentialItemDisplay loadPotentialDisplay(ItemDisplay itemDisplay) {
        if (!isItemDisplayLinked(itemDisplay))
            return null;

        int linkedFakeBlockID = itemDisplay.getPersistentDataContainer()
                                           .getOrDefault(ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID, PersistentDataType.INTEGER, -1);
        int linkedFakeBlockStateID = itemDisplay.getPersistentDataContainer()
                                                .getOrDefault(ITEM_DISPLAY_LINKED_FAKE_BLOCK_STATE_ID, PersistentDataType.INTEGER, -1);
        Location fakeBlockLocation = getLinkedFakeBlockLocation(itemDisplay);

        // Data corrupted. Removing
        if (linkedFakeBlockID == -1 || linkedFakeBlockStateID == -1 || fakeBlockLocation == null) {
            safelyRemoveItemDisplay(itemDisplay);
            return null;
        }

        FakeBlock.FakeBlockState fakeBlockStateAtLocation = FakeBlockStorage.getFakeBlockState(fakeBlockLocation, false);
        FakeBlock storedFakeBlock = MCCreativeLabExtension.getFakeBlockRegistry().get(linkedFakeBlockID);

        // Stored fake block is unknown.
        if (storedFakeBlock == null) {
            safelyRemoveItemDisplay(itemDisplay);
            return null;
        }
        FakeBlock.FakeBlockState storedFakeBlockState = storedFakeBlock.getBlockState(linkedFakeBlockStateID);
        // Another block is saved in this location.
        if (!Objects.equals(storedFakeBlockState, fakeBlockStateAtLocation) || fakeBlockStateAtLocation == null) {
            safelyRemoveItemDisplay(itemDisplay);
            return null;
        }
        return new PotentialItemDisplay(fakeBlockLocation.getBlock(), itemDisplay, fakeBlockStateAtLocation);
    }

    public abstract void spawnFakeBlockDisplay(Block block, FakeBlock.FakeBlockState fakeBlockState);

    public void removeFakeBlockDisplay(Block block){
        T displayData = getFakeBlockDisplayData(block, false);
        if (displayData == null)
            return;
        displayData.destroy();
        block.removeMetadata(FAKE_BLOCK_FACE_LINKING_KEY, MCCreativeLabExtension.getInstance());
    }

    public abstract void blockUpdate(Block block, FakeBlock.FakeBlockState fakeBlockState, BlockFace direction, BlockData neighbourBlockData);

    protected abstract void loadItemDisplayAsBlockDisplay(PotentialItemDisplay potentialItemDisplay);

    protected void setupItemDisplayNBT(ItemDisplay itemDisplay, ItemTextureData itemTextureData, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        int fakeBlockID = MCCreativeLabExtension.getFakeBlockRegistry().getId(fakeBlockState.getFakeBlock().getKey());
        itemDisplay.setItemStack(itemTextureData.createItem());

        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_LINKED_X, PersistentDataType.INTEGER, block.getX());
        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_LINKED_Y, PersistentDataType.INTEGER, block.getY());
        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_LINKED_Z, PersistentDataType.INTEGER, block.getZ());
        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID, PersistentDataType.INTEGER, fakeBlockID);
        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_LINKED_FAKE_BLOCK_STATE_ID, PersistentDataType.INTEGER, fakeBlockState
                       .getFakeBlock().getBlockStateID(fakeBlockState));
        itemDisplay.setViewRange(5);
    }


    protected T getOrCreateFakeBlockDisplayData(Block block){
        return getFakeBlockDisplayData(block, true);
    }

    protected abstract T newData();
    protected T getFakeBlockDisplayData(Block block, boolean createIfNotExist){
        if (!block.hasMetadata(FAKE_BLOCK_FACE_LINKING_KEY)) {
            if (createIfNotExist)
                block.setMetadata(FAKE_BLOCK_FACE_LINKING_KEY, new FixedMetadataValue(MCCreativeLabExtension.getInstance(), newData()));
            else
                return null;
        }
        return (T) block.getMetadata(FAKE_BLOCK_FACE_LINKING_KEY).get(0).value();
    }

    @Nullable
    protected static Location getLinkedFakeBlockLocation(ItemDisplay itemDisplay) {
        PersistentDataContainer persistentDataContainer = itemDisplay.getPersistentDataContainer();
        if (!persistentDataContainer.has(ITEM_DISPLAY_LINKED_X, PersistentDataType.INTEGER) ||
            !persistentDataContainer.has(ITEM_DISPLAY_LINKED_Y, PersistentDataType.INTEGER) ||
            !persistentDataContainer.has(ITEM_DISPLAY_LINKED_Z, PersistentDataType.INTEGER))
            return null;
        return new Location(itemDisplay.getWorld(), persistentDataContainer.get(ITEM_DISPLAY_LINKED_X, PersistentDataType.INTEGER), persistentDataContainer.get(ITEM_DISPLAY_LINKED_Y, PersistentDataType.INTEGER), persistentDataContainer.get(ITEM_DISPLAY_LINKED_Z, PersistentDataType.INTEGER));
    }

    protected static int getLinkedFakeBlockID(ItemDisplay itemDisplay) {
        return itemDisplay.getPersistentDataContainer()
                          .getOrDefault(ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID, PersistentDataType.INTEGER, -1);
    }

    protected static void safelyRemoveItemDisplay(ItemDisplay itemDisplay) {
        Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), itemDisplay::remove);
    }

    public record PotentialItemDisplay(Block block, ItemDisplay itemDisplay, FakeBlock.FakeBlockState fakeBlockState) {
        public void load() {
            fakeBlockState.getFakeBlockDisplay().getFakeBlockVisualStrategy().loadItemDisplayAsBlockDisplay(this);
        }
    }

    protected abstract static class FakeBlockDisplayData {
        protected FakeBlockDisplayData() {
        }

        protected abstract void moveTo(int x, int y, int z);

        public abstract void destroy();
    }
}

package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FakeBlockVisualCache {
    private static final NamespacedKey ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID = new NamespacedKey("mccreativelab", "linked_fake_block_id");
    private static final NamespacedKey ITEM_DISPLAY_LINKED_FAKE_BLOCK_STATE_ID = new NamespacedKey("mccreativelab", "linked_fake_block_state_id");
    private static final NamespacedKey ITEM_DISPLAY_LINKED_X = new NamespacedKey("mccreativelab", "linked_fake_block_x");
    private static final NamespacedKey ITEM_DISPLAY_LINKED_Y = new NamespacedKey("mccreativelab", "linked_fake_block_y");
    private static final NamespacedKey ITEM_DISPLAY_LINKED_Z = new NamespacedKey("mccreativelab", "linked_fake_block_z");
    private static final String FAKE_BLOCK_FACE_LINKING_KEY = "fakeBlockFaces";
    private static final NamespacedKey ITEM_DISPLAY_BLOCK_FACE_KEY = new NamespacedKey("mccreativelab", "linked_fake_block_face");

    static void spawnFakeBlockDisplay(Block block, FakeBlock.FakeBlockState fakeBlockState) {
        FakeBlockFaces fakeBlockFaces = getOrCreateFakeBlockFaces(block);
        for (Map.Entry<BlockFace, ItemTextureData> blockFaceItemTextureDataEntry : fakeBlockState.getFakeBlockDisplay()
                                                                                                 .getItemTextureDataPerBlockFace()
                                                                                                 .entrySet()) {
            ItemTextureData itemTextureData = blockFaceItemTextureDataEntry.getValue();
            BlockFace blockFace = blockFaceItemTextureDataEntry.getKey();
            ItemDisplay fakeBlockFace = createFakeBlockFace(blockFace, itemTextureData, block, fakeBlockState);
            fakeBlockFaces.saveBlockFaceDisplay(blockFace, fakeBlockFace);
        }
    }

    static void spawnFakeBlockFace(Block block, BlockFace blockFace, FakeBlock.FakeBlockState fakeBlockState) {
        ItemTextureData itemTextureData = fakeBlockState.getFakeBlockDisplay().getItemTextureDataPerBlockFace()
                                                        .getOrDefault(blockFace, null);
        if (itemTextureData == null)
            return;

        FakeBlockFaces fakeBlockFaces = getOrCreateFakeBlockFaces(block);
        if(fakeBlockFaces.getBlockFaceDisplay(blockFace) != null)
            return;
        ItemDisplay fakeBlockFace = createFakeBlockFace(blockFace, itemTextureData, block, fakeBlockState);
        fakeBlockFaces.saveBlockFaceDisplay(blockFace, fakeBlockFace);
    }

    static void removeFakeBlockFace(Block block, BlockFace blockFace) {
        FakeBlockFaces fakeBlockFaces = getFakeBlockFaces(block, false);
        if (fakeBlockFaces == null)
            return;
        fakeBlockFaces.removeBlockFaceAndDeSpawn(blockFace);
    }

    private static FakeBlockFaces getOrCreateFakeBlockFaces(Block block) {
        return getFakeBlockFaces(block, true);
    }

    @Nullable
    private static FakeBlockFaces getFakeBlockFaces(Block block, boolean createIfNotExist) {
        if (!block.hasMetadata(FAKE_BLOCK_FACE_LINKING_KEY)) {
            if (createIfNotExist)
                block.setMetadata(FAKE_BLOCK_FACE_LINKING_KEY, new FixedMetadataValue(MCCreativeLabExtension.getInstance(), new FakeBlockFaces()));
            else
                return null;
        }
        return (FakeBlockFaces) block.getMetadata(FAKE_BLOCK_FACE_LINKING_KEY).get(0).value();
    }

    static void loadPotentialFakeBlockFace(ItemDisplay itemDisplay) {
        if (!isItemDisplayLinked(itemDisplay))
            return;

        int linkedFakeBlockID = itemDisplay.getPersistentDataContainer()
                                           .getOrDefault(ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID, PersistentDataType.INTEGER, -1);
        int linkedFakeBlockStateID = itemDisplay.getPersistentDataContainer()
                                                .getOrDefault(ITEM_DISPLAY_LINKED_FAKE_BLOCK_STATE_ID, PersistentDataType.INTEGER, -1);
        BlockFace blockFace = BlockFace.valueOf(itemDisplay.getPersistentDataContainer()
                                                           .getOrDefault(ITEM_DISPLAY_BLOCK_FACE_KEY, PersistentDataType.STRING, "SELF"));
        Location fakeBlockLocation = getLinkedFakeBlockLocation(itemDisplay);

        // Data corrupted. Removing
        if (linkedFakeBlockID == -1 || linkedFakeBlockStateID == -1 || blockFace.equals(BlockFace.SELF) || fakeBlockLocation == null) {
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), itemDisplay::remove);
            return;
        }

        FakeBlock.FakeBlockState fakeBlockStateAtLocation = FakeBlockStorage.getFakeBlockState(fakeBlockLocation, false);
        FakeBlock storedFakeBlock = MCCreativeLabExtension.getCustomBlockRegistry().get(linkedFakeBlockID);

        // Stored fake block is unknown.
        if (storedFakeBlock == null) {
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), itemDisplay::remove);
            return;
        }
        FakeBlock.FakeBlockState storedFakeBlockState = storedFakeBlock.getBlockState(linkedFakeBlockStateID);
        // Another block is saved in this location.
        if (!Objects.equals(storedFakeBlockState, fakeBlockStateAtLocation) || fakeBlockStateAtLocation == null) {
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), itemDisplay::remove);
            return;
        }

        ItemTextureData blockFaceItemTextureData = fakeBlockStateAtLocation.getFakeBlockDisplay()
                                                                           .getItemTextureDataPerBlockFace()
                                                                           .getOrDefault(blockFace, null);
        // Current block does not have a texture for this block face. Thus, we do not need this block face display
        if (blockFaceItemTextureData == null) {
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), itemDisplay::remove);
            return;
        }

        setupItemDisplayNBT(itemDisplay, blockFace, blockFaceItemTextureData, fakeBlockLocation.getBlock(), fakeBlockStateAtLocation);
        getOrCreateFakeBlockFaces(fakeBlockLocation.getBlock()).saveBlockFaceDisplay(blockFace, itemDisplay);
    }

    static void removeFakeBlockDisplay(Block block) {
        FakeBlockFaces fakeBlockFaces = getFakeBlockFaces(block, false);
        if (fakeBlockFaces == null)
            return;
        fakeBlockFaces.destroyAllBlockFaces();
        block.removeMetadata(FAKE_BLOCK_FACE_LINKING_KEY, MCCreativeLabExtension.getInstance());
    }

    private static ItemDisplay createFakeBlockFace(BlockFace blockFace, ItemTextureData itemTextureData, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        Location blockCenter = block.getLocation().clone().add(0.5, 0.5, 0.5);
        Location spawnLocation = blockCenter.clone().add(blockFace.getDirection().clone().multiply(0.5005));

        ItemDisplay itemDisplay = (ItemDisplay) block.getWorld().spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
        setupItemDisplayNBT(itemDisplay, blockFace, itemTextureData, block, fakeBlockState);
        return itemDisplay;
    }

    private static void setupItemDisplayNBT(ItemDisplay itemDisplay, BlockFace blockFace, ItemTextureData itemTextureData, Block block, FakeBlock.FakeBlockState fakeBlockState) {
        int fakeBlockID = MCCreativeLabExtension.getCustomBlockRegistry().getId(fakeBlockState.getFakeBlock().getKey());
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
        itemDisplay.getPersistentDataContainer()
                   .set(ITEM_DISPLAY_BLOCK_FACE_KEY, PersistentDataType.STRING, blockFace.name());
        itemDisplay.setViewRange(5);
    }

    private static boolean isItemDisplayLinked(ItemDisplay itemDisplay) {
        return getLinkedFakeBlockID(itemDisplay) != -1;
    }

    private static int getLinkedFakeBlockID(ItemDisplay itemDisplay) {
        return itemDisplay.getPersistentDataContainer()
                          .getOrDefault(ITEM_DISPLAY_LINKED_FAKE_BLOCK_ID, PersistentDataType.INTEGER, -1);
    }

    @Nullable
    private static Location getLinkedFakeBlockLocation(ItemDisplay itemDisplay) {
        PersistentDataContainer persistentDataContainer = itemDisplay.getPersistentDataContainer();
        if (!persistentDataContainer.has(ITEM_DISPLAY_LINKED_X, PersistentDataType.INTEGER) ||
            !persistentDataContainer.has(ITEM_DISPLAY_LINKED_Y, PersistentDataType.INTEGER) ||
            !persistentDataContainer.has(ITEM_DISPLAY_LINKED_Z, PersistentDataType.INTEGER))
            return null;
        return new Location(itemDisplay.getWorld(), persistentDataContainer.get(ITEM_DISPLAY_LINKED_X, PersistentDataType.INTEGER), persistentDataContainer.get(ITEM_DISPLAY_LINKED_Y, PersistentDataType.INTEGER), persistentDataContainer.get(ITEM_DISPLAY_LINKED_Z, PersistentDataType.INTEGER));
    }

    public static class FakeBlockFaces {
        private final Map<BlockFace, ItemDisplay> blockFaces = new HashMap<>();

        FakeBlockFaces() {
        }

        @Nullable
        public ItemDisplay getBlockFaceDisplay(BlockFace blockFace) {
            Objects.requireNonNull(blockFace);
            return blockFaces.getOrDefault(blockFace, null);
        }

        void moveFakeBlockFaces(int x, int y, int z) {
            blockFaces.forEach((blockFace, itemDisplay) -> itemDisplay.teleportAsync(itemDisplay.getLocation().clone()
                                                                                                .add(x, y, z)));
        }

        void saveBlockFaceDisplay(BlockFace blockFace, ItemDisplay itemDisplay) {
            Objects.requireNonNull(blockFace);
            Objects.requireNonNull(itemDisplay);
            if (blockFaces.containsKey(blockFace))
                removeBlockFaceAndDeSpawn(blockFace);
            blockFaces.put(blockFace, itemDisplay);
        }

        void destroyAllBlockFaces() {
            blockFaces.forEach((blockFace, itemDisplay) -> itemDisplay.remove());
            blockFaces.clear();
        }

        private void removeBlockFaceAndDeSpawn(BlockFace blockFace) {
            ItemDisplay itemDisplay = blockFaces.remove(blockFace);
            if (itemDisplay == null)
                return;
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), itemDisplay::remove);
        }
    }
}

package de.verdox.mccreativelab.world.block.entity;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.registry.OpenRegistry;
import de.verdox.mccreativelab.registry.Reference;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.Mark;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FakeBlockEntityStorage {
    private static final OpenRegistry<FakeBlockEntityType<?>> FAKE_BLOCK_ENTITY_TYPE_REGISTRY = new OpenRegistry<>("mccreativelab");
    private static final NamespacedKey FAKE_BLOCK_ENTITY_KEY = new NamespacedKey("mccreativelab", "fake_block_entity");

    public static ItemStack storeFakeBlockEntityInItemStack(@NotNull ItemStack stack, @NotNull FakeBlockEntity fakeBlockEntity) {
        return storeFakeBlockEntityInItemStack(stack, false, fakeBlockEntity);
    }

    public static ItemStack storeFakeBlockEntityInItemStack(@NotNull ItemStack stack, boolean storeInventoryContents, @NotNull FakeBlockEntity fakeBlockEntity) {
        Objects.requireNonNull(stack);
        Objects.requireNonNull(fakeBlockEntity);

        stack.editMeta(itemMeta -> {
            NBTContainer nbtContainer = NBTContainer.of("mccreativelab", itemMeta.getPersistentDataContainer());
            NBTContainer storedFakeBlockEntity = nbtContainer.createNBTContainer();
            if (storeInventoryContents)
                fakeBlockEntity.saveNBTDataWithInventory(storedFakeBlockEntity);
            else
                fakeBlockEntity.saveNBTData(storedFakeBlockEntity);
            nbtContainer.set("fakeBlockEntity", storedFakeBlockEntity);
        });
        return stack;
    }

    public static void getFakeBlockEntityDataFromItemStack(FakeBlockEntity fakeBlockEntity, ItemStack stack) {
        if (!stack.hasItemMeta())
            return;
        NBTContainer nbtContainer = NBTContainer.of("mccreativelab", stack.getItemMeta().getPersistentDataContainer());

        if (!nbtContainer.has("fakeBlockEntity"))
            return;
        NBTContainer storedFakeBlockEntity = nbtContainer.getNBTContainer("fakeBlockEntity");
        fakeBlockEntity.loadNBTData(storedFakeBlockEntity);
    }

    /*    public static FakeBlockEntity extractFakeBlockEntityFromItem(@NotNull ItemStack stack){



        }*/
    public static FakeBlockEntity getAsFakeBlockEntity(Marker marker) {
        FakeBlockEntity fakeBlockEntity = load(marker);
        if (fakeBlockEntity != null)
            return fakeBlockEntity;

        NBTContainer nbtContainer = NBTContainer.of(FAKE_BLOCK_ENTITY_KEY.namespace(), marker.getPersistentDataContainer()).getNBTContainer(FAKE_BLOCK_ENTITY_KEY.getKey());
        if (nbtContainer != null) {
            nbtContainer = nbtContainer.withNameSpace("nbt");
            if (!nbtContainer.has("id")) {
                Bukkit.getLogger().warning("Could not find type of FakeBlockEntity. Skipping...");
                marker.getPersistentDataContainer().remove(FAKE_BLOCK_ENTITY_KEY);
                marker.remove();
                return null;
            } else {
                NamespacedKey keyOfBlockEntityType = NamespacedKey.fromString(nbtContainer.getString("id"));
                if (!FAKE_BLOCK_ENTITY_TYPE_REGISTRY.contains(keyOfBlockEntityType)) {
                    Bukkit.getLogger().warning("FakeBlockEntityType " + keyOfBlockEntityType.asString() + " not found in registry. Skipping...");
                    marker.getPersistentDataContainer().remove(FAKE_BLOCK_ENTITY_KEY);
                    marker.remove();
                    return null;
                }
                FakeBlockEntityType<?> fakeBlockEntityType = FAKE_BLOCK_ENTITY_TYPE_REGISTRY.get(keyOfBlockEntityType);
                fakeBlockEntity = fakeBlockEntityType.create();
                fakeBlockEntity.setMarkerEntity(marker);
                fakeBlockEntity.setFakeBlockEntityType(fakeBlockEntityType);
                fakeBlockEntity.setNamespacedKey(keyOfBlockEntityType);
                marker.setCustomEntityBehaviour(Marker.class, new FakeBlockEntityBehaviour(fakeBlockEntity));
                return loadOrCreate(marker, fakeBlockEntity);
            }
        }

        return load(marker);
    }

    public static FakeBlockEntity getFakeBlockEntityAt(Block blockLocation) {
        Marker marker;
        if (Bukkit.isPrimaryThread())
            marker = blockLocation.getWorld().getNearbyEntitiesByType(Marker.class, blockLocation.getLocation(), 1).stream().findAny().orElse(null);
        else {
            CompletableFuture<Marker> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), () -> future.complete(blockLocation.getWorld().getNearbyEntitiesByType(Marker.class, blockLocation.getLocation(), 1).stream().findAny().orElse(null)));
            marker = future.join();
        }
        if (marker == null)
            return null;
        return getAsFakeBlockEntity(marker);
    }

    public static FakeBlockEntity createFakeBlockEntity(@NotNull FakeBlock.FakeBlockState fakeBlockState, @NotNull Location location) {
        FakeBlockEntityStorage.removeFakeBlockEntityAt(location);

        if (!fakeBlockState.getFakeBlock().hasBlockEntity())
            throw new IllegalArgumentException("The FakeBlock " + fakeBlockState.getFakeBlock().getKey().asString() + " tried to create a FakeBlockEntity while not declaring any FakeBlockEntity. This is a plugin related bug and should be reported to the plugin author!");

        Objects.requireNonNull(fakeBlockState.getFakeBlock().getFakeBlockEntityType(), "The FakeBlock " + fakeBlockState.getFakeBlock().getKey().asString() + " declared using a blockEntity but getFakeBlockEntityType() returned null. This is a plugin related bug and should be reported to the plugin author!");

        if (!FAKE_BLOCK_ENTITY_TYPE_REGISTRY.contains(fakeBlockState.getFakeBlock().getFakeBlockEntityType().getKey()))
            throw new IllegalArgumentException("The FakeBlock " + fakeBlockState.getFakeBlock().getKey().asString() + " tried to create a FakeBlockEntity with the type " + fakeBlockState.getFakeBlock().getFakeBlockEntityType().getKey().asString() + ". However, the EntityType was not registered to the BlockEntityType Registry. This is a plugin related bug and should be reported to the plugin author!");

        Marker marker = (Marker) location.getWorld().spawnEntity(location.getBlock().getLocation(), EntityType.MARKER, CreatureSpawnEvent.SpawnReason.CUSTOM);

        FakeBlockEntity fakeBlockEntity = fakeBlockState.getFakeBlock().getFakeBlockEntityType().create();

        fakeBlockEntity.setMarkerEntity(marker);
        fakeBlockEntity.setNamespacedKey(fakeBlockState.getFakeBlock().getFakeBlockEntityType().getKey());
        fakeBlockEntity.setFakeBlockEntityType(fakeBlockState.getFakeBlock().getFakeBlockEntityType());
        marker.setCustomEntityBehaviour(Marker.class, new FakeBlockEntityBehaviour(fakeBlockEntity));
        return loadOrCreate(marker, fakeBlockEntity);
    }

    public static void removeFakeBlockEntityAt(Location location) {
        FakeBlockEntity fakeBlockEntity = getFakeBlockEntityAt(location.getBlock());
        if (fakeBlockEntity == null)
            return;
        fakeBlockEntity.onRemove();
        fakeBlockEntity.getMarkerEntity().remove();
    }

    public static <T extends FakeBlockEntity> Reference<FakeBlockEntityType<T>> register(NamespacedKey namespacedKey, FakeBlock fakeBlock, Supplier<T> entityConstructor) {
        FakeBlockEntityType<T> entityType = new FakeBlockEntityType<>(namespacedKey, fakeBlock, entityConstructor);
        return FAKE_BLOCK_ENTITY_TYPE_REGISTRY.register(entityType.getKey(), entityType);
    }

    @Nullable
    private static FakeBlockEntity load(Marker marker) {
/*        if (!marker.hasMetadata(FAKE_BLOCK_ENTITY_KEY.asString()))
            return null;
        return (FakeBlockEntity) marker.getMetadata(FAKE_BLOCK_ENTITY_KEY.asString()).get(0).value();*/
        return marker.getPersistentDataContainer().getPersistentDataObjectCache().loadPersistentDataObject(FAKE_BLOCK_ENTITY_KEY, FakeBlockEntity.class);
    }

    @NotNull
    private static FakeBlockEntity loadOrCreate(Marker marker, Supplier<FakeBlockEntity> constructor) {
/*        if (!marker.hasMetadata(FAKE_BLOCK_ENTITY_KEY.asString())) {
            FakeBlockEntity fakeBlockEntity = constructor.get();
            marker.setMetadata(FAKE_BLOCK_ENTITY_KEY.asString(), new FixedMetadataValue(MCCreativeLabExtension.getInstance(), fakeBlockEntity));
            return fakeBlockEntity;
        }
        return (FakeBlockEntity) marker.getMetadata(FAKE_BLOCK_ENTITY_KEY.asString()).get(0).value();*/
        return marker.getPersistentDataContainer().getPersistentDataObjectCache().loadOrCreatePersistentDataObject(FAKE_BLOCK_ENTITY_KEY, constructor.get());
    }

    @NotNull
    private static FakeBlockEntity loadOrCreate(Marker marker, FakeBlockEntity defaultValue) {
        return loadOrCreate(marker, () -> defaultValue);
    }
}

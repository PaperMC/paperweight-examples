package de.verdox.mccreativelab.world.block.entity;

import de.verdox.mccreativelab.behaviour.entity.EntityBehaviour;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.NBTPersistent;
import de.verdox.mccreativelab.world.block.FakeBlock;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Marker;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class FakeBlockEntity implements NBTPersistent, Keyed {
    private NamespacedKey namespacedKey;
    private Marker markerEntity;
    private Location location;
    private FakeBlockEntityType<?> fakeBlockEntityType;

    public void changePosition(Location location) {
        this.location = location.getBlock().getLocation().clone();
    }

    void setMarkerEntity(@NotNull Marker markerEntity) {
        Objects.requireNonNull(markerEntity);
        this.markerEntity = markerEntity;
        this.location = markerEntity.getLocation().clone();
    }

    void setFakeBlockEntityType(FakeBlockEntityType<?> fakeBlockEntityType) {
        this.fakeBlockEntityType = fakeBlockEntityType;
    }

    void setNamespacedKey(NamespacedKey namespacedKey) {
        this.namespacedKey = namespacedKey;
    }

    public Location getLocation() {
        return location;
    }

    public FakeBlockEntityType<?> getFakeBlockEntityType() {
        return fakeBlockEntityType;
    }

    @NotNull
    public Marker getMarkerEntity() {
        return markerEntity;
    }

    void doTick() {
        tick();
        if (!markerEntity.getLocation().equals(location))
            markerEntity.teleport(location);
    }

    protected void tick() {

    }

    protected void saveNBT(NBTContainer storage) {
    }

    protected void loadNBT(NBTContainer storage) {
    }

    public void onUnload(){

    }

    /**
     * Returns the inventory of the fake block entity. If the method returns null the entity does not have any inventory.
     *
     * @return The entity inventory if it exists
     */
    @Nullable
    public abstract Inventory getContainerOfEntity();

    @Override
    public final @NotNull NamespacedKey getKey() {
        return namespacedKey;
    }


    @Override
    public final void saveNBTData(NBTContainer storage) {
        saveNBT(storage);
        storage.set("id", fakeBlockEntityType.getKey().asString());
        if (getContainerOfEntity() != null) {
            ItemStack[] items = getContainerOfEntity().getStorageContents();
            storage.set("inventory", items);
        }
    }

    @Override
    public final void loadNBTData(NBTContainer storage) {
        loadNBT(storage);
        if (getContainerOfEntity() != null && storage.has("inventory"))
            getContainerOfEntity().setStorageContents(storage.getItemArray("inventory"));
    }

    public final FakeBlockEntityType<?> getType() {
        return fakeBlockEntityType;
    }
}

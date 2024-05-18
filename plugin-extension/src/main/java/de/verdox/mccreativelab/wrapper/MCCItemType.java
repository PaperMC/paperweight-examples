package de.verdox.mccreativelab.wrapper;

import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.registry.Reference;
import de.verdox.mccreativelab.world.item.FakeItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface MCCItemType extends MCCWrapped {
    static MCCItemType of(Material material) {
        return new Vanilla(material);
    }

    static MCCItemType of(FakeItem fakeItem) {
        return new FakeItemType(fakeItem);
    }

    static MCCItemType of(Reference<? extends FakeItem> fakeItem) {
        return of(fakeItem.unwrapValue());
    }

    int getCustomModelData();

    Material getBukkitMaterial();

    ItemStack createItem();

    default CustomItemData createCustomItemData(){
        return new CustomItemData(getBukkitMaterial(), getCustomModelData());
    }

    class Vanilla extends MCCWrapped.Impl<Material> implements MCCItemType {
        protected Vanilla(Material handle) {
            super(handle);
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if (mccWrapped instanceof Vanilla vanilla)
                return vanilla.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getKey();
        }

        @Override
        public int getCustomModelData() {
            return 0;
        }

        @Override
        public Material getBukkitMaterial() {
            return getHandle();
        }

        @Override
        public ItemStack createItem() {
            return new ItemStack(getHandle());
        }
    }
    
    class FakeItemType extends MCCWrapped.Impl<FakeItem> implements MCCItemType{
        protected FakeItemType(FakeItem handle) {
            super(handle);
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if (mccWrapped instanceof FakeItemType fakeItemType)
                return fakeItemType.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getKey();
        }

        @Override
        public int getCustomModelData() {
            return getHandle().getCustomModelData();
        }

        @Override
        public Material getBukkitMaterial() {
            return getHandle().getMaterial();
        }

        @Override
        public ItemStack createItem() {
            return getHandle().createItemStack();
        }
    }
}

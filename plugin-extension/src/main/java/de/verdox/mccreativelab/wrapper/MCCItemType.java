package de.verdox.mccreativelab.wrapper;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.registry.Reference;
import de.verdox.mccreativelab.world.block.behaviour.FakeBlockBehaviour;
import de.verdox.mccreativelab.world.item.FakeItem;
import de.verdox.mccreativelab.wrapper.block.MCCBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    static MCCItemType of(ItemStack itemStack){
        Reference<? extends FakeItem> reference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(itemStack);
        if(reference != null)
            return MCCItemType.of(reference);
        else
            return MCCItemType.of(itemStack.getType());
    }

    int getCustomModelData();

    Material getBukkitMaterial();

    ItemStack createItem();

    default boolean isSame(ItemStack stack){
        return createCustomItemData().isSame(stack);
    }

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

    static void placeItemAsBlockInWorld(ItemStack stack, Location location){
        MCCItemType mccItemType = MCCItemType.of(stack);
        if(mccItemType instanceof Vanilla) {
            location.getWorld().setBlockData(location, Bukkit.createBlockData(stack.getType()));
            FakeBlockBehaviour.setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), location.getBlock().getBlockData());
        }
        else if(mccItemType instanceof FakeItemType fakeItemType){
            BlockData blockData = fakeItemType.getHandle().placeBlockAction(stack, null, location, Bukkit.createBlockData(stack.getType())).getValue();
            location.getWorld().setBlockData(location, blockData);
            FakeBlockBehaviour.setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), blockData);
        }
    }
}

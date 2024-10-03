package de.verdox.mccreativelab.wrapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.verdox.mccreativelab.MCCreativeLab;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.registry.Reference;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.world.block.behaviour.FakeBlockBehaviour;
import de.verdox.mccreativelab.world.item.FakeItem;
import de.verdox.mccreativelab.world.item.FakeItemRegistry;
import de.verdox.mccreativelab.wrapper.block.MCCBlockData;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
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
    static JsonElement serialize(MCCItemType mccItemType) {
        return WrapperSerializer.ItemTypeSerializer.INSTANCE.toJson(mccItemType);
    }

    @Nullable
    static MCCItemType deserialize(JsonElement jsonElement) {
        return WrapperSerializer.ItemTypeSerializer.INSTANCE.fromJson(jsonElement);
    }

    static MCCItemType deserialize(String json) {
        return deserialize(JsonParser.parseString(json));
    }

    static MCCItemType of(Material material) {
        return new Vanilla(material);
    }

    static MCCItemType of(Material material, int customModelData) {
        if (!MCCreativeLabExtension.isServerSoftware())
            return MCCItemType.of(material);
        Reference<? extends FakeItem> reference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(new FakeItemRegistry.Entry(material, customModelData));
        if (reference != null)
            return MCCItemType.of(reference);
        else
            return of(material);
    }

    static MCCItemType of(FakeItem fakeItem) {
        return new FakeItemType(fakeItem);
    }

    static MCCItemType of(Reference<? extends FakeItem> fakeItem) {
        return of(fakeItem.unwrapValue());
    }

    static MCCItemType of(ItemStack itemStack) {
        if (!MCCreativeLabExtension.isServerSoftware())
            return MCCItemType.of(itemStack.getType());

        Reference<? extends FakeItem> reference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(itemStack);
        if (reference != null)
            return MCCItemType.of(reference);
        else
            return MCCItemType.of(itemStack.getType());
    }

    int getCustomModelData();

    Material getBukkitMaterial();

    ItemStack createItem();

    boolean isSame(ItemStack stack);

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

        @Override
        public boolean isSame(ItemStack stack) {
            return stack.getType().equals(getHandle());
        }
    }

    class FakeItemType extends MCCWrapped.Impl<FakeItem> implements MCCItemType {
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

        @Override
        public boolean isSame(ItemStack stack) {
            FakeItemRegistry.Entry entry = FakeItemRegistry.Entry.of(stack);
            return entry.vanillaMaterial().equals(getHandle().getMaterial()) && entry.customModelData() == getHandle().getCustomModelData();
        }
    }

    static void placeItemAsBlockInWorld(ItemStack stack, Location location) {
        MCCItemType mccItemType = MCCItemType.of(stack);
        if (mccItemType instanceof Vanilla) {
            location.getWorld().setBlockData(location, Bukkit.createBlockData(stack.getType()));
            FakeBlockBehaviour.setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), location.getBlock().getBlockData());
        } else if (mccItemType instanceof FakeItemType fakeItemType) {
            BlockData blockData = fakeItemType.getHandle().placeBlockAction(stack, null, location, Bukkit.createBlockData(stack.getType())).getValue();
            location.getWorld().setBlockData(location, blockData);
            FakeBlockBehaviour.setFakeBlockStateIfReusesVanillaBlockState(location.getBlock(), blockData);
        }
    }
}

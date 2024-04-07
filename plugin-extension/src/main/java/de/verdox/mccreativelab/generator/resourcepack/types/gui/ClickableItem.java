package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClickableItem {
    private final ItemStack stack;
    private final int xSize;
    private final int ySize;
    private final BiConsumer<InventoryClickEvent, ActiveGUI> onClick;
    private final Builder builder;

    protected ClickableItem(ItemStack stack, int xSize, int ySize, BiConsumer<InventoryClickEvent, ActiveGUI> onClick, Builder builder) {
        this.stack = stack;
        this.xSize = xSize;
        this.ySize = ySize;
        this.onClick = onClick;
        this.builder = builder;
    }

    public ClickableItem withDifferentItemMeta(Consumer<ItemMeta> metaConsumer) {
        var builder = this.builder.createCopy().withItemMeta(metaConsumer);
        return new ClickableItem(builder.createStack(), xSize, ySize, onClick, builder);
    }

    Builder getBuilder() {
        return builder;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public BiConsumer<InventoryClickEvent, ActiveGUI> getOnClick() {
        return onClick;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static class Builder {
        private int xSize = 1;
        private int ySize = 1;
        private BiConsumer<InventoryClickEvent, ActiveGUI> onClick;
        private ItemStack item = new ItemStack(Material.STICK);
        Consumer<ItemMeta> metaSetup = meta -> {
        };

        public Builder(ItemStack stack) {
            this.item = stack;
        }

        public Builder(Material material) {
            this.item = new ItemStack(material);
        }

        public Builder(){}

        public Builder withClickSize(int xSize, int ySize) {
            this.xSize = xSize;
            this.ySize = ySize;
            return this;
        }

        public Builder withClick(BiConsumer<InventoryClickEvent, ActiveGUI> onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder withItemMeta(Consumer<ItemMeta> metaSetup) {
            this.metaSetup = metaSetup;
            return this;
        }

        public int getXSize() {
            return xSize;
        }

        public int getYSize() {
            return ySize;
        }

        public Builder createCopy() {
            var copy = new Builder();
            copy.xSize = this.xSize;
            copy.ySize = this.ySize;
            copy.metaSetup = this.metaSetup;
            return copy;
        }

        ItemStack createStack() {
            ItemStack stack1 = this.item.clone();
            if (metaSetup != null)
                stack1.editMeta(metaSetup);
            return stack1;
        }

        public ClickableItem build() {
            return new ClickableItem(createStack(), xSize, ySize, onClick, this);
        }
    }

}

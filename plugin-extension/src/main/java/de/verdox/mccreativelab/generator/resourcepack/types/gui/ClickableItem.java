package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.recipe.CustomItemData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClickableItem {
    private final ItemStack stack;
    private final BiConsumer<InventoryClickEvent, ActiveGUI> onClick;
    private final Builder builder;

    protected ClickableItem(ItemStack stack, BiConsumer<InventoryClickEvent, ActiveGUI> onClick, Builder builder) {
        this.stack = stack;
        this.onClick = onClick;
        this.builder = builder;
    }

    Builder getBuilder() {
        return builder;
    }

    public BiConsumer<InventoryClickEvent, ActiveGUI> getOnClick() {
        return onClick;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static class Builder {
        private BiConsumer<InventoryClickEvent, ActiveGUI> onClick = (inventoryClickEvent, activeGUI) -> {
        };
        private ItemStack item = new ItemStack(Material.STICK);
        public boolean popGUIStack = false;
        public boolean clearGUIStackAndClose = false;
        Consumer<ItemMeta> metaSetup = meta -> {
        };

        public Builder(ItemStack stack) {
            this.item = stack;
        }

        public Builder(Material material) {
            this.item = new ItemStack(material);
        }

        public Builder(CustomItemData customItemData) {
            this(customItemData.createStack());
        }

        public Builder() {
        }

        public Builder withClick(BiConsumer<InventoryClickEvent, ActiveGUI> onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder withItem(ItemStack stack) {
            this.item = stack;
            return this;
        }

        public Builder withItemMeta(Consumer<ItemMeta> metaSetup) {
            this.metaSetup = metaSetup;
            return this;
        }

        public Builder createCopy() {
            var copy = new Builder();
            copy.onClick = this.onClick;
            copy.item = this.item.clone();
            copy.popGUIStack = this.popGUIStack;
            copy.clearGUIStackAndClose = this.clearGUIStackAndClose;
            copy.metaSetup = this.metaSetup;
            return copy;
        }

        public Builder backToLastScreenOnClick() {
            popGUIStack = true;
            return this;
        }

        public Builder closeGUI() {
            clearGUIStackAndClose = true;
            return this;
        }

        ItemStack createStack() {
            ItemStack stack1 = this.item.clone();
            if (metaSetup != null)
                stack1.editMeta(metaSetup);
            return stack1;
        }

        public ClickableItem build() {
            return new ClickableItem(createStack(), onClick, this);
        }
    }

}

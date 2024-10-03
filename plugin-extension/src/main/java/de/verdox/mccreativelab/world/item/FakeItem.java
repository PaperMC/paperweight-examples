package de.verdox.mccreativelab.world.item;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.recipe.CustomItemData;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.item.data.ItemDataContainer;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FakeItem implements Keyed, ItemBehaviour {
    private NamespacedKey key;
    private Material material;
    private int customModelData;
    private FakeItemProperties fakeItemProperties;
    private Consumer<ItemMeta> metaConsumer;
    private Translatable nameTranslation;
    private Function<Translatable, Component> nameFormat;

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public FakeItemProperties getFakeItemProperties() {
        return fakeItemProperties;
    }

    public Translatable getNameTranslatable() {
        return nameTranslation;
    }

    public Consumer<ItemMeta> getMetaConsumer() {
        return metaConsumer;
    }

    public void setFakeItemProperties(FakeItemProperties fakeItemProperties) {
        this.fakeItemProperties = fakeItemProperties;
    }

    void setNameTranslatable(Translatable nameTranslation) {
        this.nameTranslation = nameTranslation;
    }

    void setMaterial(Material material) {
        this.material = material;
    }

    void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    void setMetaConsumer(Consumer<ItemMeta> metaConsumer) {
        this.metaConsumer = metaConsumer;
    }

    void setNameFormat(Function<Translatable, Component> nameFormat) {
        this.nameFormat = nameFormat;
    }

    void setKey(NamespacedKey key) {
        this.key = key;
    }

    public boolean isItem(ItemStack stack) {
        return CustomItemData.fromItemStack(stack).isSame(createItemStack());
    }

    public final ItemStack createItemStack() {
        ItemStack stack = new ItemStack(material);
        stack.editMeta((meta) -> {
            if (metaConsumer != null)
                metaConsumer.accept(meta);

            meta.setMaxStackSize(getMaxStackSize());
            if(meta instanceof Damageable damageable && getMaxDamage() > 0)
                damageable.setMaxDamage(getMaxDamage());
            meta.setFireResistant(isFireResistant());
            meta.setCustomModelData(customModelData);

            if(nameFormat != null)
                meta.itemName(nameFormat.apply(this.nameTranslation));
            else
                meta.itemName(nameTranslation.asTranslatableComponent());
            if(this.fakeItemProperties.foodProperties != null)
                this.fakeItemProperties.foodProperties.apply(meta.getFood());
        });
        ItemDataContainer.from(stack);
        //stack.setItemBehaviour(this);
        return stack;
    }

    public float getDestroySpeed(ItemStack stack, Block block, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState == null)
            return block.getDestroySpeed(stack, true);
        return 1.0f;
    }

    public void onClick(InventoryClickEvent clickEvent) {

    }

    public int getMaxStackSize(){
        return fakeItemProperties.maxStackSize;
    }

    public int getMaxDamage(){
        return fakeItemProperties.maxDamage;
    }

    public boolean isFireResistant(){
        return fakeItemProperties.isFireResistant;
    }

    @Override
    public final BehaviourResult.Object<ItemStack> getCraftRemainingItem(ItemStack stack) {
        return new BehaviourResult.Object<>(fakeItemProperties.craftingRemainingItem, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool canDropOnDeath(ItemStack stack) {
        return new BehaviourResult.Bool(!fakeItemProperties.preventDrop, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isEnchantable(ItemStack stack) {
        return new BehaviourResult.Bool(fakeItemProperties.enchantable, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool canFitInsideContainerItems(ItemStack stack) {
        return new BehaviourResult.Bool(fakeItemProperties.fitsInsideContainerItem, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool canBreakWhenMaxDamage(ItemStack stack) {
        return new BehaviourResult.Bool(fakeItemProperties.canBreak, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static class Builder<T extends FakeItem> {
        private final Material vanillaMaterial;
        private final NamespacedKey namespacedKey;
        private final Supplier<T> itemBuilder;
        private Consumer<ItemMeta> itemMetaBuilder = itemMeta -> {
        };
        private FakeItemProperties fakeItemProperties = new FakeItemProperties();
        private Asset<CustomResourcePack> texture;
        private final Translatable standardNameTranslation;

        private ItemTextureData itemTextureData;
        private ItemTextureData.ModelType modelType;
        private final String translationKey;

        @Nullable
        private Function<Translatable, Component> nameFormat;

        public Builder(NamespacedKey namespacedKey, Material vanillaMaterial, Supplier<T> itemBuilder) {
            this.itemBuilder = itemBuilder;
            Objects.requireNonNull(vanillaMaterial);
            Objects.requireNonNull(namespacedKey);
            this.vanillaMaterial = vanillaMaterial;
            this.namespacedKey = namespacedKey;
            this.translationKey = "custom_item.description_id." + new NamespacedKey(namespacedKey.namespace(), namespacedKey.getKey()).asString();
            this.standardNameTranslation = new Translatable(translationKey);
        }

        public Builder<T> withItemMeta(Consumer<ItemMeta> itemMetaBuilder) {
            this.itemMetaBuilder = itemMetaBuilder;
            return this;
        }

        public Builder<T> withItemTextureData(ItemTextureData itemTextureData) {
            this.itemTextureData = itemTextureData;
            return this;
        }

        public Builder<T> withTranslatedName(Consumer<Translatable> setup) {
            setup.accept(this.standardNameTranslation);
            return this;
        }

        public Builder<T> withNameFormat(Function<Translatable, Component> nameFormat){
            this.nameFormat = nameFormat;
            return this;
        }

        public Builder<T> withProperties(FakeItemProperties fakeItemProperties) {
            this.fakeItemProperties = fakeItemProperties;
            return this;
        }

        public Builder<T> withTexture(Asset<CustomResourcePack> texture) {
            this.texture = texture;
            return this;
        }

        public Builder<T> withModel(ItemTextureData.ModelType modelType) {
            this.modelType = modelType;
            return this;
        }

        T buildItem() {
            //int customModelData = CustomModelDataProvider.drawCustomModelData(vanillaMaterial);
            // Generating customModelData with deterministic hash value of the item key
            ItemTextureData itemTextureData;
            if (this.itemTextureData != null)
                itemTextureData = this.itemTextureData;
            else
                itemTextureData = new ItemTextureData(new NamespacedKey(namespacedKey.namespace(), "item/" + namespacedKey.value()), vanillaMaterial, texture, modelType, texture == null && modelType == null);
            int customModelData = itemTextureData.getCustomModelData();
            T value = itemBuilder.get();

            value.setMaterial(vanillaMaterial);
            value.setCustomModelData(customModelData);
            value.setKey(namespacedKey);

            if (this.standardNameTranslation != null && value.getNameTranslatable() == null)
                value.setNameTranslatable(standardNameTranslation);
            if (this.fakeItemProperties != null && value.getFakeItemProperties() == null)
                value.setFakeItemProperties(fakeItemProperties);
            if (this.itemMetaBuilder != null && value.getMetaConsumer() == null) value.setMetaConsumer(itemMetaBuilder);
            if(this.nameFormat != null) value.setNameFormat(this.nameFormat);

            Objects.requireNonNull(namespacedKey);
            Objects.requireNonNull(vanillaMaterial);

            MCCreativeLabExtension.getCustomResourcePack().registerIfNotAlready(itemTextureData);
            if (standardNameTranslation != null) MCCreativeLabExtension.getCustomResourcePack().addTranslation(standardNameTranslation);

            ItemBehaviour.ITEM_BEHAVIOUR.setBehaviour(new CustomItemData(vanillaMaterial, customModelData), value);
            return value;
        }
    }

    public static class FakeItemProperties {
        private int maxStackSize = 64;
        private int maxDamage;
        private @Nullable ItemStack craftingRemainingItem;
        private @Nullable FakeItem.FoodProperties foodProperties;
        private boolean isFireResistant;
        private boolean enchantable;
        private boolean fitsInsideContainerItem = true;
        private boolean canBreak = true;
        private boolean preventNormalDurabilityChange;
        private boolean preventDrop;
        private boolean preventInventoryClick;

        boolean isPreventNormalDurabilityChange() {
            return preventNormalDurabilityChange;
        }

        boolean isPreventDrop() {
            return preventDrop;
        }

        boolean isPreventInventoryClick() {
            return preventInventoryClick;
        }

        public FakeItemProperties preventDrop(boolean preventDrop) {
            this.preventDrop = preventDrop;
            return this;
        }

        public FakeItemProperties preventInventoryClick(boolean preventInventoryClick) {
            this.preventInventoryClick = preventInventoryClick;
            return this;
        }

        public int getMaxStackSize() {
            return maxStackSize;
        }

        public int getMaxDamage() {
            return maxDamage;
        }

        public FakeItemProperties food(FoodProperties foodComponent) {
            this.foodProperties = foodComponent;
            return this;
        }

        public FakeItemProperties preventNormalDurabilityChange(boolean preventNormalDurabilityChange) {
            this.preventNormalDurabilityChange = preventNormalDurabilityChange;
            return this;
        }

        public FakeItemProperties breaksWhenMaxDamageReached(boolean canBreak) {
            this.canBreak = canBreak;
            return this;
        }

        public FakeItemProperties fitsInsideContainerItem(boolean fitsInsideContainerItem) {
            this.fitsInsideContainerItem = fitsInsideContainerItem;
            return this;
        }

        public FakeItemProperties enchantable(boolean enchantable) {
            this.enchantable = enchantable;
            return this;
        }

        public FakeItemProperties stacksTo(int maxCount) {
            if (this.maxDamage > 0) {
                throw new RuntimeException("Unable to have damage AND stack.");
            } else {
                this.maxStackSize = maxCount;
                return this;
            }
        }

        public FakeItemProperties defaultDurability(int maxDamage) {
            return this.maxDamage == 0 ? this.durability(maxDamage) : this;
        }

        public FakeItemProperties durability(int maxDamage) {
            this.maxDamage = maxDamage;
            this.maxStackSize = 1;
            return this;
        }

        public FakeItemProperties craftRemainder(ItemStack recipeRemainder) {
            this.craftingRemainingItem = recipeRemainder;
            return this;
        }

        public FakeItemProperties fireResistant() {
            this.isFireResistant = true;
            return this;
        }
    }

    public static class FoodProperties {
        private final int nutrition;
        private final float saturationModifier;
        private final boolean canAlwaysEat;
        private final float seconds;
        private final List<Pair<PotionEffect, Float>> effects;

        FoodProperties(int nutrition, float saturationModifier, boolean canAlwaysEat, float seconds, List<Pair<PotionEffect, Float>> effects) {
            this.nutrition = nutrition;
            this.saturationModifier = saturationModifier;
            this.canAlwaysEat = canAlwaysEat;
            this.seconds = seconds;
            this.effects = effects;
        }

        public int getNutrition() {
            return nutrition;
        }

        public float getSaturationModifier() {
            return saturationModifier;
        }

        public boolean isCanAlwaysEat() {
            return canAlwaysEat;
        }

        public List<Pair<PotionEffect, Float>> getEffects() {
            return effects;
        }

        public void apply(FoodComponent foodComponent){
            foodComponent.setCanAlwaysEat(canAlwaysEat);
            foodComponent.setEatSeconds(seconds);
            foodComponent.setNutrition(nutrition);
            foodComponent.setSaturation(saturationModifier);
            effects.forEach(potionEffectFloatPair -> foodComponent.addEffect(potionEffectFloatPair.key(), potionEffectFloatPair.right()));
        }

        public static class Builder {
            private int nutrition;
            private float saturationModifier;
            private boolean canAlwaysEat;
            private final List<Pair<PotionEffect, Float>> effects = new LinkedList<>();
            private float seconds;

            public Builder nutrition(int hunger) {
                this.nutrition = hunger;
                return this;
            }

            public Builder saturationMod(float saturationModifier) {
                this.saturationModifier = saturationModifier;
                return this;
            }

            public Builder alwaysEat() {
                this.canAlwaysEat = true;
                return this;
            }

            public Builder effect(PotionEffect effect, float chance) {
                this.effects.add(Pair.of(effect, chance));
                return this;
            }

            public Builder withEatSeconds(float seconds){
                this.seconds = seconds;
                return this;
            }

            public FoodProperties build() {
                return new FoodProperties(this.nutrition, this.saturationModifier, this.canAlwaysEat, seconds, this.effects);
            }
        }
    }
}

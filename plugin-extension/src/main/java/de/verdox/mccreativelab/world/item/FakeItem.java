package de.verdox.mccreativelab.world.item;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomModelDataProvider;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.recipe.CustomItemData;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FakeItem implements Keyed, ItemBehaviour {
    private NamespacedKey key;
    private Material material;
    private int customModelData;
    private FakeItemProperties fakeItemProperties;
    private Consumer<ItemMeta> metaConsumer;
    private Translatable nameTranslatable;

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
        return nameTranslatable;
    }

    public Consumer<ItemMeta> getMetaConsumer() {
        return metaConsumer;
    }

    public void setFakeItemProperties(FakeItemProperties fakeItemProperties) {
        this.fakeItemProperties = fakeItemProperties;
    }

    void setNameTranslatable(Translatable nameTranslatable) {
        this.nameTranslatable = nameTranslatable;
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

    void setKey(NamespacedKey key) {
        this.key = key;
    }

    public boolean isItem(ItemStack stack){
        return CustomItemData.fromItemStack(stack).isSame(createItemStack());
    }

    public final ItemStack createItemStack() {
        ItemStack stack = new ItemStack(material);
        if (metaConsumer != null)
            stack.editMeta(metaConsumer);
        stack.editMeta((meta) -> {
            if (nameTranslatable != null)
                meta.displayName(Component.translatable(nameTranslatable.key())
                                          .decoration(TextDecoration.ITALIC, false));
            meta.setCustomModelData(customModelData);
        });
        //stack.setItemBehaviour(this);
        return stack;
    }

    public float getDestroySpeed(ItemStack stack, Block block, @Nullable FakeBlock.FakeBlockState fakeBlockState) {
        if (fakeBlockState == null)
            return block.getDestroySpeed(stack, true);
        return 1.0f;
    }

    public void onClick(InventoryClickEvent clickEvent){

    }

    @Override
    public final BehaviourResult.Object<org.bukkit.inventory.FoodProperties> getFoodProperties(ItemStack stack) {
        if (fakeItemProperties.foodProperties == null)
            return new BehaviourResult.Object<>(null, BehaviourResult.Object.Type.REPLACE_VANILLA);
        return new BehaviourResult.Object<>(new org.bukkit.inventory.FoodProperties() {
            @Override
            public int getNutrition() {
                return fakeItemProperties.foodProperties.getNutrition();
            }

            @Override
            public float getSaturationModifier() {
                return fakeItemProperties.foodProperties.getSaturationModifier();
            }

            @Override
            public boolean isMeat() {
                return fakeItemProperties.foodProperties.isMeat;
            }

            @Override
            public boolean canAlwaysEat() {
                return fakeItemProperties.foodProperties.canAlwaysEat;
            }

            @Override
            public boolean isFastFood() {
                return fakeItemProperties.foodProperties.fastFood;
            }

            @Override
            public List<Pair<PotionEffect, Float>> getEffects() {
                return fakeItemProperties.foodProperties.getEffects();
            }
        }, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public final BehaviourResult.Object<Integer> getMaxStackSize(ItemStack stack) {
        return new BehaviourResult.Object<>(fakeItemProperties.maxStackSize, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public final BehaviourResult.Object<Integer> getMaxDamage(ItemStack stack) {
        return new BehaviourResult.Object<>(fakeItemProperties.maxDamage, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public final BehaviourResult.Bool isFireResistant(ItemStack stack) {
        return new BehaviourResult.Bool(fakeItemProperties.isFireResistant, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    @Override
    public final BehaviourResult.Object<ItemStack> getCraftRemainingItem(ItemStack stack) {
        return new BehaviourResult.Object<>(fakeItemProperties.craftingRemainingItem, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }

    @Override
    public BehaviourResult.Bool isEdible(ItemStack stack) {
        return new BehaviourResult.Bool(fakeItemProperties.foodProperties != null, BehaviourResult.Bool.Type.REPLACE_VANILLA);
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
        private Translatable translatable;

        public Builder(NamespacedKey namespacedKey, Material vanillaMaterial, Supplier<T> itemBuilder) {
            this.itemBuilder = itemBuilder;
            Objects.requireNonNull(vanillaMaterial);
            Objects.requireNonNull(namespacedKey);
            this.vanillaMaterial = vanillaMaterial;
            this.namespacedKey = namespacedKey;
        }

        public Builder<T> withItemMeta(Consumer<ItemMeta> itemMetaBuilder) {
            this.itemMetaBuilder = itemMetaBuilder;
            return this;
        }

        public Builder<T> withStandardName(Translatable translatable) {
            this.translatable = translatable;
            return this;
        }

        public Builder<T> withStandardName(String standardName) {
            return withStandardName(new Translatable(LanguageInfo.ENGLISH_US, "custom_item.description_id." + new NamespacedKey(namespacedKey.namespace(), namespacedKey.getKey()).asString(), standardName));
        }

        public Builder<T> withProperties(FakeItemProperties fakeItemProperties) {
            this.fakeItemProperties = fakeItemProperties;
            return this;
        }

        public Builder<T> withTexture(Asset<CustomResourcePack> texture) {
            this.texture = texture;
            return this;
        }

        T buildItem() {
            //int customModelData = CustomModelDataProvider.drawCustomModelData(vanillaMaterial);
            // Generating customModelData with deterministic hash value of the item key
            ItemTextureData itemTextureData = new ItemTextureData(new NamespacedKey(namespacedKey.namespace(), "item/" + namespacedKey.value()), vanillaMaterial, texture, null, texture == null);
            int customModelData = itemTextureData.getCustomModelData();
            T value = itemBuilder.get();

            value.setMaterial(vanillaMaterial);
            value.setCustomModelData(customModelData);
            value.setKey(namespacedKey);

            if (this.translatable != null && value.getNameTranslatable() == null) value.setNameTranslatable(translatable);
            if (this.fakeItemProperties != null && value.getFakeItemProperties() == null) value.setFakeItemProperties(fakeItemProperties);
            if (this.itemMetaBuilder != null && value.getMetaConsumer() == null) value.setMetaConsumer(itemMetaBuilder);

            Objects.requireNonNull(namespacedKey);
            Objects.requireNonNull(vanillaMaterial);

            MCCreativeLabExtension.getCustomResourcePack().register(itemTextureData);
            if (translatable != null) MCCreativeLabExtension.getCustomResourcePack().addTranslation(translatable);

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
        private boolean fitsInsideContainerItem;
        private boolean canBreak;
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
        private final boolean isMeat;
        private final boolean canAlwaysEat;
        private final boolean fastFood;
        private final List<Pair<PotionEffect, Float>> effects;

        FoodProperties(int nutrition, float saturationModifier, boolean isMeat, boolean canAlwaysEat, boolean fastFood, List<Pair<PotionEffect, Float>> effects) {
            this.nutrition = nutrition;
            this.saturationModifier = saturationModifier;
            this.isMeat = isMeat;
            this.canAlwaysEat = canAlwaysEat;
            this.fastFood = fastFood;
            this.effects = effects;
        }

        public int getNutrition() {
            return nutrition;
        }

        public float getSaturationModifier() {
            return saturationModifier;
        }

        public boolean isMeat() {
            return isMeat;
        }

        public boolean isCanAlwaysEat() {
            return canAlwaysEat;
        }

        public boolean isFastFood() {
            return fastFood;
        }

        public List<Pair<PotionEffect, Float>> getEffects() {
            return effects;
        }

        public static class Builder {
            private int nutrition;
            private float saturationModifier;
            private boolean isMeat;
            private boolean canAlwaysEat;
            private boolean fastFood;
            private final List<Pair<PotionEffect, Float>> effects = new LinkedList<>();

            public Builder nutrition(int hunger) {
                this.nutrition = hunger;
                return this;
            }

            public Builder saturationMod(float saturationModifier) {
                this.saturationModifier = saturationModifier;
                return this;
            }

            public Builder meat() {
                this.isMeat = true;
                return this;
            }

            public Builder alwaysEat() {
                this.canAlwaysEat = true;
                return this;
            }

            public Builder fast() {
                this.fastFood = true;
                return this;
            }

            public Builder effect(PotionEffect effect, float chance) {
                this.effects.add(Pair.of(effect, chance));
                return this;
            }

            public FoodProperties build() {
                return new FoodProperties(this.nutrition, this.saturationModifier, this.isMeat, this.canAlwaysEat, this.fastFood, this.effects);
            }
        }
    }
}

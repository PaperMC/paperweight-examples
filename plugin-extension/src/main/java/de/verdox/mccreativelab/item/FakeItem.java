package de.verdox.mccreativelab.item;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.item.behaviour.VanillaReplacingItemBehaviour;
import de.verdox.mccreativelab.recipe.CustomItemData;
import io.papermc.paper.inventory.ItemRarity;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FoodProperties;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FakeItem implements Keyed, VanillaReplacingItemBehaviour {
    private NamespacedKey key;
    private final Material material;
    private final int customModelData;
    private FakeItemProperties fakeItemProperties;
    private Consumer<ItemMeta> metaConsumer;

    protected FakeItem(Material material, int customModelData) {
        this.material = material;
        this.customModelData = customModelData;
    }

    void setFakeItemProperties(FakeItemProperties fakeItemProperties) {
        this.fakeItemProperties = fakeItemProperties;
    }

    void setMetaConsumer(Consumer<ItemMeta> metaConsumer) {
        this.metaConsumer = metaConsumer;
    }

    void setKey(NamespacedKey key) {
        this.key = key;
    }

    public final ItemStack createItemStack() {
        ItemStack stack = new ItemStack(material);
        if(metaConsumer != null)
            stack.editMeta(metaConsumer);
        stack.editMeta((meta) -> meta.setCustomModelData(customModelData));
        return stack;
    }

    @Override
    public final BehaviourResult.Object<FoodProperties> getFoodProperties(ItemStack stack) {
        if(fakeItemProperties.foodProperties == null)
            return new BehaviourResult.Object<>(null, BehaviourResult.Object.Type.REPLACE_VANILLA);
        return new BehaviourResult.Object<>(new FoodProperties() {
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
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static class Builder<T extends FakeItem> {
        public static final Map<Material, AtomicInteger> customModelCounters = new HashMap<>();
        private final Material vanillaMaterial;
        private final NamespacedKey namespacedKey;
        private final Class<? extends T> fakeItemClass;
        private Consumer<ItemMeta> itemMetaBuilder;
        private FakeItemProperties fakeItemProperties;

        public Builder(NamespacedKey namespacedKey, Material vanillaMaterial, Class<? extends T> fakeItemClass) {
            Objects.requireNonNull(vanillaMaterial);
            Objects.requireNonNull(namespacedKey);
            Objects.requireNonNull(fakeItemClass);
            this.vanillaMaterial = vanillaMaterial;
            this.namespacedKey = namespacedKey;
            this.fakeItemClass = fakeItemClass;
        }

        public Builder<T> withItemMeta(Consumer<ItemMeta> itemMetaBuilder) {
            this.itemMetaBuilder = itemMetaBuilder;
            return this;
        }

        public Builder<T> withProperties(FakeItemProperties fakeItemProperties) {
            this.fakeItemProperties = fakeItemProperties;
            return this;
        }

        public Builder<T> withTexture() {
            return this;
        }

        T buildItem(){
            try {
                var constructor = fakeItemClass.getDeclaredConstructor(Material.class, int.class);
                constructor.setAccessible(true);
                int customModelData = customModelCounters.computeIfAbsent(vanillaMaterial, material1 -> new AtomicInteger(5000)).getAndIncrement();
                T value = constructor.newInstance(vanillaMaterial, customModelData);

                value.setFakeItemProperties(fakeItemProperties);
                value.setMetaConsumer(itemMetaBuilder);
                value.setKey(namespacedKey);
                ItemBehaviour.ITEM_BEHAVIOUR.setBehaviour(new CustomItemData(vanillaMaterial, customModelData), value);
                return value;
            } catch (NoSuchMethodException e) {
                Bukkit.getLogger()
                      .warning("FakeItem class " + fakeItemClass.getSimpleName() + " does not implement base constructor(Material, Integer)");
                throw new RuntimeException(e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class FakeItemProperties {
        private int maxStackSize = 64;
        private int maxDamage;
        private @Nullable ItemStack craftingRemainingItem;
        private @Nullable FakItemFoodProperties foodProperties;
        private boolean isFireResistant;

        public FakeItemProperties food(FakItemFoodProperties foodComponent) {
            this.foodProperties = foodComponent;
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

    public static class FakItemFoodProperties {
        private final int nutrition;
        private final float saturationModifier;
        private final boolean isMeat;
        private final boolean canAlwaysEat;
        private final boolean fastFood;
        private final List<Pair<PotionEffect, Float>> effects;

        FakItemFoodProperties(int nutrition, float saturationModifier, boolean isMeat, boolean canAlwaysEat, boolean fastFood, List<Pair<PotionEffect, Float>> effects) {
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

            public FakItemFoodProperties build() {
                return new FakItemFoodProperties(this.nutrition, this.saturationModifier, this.isMeat, this.canAlwaysEat, this.fastFood, this.effects);
            }
        }
    }
}

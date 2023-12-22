package de.verdox.mccreativelab.crops;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.CropUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CustomCropSettings {
    public static class Display extends ResourcePackResource {
        public static final BlockData AGE_0 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(0));
        public static final BlockData AGE_1 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(1));
        public static final BlockData AGE_2 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(2));
        public static final BlockData AGE_3 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(3));
        public static final BlockData AGE_4 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(4));
        public static final BlockData AGE_5 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(5));
        public static final BlockData AGE_6 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(6));
        public static final BlockData AGE_7 = Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(7));
        private static final Map<Integer, BlockData> ageBlockDataCache = Map.of(
                0, AGE_0,
                1, AGE_1,
                2, AGE_2,
                3, AGE_3,
                4, AGE_4,
                5, AGE_5,
                6, AGE_6,
                7, AGE_7
        );
        private final List<Asset<CustomResourcePack>> ageTextures;
        private final ItemTextureData.ModelType modelType;
        private final List<BlockData> hitbox;

        public Display(NamespacedKey namespacedKey, List<Asset<CustomResourcePack>> ageTextures, ItemTextureData.ModelType modelType, List<BlockData> hitbox){
            super(namespacedKey);
            this.ageTextures = ageTextures;
            this.modelType = modelType;
            this.hitbox = hitbox;
        }

        @NotNull
        public BlockData getBlockDataForHitbox(int age) {
            if (age < 0 || age > 7)
                throw new IllegalArgumentException("Age must be between 0 and 7");

            if (age < this.hitbox.size()) {
                return hitbox.get(age);
            } else
                return Objects.requireNonNull(ageBlockDataCache.get(age));
        }

        @Override
        public void installResourceToPack(CustomResourcePack customPack) throws IOException {

        }
    }
    public record GrowthRate(TimeUnit timeUnit, long time) {
        public static GrowthRate of(TimeUnit timeUnit, long time) {
            return new GrowthRate(timeUnit, time);
        }

        public long getAvgTicksToGrow() {
            return timeUnit.toSeconds(time) * 20;
        }

        float getMalus(int cropAges) {
            return CropUtil.calculatePercentageMalus(3, cropAges, timeUnit, time);
        }
    }
}

package de.verdox.mccreativelab.world.block.replaced;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.debug.vanilla.CropRandomTickBehaviour;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.world.block.FakeBlockStorage;
import de.verdox.mccreativelab.world.block.display.TransparentFullBlockEntityDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class ReplacedCrop extends FakeBlock {
    protected ReplacedCrop(List<FakeBlockState> fakeBlockStates) {
        super(fakeBlockStates);
    }

    private final ReplacedCropRandomTickBehaviour vanillaCropRandomTickBehaviour = new ReplacedCropRandomTickBehaviour(9);

    public static ItemTextureData.ModelType createFakeCropModel(NamespacedKey cropTexture) {
        return new ItemTextureData.ModelType("minecraft:block/cube_all", (namespacedKey, jsonObject) ->
            JsonObjectBuilder.create(jsonObject)
                .add("parent", "minecraft:block/crop")
                .add("textures",
                    JsonObjectBuilder.create().add("crop", cropTexture.asString()))
                .build());
    }

    @Override
    public BehaviourResult.Void randomTick(Block block, VanillaRandomSource vanillaRandomSource) {
        return vanillaCropRandomTickBehaviour.randomTick(block, vanillaRandomSource);
    }

    @Override
    public BehaviourResult.Bool isBlockRandomlyTicking(Block block, BlockData blockData) {
        return vanillaCropRandomTickBehaviour.isBlockRandomlyTicking(block, blockData);
    }

    @Override
    public BehaviourResult.Bool isBlockDataRandomlyTicking(BlockData blockData) {
        return vanillaCropRandomTickBehaviour.isBlockDataRandomlyTicking(blockData);
    }

    public static void createFakeCropBlockState(FakeBlock.FakeBlockState.Builder builder, NamespacedKey namespacedKey, FakeBlock.FakeBlockHitbox fakeBlockHitbox, Supplier<BlockData> blockDataFunction) {
        builder
            .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.fromVanillaBlockData(fakeBlockHitbox.getBlockData()))
            .withBlockDisplay(new TransparentFullBlockEntityDisplay.Builder()
                .withModel(ReplacedCrop.createFakeCropModel(namespacedKey))
                .withDestroyParticleData(blockDataFunction.get())
                .withHitbox(fakeBlockHitbox)
            );
    }

    @Override
    public BehaviourResult.Bool fertilizeAction(Block block, ItemStack stack) {
        int currentAge = vanillaCropRandomTickBehaviour.getAge(block);
        int maxAge = vanillaCropRandomTickBehaviour.getMaxAge(block);
        if(currentAge == maxAge)
            return bool(false);
        int newAge = currentAge + vanillaCropRandomTickBehaviour.getBoneMealAgeIncrease(block, ThreadLocalRandom.current());


        if (newAge > maxAge) {
            newAge = maxAge;
        }

        vanillaCropRandomTickBehaviour.ageUpCrop(block, newAge);
        return bool(true);
    }

    @Override
    protected List<ItemStack> drawLoot(Block block, FakeBlockState fakeBlockState, @Nullable Entity causeOfItemDrop, @Nullable ItemStack toolUsed, boolean ignoreTool) {
        return List.copyOf(block.getDrops(toolUsed, causeOfItemDrop));
    }

    public static FakeBlockHitbox createCropHitbox(int age) {
        if (age < 0 || age > 7)
            throw new IllegalStateException("Age must be between 0 and 7");
        return new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(age))) {
            @Override
            protected void makeInvisible(CustomResourcePack customResourcePack, Asset<CustomResourcePack> emptyBlockModelAsset, Asset<CustomResourcePack> emptyBlockStatesFile) {
                makeModelEmpty(customResourcePack, new NamespacedKey("minecraft", "block/wheat_stage" + age));
            }
        };
    }

    public class ReplacedCropRandomTickBehaviour extends CropRandomTickBehaviour {
        public ReplacedCropRandomTickBehaviour(int minLightLevel) {
            super(minLightLevel);
        }


        @Override
        protected boolean validate(Block block) {
            return ReplacedCrop.this.equals(FakeBlockStorage.getFakeBlock(block.getLocation(), false));
        }

        @Override
        protected int getAge(Block block) {
            return ((Ageable) block.getBlockData()).getAge();
        }

        @Override
        protected int getMaxAge(Block block) {
            return 7;
        }

        @Override
        protected boolean isSameCrop(Block block, Location relativePos) {
            return Objects.equals(FakeBlockStorage.getFakeBlockState(block.getLocation(), false), FakeBlockStorage.getFakeBlockState(block.getLocation(), false));
        }

        @Override
        protected void ageUpCrop(Block block, int newAge) {
            int maxAge = getMaxAge(block);
            FakeBlockStorage.setFakeBlockState(block.getLocation(), getFakeBlockStates()[Math.min(maxAge, newAge)], false);
        }

        @Override
        protected boolean canGrow(Block block, VanillaRandomSource vanillaRandomSource) {
            float growthSpeed = calculateCropGrowthSpeed(block);
            int modifier = getAndValidateGrowth("Wheat");
            var randomNumber = drawRandomNumber(vanillaRandomSource);
            return randomNumber < (modifier / (100.0f * (Math.floor((25.0F / growthSpeed) + 1))));
        }

        @Override
        protected int getBoneMealAgeIncrease(Block block, Random random) {
            return random.nextInt(2,5);
        }
    }
}

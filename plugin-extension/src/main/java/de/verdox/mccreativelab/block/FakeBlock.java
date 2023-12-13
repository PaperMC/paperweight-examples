package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.mccreativelab.random.VanillaRandomSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FakeBlock {
    private final FakeBlockState[] fakeBlockStates;
    private Map<FakeBlockState, Integer> blockStateToIdMapping = new HashMap<>();
    FakeBlock(List<FakeBlockState> fakeBlockStates) {
        this.fakeBlockStates = fakeBlockStates.toArray(FakeBlockState[]::new);
        for (int i = 0; i < fakeBlockStates.size(); i++) {
            blockStateToIdMapping.put(fakeBlockStates.get(i), i);
        }
    }

    public FakeBlockState[] getFakeBlockStates() {
        return fakeBlockStates;
    }

    public FakeBlockState getDefaultBlockState(){
        return getBlockState(0);
    }

    int getBlockStateID(FakeBlockState fakeBlockState){
        return blockStateToIdMapping.getOrDefault(fakeBlockState, -1);
    }

    @Nullable FakeBlockState getBlockState(int blockStateID){
        if(blockStateID >= fakeBlockStates.length)
            return null;
        return fakeBlockStates[blockStateID];
    }

    public PistonMoveReaction getPistonMoveReaction(){
        return PistonMoveReaction.MOVE;
    }

    public void randomTick(FakeBlockState fakeBlockState, Block block, VanillaRandomSource vanillaRandomSource) {}

    public void tick(FakeBlockState fakeBlockState, Block block, VanillaRandomSource vanillaRandomSource) {

    }

    /**
     * Use to implement
     * {@link org.bukkit.event.entity.EntitySpawnEvent}
     */
    public void popExperience(FakeBlockState fakeBlockState, Block block, Player player) {

    }

    public void entityInside(FakeBlockState fakeBlockState, Block block, Entity entity) {

    }

    public static class Builder<T extends FakeBlock>{
        private final Class<? extends T> fakeBlockClass;
        final NamespacedKey namespacedKey;
        final List<FakeBlockState> blockStates= new LinkedList<>();

        public Builder(NamespacedKey namespacedKey, Class<? extends T> fakeBlockClass){
            this.namespacedKey = namespacedKey;
            this.fakeBlockClass = fakeBlockClass;
        }

        public Builder<T> withBlockState(Consumer<FakeBlockState.Builder> builderConsumer){
            FakeBlockState.Builder builder = new FakeBlockState.Builder(namespacedKey);
            builderConsumer.accept(builder);
            blockStates.add(builder.build());
            return this;
        }

        T buildBlock(){
            if(blockStates.isEmpty())
                throw new IllegalStateException(namespacedKey.asString()+" must provide at least one fake block state");
            try {
                var constructor = fakeBlockClass.getDeclaredConstructor(List.class);
                return constructor.newInstance(blockStates);
            } catch (NoSuchMethodException e) {
                Bukkit.getLogger()
                      .warning("FakeBlock class " + fakeBlockClass.getSimpleName() + " does not implement base constructor(FakeBlockState[])");
                throw new RuntimeException(e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static class FakeBlockState {
        private FakeBlock fakeBlock;
        private final FakeBlockProperties properties;
        private final FakeBlockDisplay fakeBlockDisplay;
        private final FakeBlockSoundGroup fakeBlockSoundGroup;

        FakeBlockState(FakeBlockProperties properties, FakeBlockDisplay fakeBlockDisplay, FakeBlockSoundGroup fakeBlockSoundGroup) {
            this.properties = properties;
            this.fakeBlockDisplay = fakeBlockDisplay;
            this.fakeBlockSoundGroup = fakeBlockSoundGroup;
        }

        public FakeBlockDisplay getFakeBlockDisplay() {
            return fakeBlockDisplay;
        }

        public FakeBlockSoundGroup getFakeBlockSoundGroup() {
            return fakeBlockSoundGroup;
        }

        public FakeBlockProperties getProperties() {
            return properties;
        }

        public FakeBlock getFakeBlock() {
            return fakeBlock;
        }

        FakeBlockState linkFakeBlock(FakeBlock fakeBlock) {
            this.fakeBlock = fakeBlock;
            return this;
        }
        public static class Builder {
            private FakeBlockProperties fakeBlockProperties = new FakeBlockProperties();
            private FakeBlockDisplay fakeBlockDisplay;
            private FakeBlockSoundGroup fakeBlockSoundGroup;
            private final NamespacedKey namespacedKey;

            Builder(NamespacedKey namespacedKey){
                this.namespacedKey = namespacedKey;
            }

            public Builder withBlockProperties(Consumer<FakeBlockProperties> fakeBlockPropertiesConsumer){
                this.fakeBlockProperties = new FakeBlockProperties();
                fakeBlockPropertiesConsumer.accept(this.fakeBlockProperties);
                return this;
            }

            public Builder withSoundGroup(@NotNull SoundData digSound, @NotNull SoundData stepSound, @NotNull SoundData breakSound, @NotNull SoundData placeSound){
                this.fakeBlockSoundGroup = new FakeBlockSoundGroup(namespacedKey, digSound, stepSound, breakSound, placeSound);
                return this;
            }

            public Builder withBlockDisplay(Asset<CustomResourcePack> blockTexture, BlockData destroyParticles, ItemTextureData.ModelType blockModel, FakeBlockHitbox hitBox){
                this.fakeBlockDisplay = new FakeBlockDisplay(namespacedKey, blockTexture, destroyParticles, blockModel, hitBox);
                return this;
            }

            public Builder withFullBlockDisplay(Asset<CustomResourcePack> blockTexture, BlockData destroyParticles){
                this.fakeBlockDisplay = new FakeBlockDisplay(namespacedKey, blockTexture, destroyParticles, ItemTextureData.ModelType.CUBE_ALL, FakeBlockHitbox.FULL_BLOCK);
                return this;
            }

            FakeBlockState build(){
                Objects.requireNonNull(fakeBlockDisplay, namespacedKey.asString()+" must set a block display");
                return new FakeBlockState(fakeBlockProperties, fakeBlockDisplay, fakeBlockSoundGroup);
            }
        }
    }

    public static class FakeBlockDisplay extends ResourcePackResource {
        private static final AtomicInteger ID_COUNTER = new AtomicInteger(9999);
        private final Asset<CustomResourcePack> blockTexture;
        private final ItemTextureData.ModelType blockModel;
        private final FakeBlockHitbox hitBox;
        private final BlockData destroyParticles;
        private ItemTextureData itemTextureData;

        FakeBlockDisplay(NamespacedKey namespacedKey, Asset<CustomResourcePack> blockTexture, BlockData destroyParticles, ItemTextureData.ModelType blockModel, FakeBlockHitbox hitBox) {
            super(namespacedKey);
            this.blockTexture = blockTexture;
            this.blockModel = blockModel;
            this.hitBox = hitBox;
            this.destroyParticles = destroyParticles;
        }

        public Asset<CustomResourcePack> getBlockTexture() {
            return blockTexture;
        }

        public BlockData getDestroyParticles() {
            return destroyParticles;
        }

        public FakeBlockHitbox getHitBox() {
            return hitBox;
        }

        public ItemTextureData.ModelType getBlockModel() {
            return blockModel;
        }

        public ItemTextureData getItemTextureData() {
            return itemTextureData;
        }

        @Override
        public void installToDataPack(CustomResourcePack customPack) throws IOException {
            itemTextureData = new ItemTextureData(new NamespacedKey(key().namespace(), "item/fake_block/"+key().getKey()), Material.BARRIER, ID_COUNTER.getAndIncrement(), blockTexture, blockModel);
            itemTextureData.installToDataPack(customPack);
        }

        public ItemDisplay spawnFakeBlock(Location location){
            location = location.add(0.5,0.5,0.5);
            ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
            itemDisplay.setItemStack(itemTextureData.createItem());
            location.getBlock().setBlockData(hitBox.blockData);
            return itemDisplay;
        }
    }
    public static class FakeBlockSoundGroup extends ResourcePackResource {
        private final SoundData digSound;
        private final SoundData stepSound;
        private final SoundData breakSound;
        private final SoundData placeSound;

        public FakeBlockSoundGroup(NamespacedKey namespacedKey, @NotNull SoundData digSound, @NotNull SoundData stepSound, @NotNull SoundData breakSound, @NotNull SoundData placeSound) {
            super(namespacedKey);
            Objects.requireNonNull(digSound);
            Objects.requireNonNull(stepSound);
            Objects.requireNonNull(breakSound);
            Objects.requireNonNull(placeSound);
            this.digSound = digSound;
            this.stepSound = stepSound;
            this.breakSound = breakSound;
            this.placeSound = placeSound;
        }

        @Override
        public void onRegister(CustomResourcePack customPack) {
            customPack.register(digSound);
            customPack.register(stepSound);
            customPack.register(breakSound);
            customPack.register(placeSound);
        }

        public SoundData getStepSound() {
            return stepSound;
        }

        public SoundData getBreakSound() {
            return breakSound;
        }

        public SoundData getDigSound() {
            return digSound;
        }

        public SoundData getPlaceSound() {
            return placeSound;
        }

        @Override
        public void installToDataPack(CustomResourcePack customPack) throws IOException {

        }
    }

    public enum FakeBlockHitbox {
        FULL_BLOCK(Bukkit.createBlockData(Material.PURPLE_STAINED_GLASS, blockData -> {})),
        CROP_AGE_0(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(0))),
        CROP_AGE_1(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(1))),
        CROP_AGE_2(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(2))),
        CROP_AGE_3(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(3))),
        CROP_AGE_4(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(4))),
        CROP_AGE_5(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(5))),
        CROP_AGE_6(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(6))),
        CROP_AGE_7(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(7))),
        ;
        private final BlockData blockData;

        FakeBlockHitbox(BlockData blockData){
            this.blockData = blockData;
        }

        public static void makeHitBoxesInvisible(CustomResourcePack customResourcePack) throws IOException {
            Bukkit.getLogger().info("Installing invisible hitboxes");

            Asset<CustomResourcePack> emptyBlockModel = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/models/empty.json"));
            emptyBlockModel.installAsset(customResourcePack, new NamespacedKey("minecraft","block/empty"), ResourcePackAssetTypes.MODELS, "json");

/*            Asset<CustomResourcePack> emptySlabModel = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/models/empty_slab.json"));
            emptySlabModel.installAsset(customResourcePack, new NamespacedKey("minecraft","block/empty_slab"), ResourcePackAssetTypes.MODELS, "json");
            Asset<CustomResourcePack> emptySlabModelTop = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/models/empty_slab_top.json"));
            emptySlabModelTop.installAsset(customResourcePack, new NamespacedKey("minecraft","block/empty_slab_top"), ResourcePackAssetTypes.MODELS, "json");*/


/*
            Asset<CustomResourcePack> petrifiedOakSlabBlockStates = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/blockstates/petrified_oak_slab.json"));
            petrifiedOakSlabBlockStates.installAsset(customResourcePack, new NamespacedKey("minecraft","petrified_oak_slab"), ResourcePackAssetTypes.BLOCK_STATES, "json");
*/

            Asset<CustomResourcePack> purpleStainedGlassModel = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/blockstates/purple_stained_glass.json"));
            purpleStainedGlassModel.installAsset(customResourcePack, new NamespacedKey("minecraft","purple_stained_glass"), ResourcePackAssetTypes.BLOCK_STATES, "json");

            Asset<CustomResourcePack> wheatBlockStatesModified = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/blockstates/wheat.json"));
            wheatBlockStatesModified.installAsset(customResourcePack, new NamespacedKey("minecraft","wheat"), ResourcePackAssetTypes.BLOCK_STATES, "json");

            Asset<CustomResourcePack> emptyBlockTexture = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/textures/empty.png"));
            emptyBlockTexture.installAsset(customResourcePack, new NamespacedKey("minecraft","block/empty"), ResourcePackAssetTypes.TEXTURES, "png");
        }
    }

    public static class FakeBlockProperties {
        private int lightEmission = 0;
        private float explosionResistance = 0.0F;
        private float hardness = 1.0F;
        private boolean requiresCorrectToolForDrops = false;
        private boolean isRandomlyTicking = false;
        private float speedFactor = 1.0F;
        private float jumpFactor = 1.0F;
        private boolean immutable;

        FakeBlockProperties(){

        }

        public FakeBlockProperties withLightEmission(int lightEmission) {
            checkImmutability();
            this.lightEmission = lightEmission;
            return this;
        }

        public FakeBlockProperties withExplosionResistance(float explosionResistance) {
            checkImmutability();
            this.explosionResistance = explosionResistance;
            return this;
        }

        public FakeBlockProperties withBlockHardness(float hardness) {
            checkImmutability();
            this.hardness = hardness;
            return this;
        }

        public FakeBlockProperties requiresCorrectToolForDrops(boolean requiresCorrectToolForDrops) {
            checkImmutability();
            this.requiresCorrectToolForDrops = requiresCorrectToolForDrops;
            return this;
        }

        public FakeBlockProperties isRandomlyTicking(boolean isRandomlyTicking) {
            checkImmutability();
            this.isRandomlyTicking = isRandomlyTicking;
            return this;
        }

        public FakeBlockProperties withSpeedFactor(float speedFactor) {
            checkImmutability();
            this.speedFactor = speedFactor;
            return this;
        }

        public FakeBlockProperties withJumpFactor(float jumpFactor) {
            checkImmutability();
            this.jumpFactor = jumpFactor;
            return this;
        }

        private void checkImmutability(){
            if(immutable)
                throw new IllegalStateException("Block properties can't be changed after building it");
        }

        void makeImmutable(){
            immutable = true;
        }

        public int getLightEmission() {
            return lightEmission;
        }

        public float getExplosionResistance() {
            return explosionResistance;
        }

        public float getHardness() {
            return hardness;
        }

        public boolean isRequiresCorrectToolForDrops() {
            return requiresCorrectToolForDrops;
        }

        public boolean isRandomlyTicking() {
            return isRandomlyTicking;
        }

        public float getSpeedFactor() {
            return speedFactor;
        }

        public float getJumpFactor() {
            return jumpFactor;
        }
    }
}

package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.block.behaviour.VanillaReplacingBlockBehaviour;
import de.verdox.mccreativelab.block.display.FakeBlockDisplay;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

public class FakeBlock implements Keyed, VanillaReplacingBlockBehaviour {
    private final FakeBlockState[] fakeBlockStates;
    private final Map<FakeBlockState, Integer> blockStateToIdMapping = new HashMap<>();
    private NamespacedKey key;

    protected FakeBlock(List<FakeBlockState> fakeBlockStates) {
        this.fakeBlockStates = fakeBlockStates.toArray(FakeBlockState[]::new);
        for (int i = 0; i < fakeBlockStates.size(); i++) {
            blockStateToIdMapping.put(fakeBlockStates.get(i), i);
        }
    }

    FakeBlock setKey(NamespacedKey key) {
        this.key = key;
        return this;
    }

    public final FakeBlockState[] getFakeBlockStates() {
        return fakeBlockStates;
    }

    public FakeBlockState getDefaultBlockState() {
        return getBlockState(0);
    }

    public final int getBlockStateID(FakeBlockState fakeBlockState) {
        return blockStateToIdMapping.getOrDefault(fakeBlockState, -1);
    }

    @Nullable
    public final FakeBlockState getBlockState(int blockStateID) {
        if (blockStateID >= fakeBlockStates.length)
            return null;
        return fakeBlockStates[blockStateID];
    }

    //TODO: Not implemented yet -> We just block it for now
    public final PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.BLOCK;
    }

    public boolean isPreferredTool(@NotNull FakeBlockState fakeBlockState, @NotNull Block block, @NotNull Player player, @Nullable ItemStack stack) {
        return true;
    }

    public List<ItemStack> drawLoot(){
        return List.of();
    }

    public float getDestroySpeed(@NotNull FakeBlockState fakeBlockState, @NotNull Block block, @Nullable ItemStack itemStack) {
        return 1.0f;
    }

    public void remove(Location location, boolean withEffects) {
        FakeBlock.FakeBlockState fakeBlockState = FakeBlockStorage.getFakeBlockStateOrThrow(location, false);
        if (fakeBlockState == null)
            return;
        if (withEffects)
            FakeBlockUtil.simulateBlockBreakWithParticlesAndSound(fakeBlockState, location.getBlock());
        FakeBlockUtil.removeFakeBlockIfPossible(location.getBlock());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "FakeBlock{" +
            "fakeBlockStates=" + Arrays.toString(fakeBlockStates) +
            ", blockStateToIdMapping=" + blockStateToIdMapping +
            ", key=" + key +
            '}';
    }

    public static class Builder<T extends FakeBlock> {
        private final Class<? extends T> fakeBlockClass;
        final NamespacedKey namespacedKey;
        final List<FakeBlockState> blockStates = new LinkedList<>();

        public Builder(NamespacedKey namespacedKey, Class<? extends T> fakeBlockClass) {
            this.namespacedKey = namespacedKey;
            this.fakeBlockClass = fakeBlockClass;
        }

        public Builder<T> withBlockState(Consumer<FakeBlockState.Builder> builderConsumer) {
            FakeBlockState.Builder builder = new FakeBlockState.Builder(namespacedKey);
            builderConsumer.accept(builder);
            blockStates.add(builder.build());
            return this;
        }

        T buildBlock() {
            if (blockStates.isEmpty())
                throw new IllegalStateException(namespacedKey.asString() + " must provide at least one fake block state");
            try {
                var constructor = fakeBlockClass.getDeclaredConstructor(List.class);
                constructor.setAccessible(true);
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

            Builder(NamespacedKey namespacedKey) {
                this.namespacedKey = namespacedKey;
            }

            public Builder withBlockProperties(Consumer<FakeBlockProperties> fakeBlockPropertiesConsumer) {
                this.fakeBlockProperties = new FakeBlockProperties();
                fakeBlockPropertiesConsumer.accept(this.fakeBlockProperties);
                return this;
            }

            public Builder withSoundGroup(SoundData hitSound, SoundData stepSound, SoundData breakSound, SoundData placeSound, SoundData fallSound) {
                this.fakeBlockSoundGroup = new FakeBlockSoundGroup(namespacedKey, hitSound, stepSound, breakSound, placeSound, fallSound);
                return this;
            }

            public Builder withBlockDisplay(FakeBlockDisplay.Builder<?> builder){
                this.fakeBlockDisplay = builder.build(namespacedKey);
                return this;
            }

            FakeBlockState build() {
                Objects.requireNonNull(fakeBlockDisplay, namespacedKey.asString() + " must set a block display");
                return new FakeBlockState(fakeBlockProperties, fakeBlockDisplay, fakeBlockSoundGroup);
            }
        }

        @Override
        public String toString() {
            return "FakeBlockState{" +
                ", properties=" + properties +
                ", fakeBlockDisplay=" + fakeBlockDisplay +
                ", fakeBlockSoundGroup=" + fakeBlockSoundGroup +
                '}';
        }
    }

/*    public static class FakeBlockDisplay extends ResourcePackResource {
        private static final AtomicInteger ID_COUNTER = new AtomicInteger(9999);
        private final FakeBlockHitbox hitBox;
        private final BlockData destroyParticles;
        private final Map<BlockFace, ItemTextureData> itemTextureDataPerBlockFace = new HashMap<>();
        private final Map<BlockFace, Asset<CustomResourcePack>> texturesPerBlockFace;
        private final Map<BlockFace, ItemTextureData.ModelType> modelsPerBlockFace;
        private final FakeBlockVisualStrategy fakeBlockVisualStrategy;
        private final boolean isReusingMinecraftBlockstate;
        private ItemTextureData fullBlockTexture;
        private ItemTextureData.ModelType fullBlockModel;

        FakeBlockDisplay(NamespacedKey namespacedKey, BlockData destroyParticles, FakeBlockHitbox hitBox, Map<BlockFace, Asset<CustomResourcePack>> texturesPerBlockFace, Map<BlockFace, ItemTextureData.ModelType> modelsPerBlockFace, FakeBlockVisualStrategy fakeBlockVisualStrategy, boolean isReusingMinecraftBlockstate) {
            super(namespacedKey);
            this.hitBox = hitBox;
            this.destroyParticles = destroyParticles;
            this.texturesPerBlockFace = texturesPerBlockFace;
            this.modelsPerBlockFace = modelsPerBlockFace;
            this.fakeBlockVisualStrategy = fakeBlockVisualStrategy;
            this.isReusingMinecraftBlockstate = isReusingMinecraftBlockstate;
        }

        public boolean isReusingMinecraftBlockstate() {
            return isReusingMinecraftBlockstate;
        }

        public FakeBlockVisualStrategy getFakeBlockVisualStrategy() {
            return fakeBlockVisualStrategy;
        }

        public ItemTextureData getFullBlockTexture() {
            return fullBlockTexture;
        }

        public BlockData getDestroyParticles() {
            return destroyParticles;
        }

        public FakeBlockHitbox getHitBox() {
            return hitBox;
        }

        public Map<BlockFace, ItemTextureData> getItemTextureDataPerBlockFace() {
            return Map.copyOf(itemTextureDataPerBlockFace);
        }

        @Override
        public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
            texturesPerBlockFace.forEach((face, customResourcePackAsset) -> {
                ItemTextureData.ModelType modelType = modelsPerBlockFace.get(face);
                NamespacedKey faceKey = getKeyOfFaceTexture(new NamespacedKey(key().namespace(), "item/fake_block/" + key().getKey()), face);
                ItemTextureData itemTextureData = new ItemTextureData(faceKey, Material.BARRIER, ID_COUNTER.getAndIncrement(), customResourcePackAsset, modelType);
                itemTextureDataPerBlockFace.put(face, itemTextureData);
                customPack.register(itemTextureData);
            });

            NamespacedKey fullDisplayKey = getKeyOfFullDisplay(new NamespacedKey(key().namespace(), "item/fake_block/" + key().getKey()));
            this.fullBlockModel = ItemTextureData.ModelType.createFullCubeWithSeparateTextures(itemTextureDataPerBlockFace);
            ItemTextureData itemTextureData = new ItemTextureData(fullDisplayKey, Material.BARRIER, ID_COUNTER.getAndIncrement(), null, this.fullBlockModel);
            this.fullBlockTexture = itemTextureData;
            customPack.register(itemTextureData);

            if(isReusingMinecraftBlockstate)
                customPack.register(new AlternateBlockStateModel(hitBox.blockData, fullDisplayKey));
        }

        @Override
        public String toString() {
            return "FakeBlockDisplay{" +
                "hitBox=" + hitBox +
                ", destroyParticles=" + destroyParticles +
                ", itemTextureDataPerBlockFace=" + itemTextureDataPerBlockFace +
                ", texturesPerBlockFace=" + texturesPerBlockFace +
                ", modelsPerBlockFace=" + modelsPerBlockFace +
                ", fakeBlockVisualStrategy=" + fakeBlockVisualStrategy +
                ", fullBlockTexture=" + fullBlockTexture +
                ", fullBlockModel=" + fullBlockModel +
                '}';
        }

        @Override
        public void installResourceToPack(CustomResourcePack customPack) throws IOException {

        }

        private static NamespacedKey getKeyOfFaceTexture(NamespacedKey namespacedKey, BlockFace face) {
            return new NamespacedKey(namespacedKey.namespace(), namespacedKey.getKey() + "/face/" + face.name()
                                                                                                        .toLowerCase(Locale.ROOT));
        }

        private static NamespacedKey getKeyOfFullDisplay(NamespacedKey namespacedKey) {
            return new NamespacedKey(namespacedKey.namespace(), namespacedKey.getKey() + "/full_display");
        }

        public static class Builder {
            private static final List<BlockFace> validFaces = List.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
            private final NamespacedKey namespacedKey;
            private final Map<BlockFace, Asset<CustomResourcePack>> texturesPerBlockFace = new HashMap<>();
            private final Map<BlockFace, ItemTextureData.ModelType> modelsPerBlockFace = new HashMap<>();
            private FakeBlockHitbox fakeBlockHitbox = FakeBlockHitbox.SOLID_BLOCK;
            private BlockData destroyParticleData = Bukkit.createBlockData(Material.STONE);
            private FakeBlockVisualStrategy fakeBlockVisualStrategy = TransparentBlockVisualStrategy.INSTANCE;
            private boolean isReusingMinecraftBlockState;

            Builder(NamespacedKey namespacedKey) {
                this.namespacedKey = namespacedKey;
            }

            public Builder withTexture(BlockFace face, Asset<CustomResourcePack> blockTexture) {
                if (!validFaces.contains(face)) {
                    Bukkit.getLogger()
                          .warning("Cannot add texture for block face " + face + ". Allowed faces are: " + validFaces);
                    return this;
                }

                modelsPerBlockFace.put(face, ItemTextureData.ModelType.createOnlyOneSideTextureOfCube(face));
                texturesPerBlockFace.put(face, blockTexture);
                return this;
            }

            public Builder withFakeBlockVisualStrategy(FakeBlockVisualStrategy fakeBlockVisualStrategy) {
                if(!FakeBlockRegistry.USE_ALTERNATE_FAKE_BLOCK_ENGINE && !(fakeBlockVisualStrategy instanceof DummyBlockVisualStrategy))
                    throw new IllegalStateException("Alternate Fake Block Visual Engine not activated.");
                this.fakeBlockVisualStrategy = fakeBlockVisualStrategy;
                return this;
            }

            public Builder withFullBlockTexture(Asset<CustomResourcePack> blockTexture) {
                for (BlockFace validFace : validFaces)
                    withTexture(validFace, blockTexture);
                return this;
            }

            public Builder withTopAndBottomTexture(Asset<CustomResourcePack> blockTexture) {
                withTexture(BlockFace.UP, blockTexture);
                withTexture(BlockFace.DOWN, blockTexture);
                return this;
            }

            public Builder withSideTexture(Asset<CustomResourcePack> blockTexture) {
                withTexture(BlockFace.NORTH, blockTexture);
                withTexture(BlockFace.EAST, blockTexture);
                withTexture(BlockFace.SOUTH, blockTexture);
                withTexture(BlockFace.WEST, blockTexture);
                return this;
            }

            public Builder withHitbox(FakeBlockHitbox fakeBlockHitbox) {
                this.fakeBlockHitbox = fakeBlockHitbox;
                return this;
            }

            public Builder withDestroyParticles(BlockData blockData) {
                this.destroyParticleData = blockData;
                return this;
            }

            public Builder useUnusedBlockState(Material material){
                FakeBlockHitbox fakeBlockHitbox = new FakeBlockHitbox(UnusedBlockStates.getUnusedBlockState(material));
                withHitbox(fakeBlockHitbox);
                withDestroyParticles(fakeBlockHitbox.blockData);
                withFakeBlockVisualStrategy(DummyBlockVisualStrategy.INSTANCE);
                isReusingMinecraftBlockState = true;
                return this;
            }

            FakeBlockDisplay build() {
                return new FakeBlockDisplay(namespacedKey, destroyParticleData, fakeBlockHitbox, texturesPerBlockFace, modelsPerBlockFace, fakeBlockVisualStrategy, isReusingMinecraftBlockState);
            }
        }
    }*/

    public static class FakeBlockSoundGroup extends ResourcePackResource {
        private final SoundData hitSound;
        private final SoundData stepSound;
        private final SoundData breakSound;
        private final SoundData placeSound;
        private final SoundData fallSound;

        public FakeBlockSoundGroup(@NotNull NamespacedKey namespacedKey, SoundData hitSound, SoundData stepSound, SoundData breakSound, SoundData placeSound, SoundData fallSound) {
            super(namespacedKey);
            this.hitSound = hitSound;
            this.stepSound = stepSound;
            this.breakSound = breakSound;
            this.placeSound = placeSound;
            this.fallSound = fallSound;
        }

        @Override
        public void onRegister(CustomResourcePack customPack) {
            customPack.registerNullable(hitSound);
            customPack.registerNullable(stepSound);
            customPack.registerNullable(breakSound);
            customPack.registerNullable(placeSound);
            customPack.registerNullable(fallSound);
        }

        public Wrappers.SoundGroup asSoundGroup() {
            return Wrappers.of(Wrappers.of(hitSound), Wrappers.of(stepSound), Wrappers.of(breakSound), Wrappers.of(placeSound), Wrappers.of(fallSound));
        }

        @Override
        public void installResourceToPack(CustomResourcePack customPack) throws IOException {

        }

        @Override
        public String toString() {
            return "FakeBlockSoundGroup{" +
                "hitSound=" + hitSound +
                ", stepSound=" + stepSound +
                ", breakSound=" + breakSound +
                ", placeSound=" + placeSound +
                ", fallSound=" + fallSound +
                '}';
        }
    }

    public static class FakeBlockHitbox {
        public static final FakeBlockHitbox SOLID_BLOCK = new FakeBlockHitbox(Bukkit.createBlockData(Material.ANCIENT_DEBRIS, blockData -> {
        }));
        public static final FakeBlockHitbox TRANSPARENT_BLOCK = new FakeBlockHitbox(Bukkit.createBlockData(Material.PURPLE_STAINED_GLASS, blockData -> {
        }));
        public static final FakeBlockHitbox CROP_AGE_0 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(0)));
        public static final FakeBlockHitbox CROP_AGE_1 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(1)));
        public static final FakeBlockHitbox CROP_AGE_2 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(2)));
        public static final FakeBlockHitbox CROP_AGE_3 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(3)));
        public static final FakeBlockHitbox CROP_AGE_4 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(4)));
        public static final FakeBlockHitbox CROP_AGE_5 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(5)));
        public static final FakeBlockHitbox CROP_AGE_6 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(6)));
        public static final FakeBlockHitbox CROP_AGE_7 = new FakeBlockHitbox(Bukkit.createBlockData(Material.WHEAT, blockData -> ((Ageable) blockData).setAge(7)));
        private final BlockData blockData;

        FakeBlockHitbox(BlockData blockData) {
            this.blockData = blockData;
        }

        public BlockData getBlockData() {
            return blockData;
        }

        public static void makeHitBoxesInvisible(CustomResourcePack customResourcePack) throws IOException {
            Bukkit.getLogger().info("Installing invisible hitboxes");

            Asset<CustomResourcePack> emptyBlockModel = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/models/empty.json"));
            emptyBlockModel.installAsset(customResourcePack, new NamespacedKey("minecraft", "block/empty"), ResourcePackAssetTypes.MODELS, "json");

            Asset<CustomResourcePack> emptyBlockStatesFile = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/blockstates/empty_blockstates.json"));

            emptyBlockStatesFile.installAsset(customResourcePack, new NamespacedKey("minecraft", SOLID_BLOCK
                .getBlockData().getMaterial().name()
                .toLowerCase(Locale.ROOT)), ResourcePackAssetTypes.BLOCK_STATES, "json");
            emptyBlockStatesFile.installAsset(customResourcePack, new NamespacedKey("minecraft", TRANSPARENT_BLOCK
                .getBlockData().getMaterial().name()
                .toLowerCase(Locale.ROOT)), ResourcePackAssetTypes.BLOCK_STATES, "json");

            Asset<CustomResourcePack> wheatBlockStatesModified = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/blockstates/wheat.json"));
            wheatBlockStatesModified.installAsset(customResourcePack, new NamespacedKey("minecraft", "wheat"), ResourcePackAssetTypes.BLOCK_STATES, "json");

            Asset<CustomResourcePack> emptyBlockTexture = new Asset<>(() -> CustomResourcePack.class.getResourceAsStream("/empty_block/textures/empty.png"));
            emptyBlockTexture.installAsset(customResourcePack, new NamespacedKey("minecraft", "block/empty"), ResourcePackAssetTypes.TEXTURES, "png");
        }

        public static FakeBlockHitbox createFakeBlockHitbox(BlockData blockData){
            return new FakeBlockHitbox(blockData);
        }

        @Override
        public String toString() {
            return "FakeBlockHitbox{" +
                "blockData=" + blockData +
                '}';
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

        FakeBlockProperties() {

        }

        public FakeBlockProperties fromVanillaBlockData(BlockData blockData) {
            withLightEmission(blockData.getLightEmission());
            withExplosionResistance(blockData.getMaterial().getBlastResistance());
            withBlockHardness(blockData.getMaterial().getHardness());
            requiresCorrectToolForDrops(blockData.requiresCorrectToolForDrops());
            isRandomlyTicking(blockData.isRandomlyTicked());
            return this;
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

        private void checkImmutability() {
            if (immutable)
                throw new IllegalStateException("Block properties can't be changed after building it");
        }

        void makeImmutable() {
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

        @Override
        public String toString() {
            return "FakeBlockProperties{" +
                "lightEmission=" + lightEmission +
                ", explosionResistance=" + explosionResistance +
                ", hardness=" + hardness +
                ", requiresCorrectToolForDrops=" + requiresCorrectToolForDrops +
                ", isRandomlyTicking=" + isRandomlyTicking +
                ", speedFactor=" + speedFactor +
                ", jumpFactor=" + jumpFactor +
                ", immutable=" + immutable +
                '}';
        }
    }
}

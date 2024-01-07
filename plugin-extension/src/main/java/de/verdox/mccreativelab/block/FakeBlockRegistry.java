package de.verdox.mccreativelab.block;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import de.verdox.mccreativelab.block.behaviour.ReplacingFakeBlockBehaviour;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.mccreativelab.registry.CustomRegistry;
import de.verdox.mccreativelab.sound.ReplacedSoundGroups;
import de.verdox.mccreativelab.util.storage.palette.IdMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeBlockRegistry extends CustomRegistry<FakeBlock> {
    public static final IdMap<FakeBlock.FakeBlockState> FAKE_BLOCK_STATE_ID_MAP = new IdMap<>();

    public static BlockBehaviour SOLID_BLOCK_BEHAVIOUR = new BlockBehaviour() {
    };
    public static BlockBehaviour TRANSPARENT_BLOCK_BEHAVIOUR = new BlockBehaviour() {
    };
    private static final Map<BlockData, FakeBlock.FakeBlockState> reusedBlockStates = new HashMap<>();

    public static final FakeBlockDamage fakeBlockDamage = new FakeBlockDamage();

    public static void setupFakeBlocks() {

        fakeBlockDamage.init(MCCreativeLabExtension.getInstance().getCustomResourcePack());

        Material solidBlockMaterial = FakeBlock.FakeBlockHitbox.SOLID_BLOCK.getBlockData().getMaterial();
        Material transparentBlockMaterial = FakeBlock.FakeBlockHitbox.TRANSPARENT_BLOCK.getBlockData().getMaterial();

        NamespacedKey solidBlockKey = new NamespacedKey("replaced_blocks", "solid_block");
        NamespacedKey transparentBlockKey = new NamespacedKey("replaced_blocks", "transparent_block");

        BlockBehaviour.BLOCK_BEHAVIOUR.setBehaviour(solidBlockMaterial, new ReplacingFakeBlockBehaviour(solidBlockMaterial, solidBlockKey, () -> SOLID_BLOCK_BEHAVIOUR));
        BlockBehaviour.BLOCK_BEHAVIOUR.setBehaviour(transparentBlockMaterial, new ReplacingFakeBlockBehaviour(transparentBlockMaterial, transparentBlockKey, () -> TRANSPARENT_BLOCK_BEHAVIOUR));
        BlockBehaviour.BLOCK_BEHAVIOUR.setBehaviour(Material.NOTE_BLOCK, new ReplacingFakeBlockBehaviour(Material.NOTE_BLOCK));

        Asset<CustomResourcePack> ancient_debris_side_texture = new Asset<>("/replaced/blocks/ancient_debris_side.png");
        Asset<CustomResourcePack> ancient_debris_top_texture = new Asset<>("/replaced/blocks/ancient_debris_top.png");
        Asset<CustomResourcePack> purple_stained_glass_texture = new Asset<>("/replaced/blocks/purple_stained_glass.png");

        MCCreativeLabExtension
            .getFakeBlockRegistry()
            .register(new FakeBlock.Builder<>(solidBlockKey, FakeBlock.class)
                .withBlockState(builder ->
                    builder
                        .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.fromVanillaBlockData(solidBlockMaterial.createBlockData()))
                        .withBlockDisplay(builder1 ->
                            builder1.withTopAndBottomTexture(ancient_debris_top_texture)
                                    .withSideTexture(ancient_debris_side_texture)
                                    .withDestroyParticles(Bukkit.createBlockData(Material.NETHERRACK))
                        ))
            );

        MCCreativeLabExtension
            .getFakeBlockRegistry()
            .register(new FakeBlock.Builder<>(transparentBlockKey, FakeBlock.class)
                .withBlockState(builder ->
                    builder
                        .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.fromVanillaBlockData(transparentBlockMaterial.createBlockData()))
                        .withBlockDisplay(builder1 ->
                            builder1.withFullBlockTexture(purple_stained_glass_texture)
                                    .withDestroyParticles(Bukkit.createBlockData(Material.PURPLE_STAINED_GLASS_PANE)))
                )
            );

        SoundData newGlassBreakSound = new SoundData(new NamespacedKey("minecraft", "block.glass.custom.break"), false, "subtitles.block.generic.break")
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/glass/break/glass1"), new Asset<>("/sounds/glass/break/glass1.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/glass/break/glass2"), new Asset<>("/sounds/glass/break/glass2.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/glass/break/glass3"), new Asset<>("/sounds/glass/break/glass3.ogg"), 1, 1);

        Wrappers.SoundGroup newGlassSoundGroup = Wrappers.of(Wrappers.of(Sound.BLOCK_STONE_HIT), Wrappers.of(Sound.BLOCK_STONE_STEP), Wrappers.of(newGlassBreakSound), Wrappers.of(Sound.BLOCK_STONE_PLACE), Wrappers.of(Sound.BLOCK_STONE_FALL));
        ReplacedSoundGroups.replaceSoundGroup("block.glass", Material.GLASS.createBlockData()
                                                                           .getSoundGroup(), newGlassSoundGroup);

        Wrappers.SoundGroup newAncientDebrisSoundGroup = Wrappers.of(Wrappers.of(Sound.BLOCK_STONE_HIT), Wrappers.of(Sound.BLOCK_STONE_STEP), Wrappers.of(Sound.BLOCK_STONE_BREAK), Wrappers.of(Sound.BLOCK_STONE_PLACE), Wrappers.of(Sound.BLOCK_STONE_FALL));
        ReplacedSoundGroups.replaceSoundGroup("block.ancient_debris", Material.ANCIENT_DEBRIS.createBlockData()
                                                                                             .getSoundGroup(), newAncientDebrisSoundGroup);
    }

    public static boolean hasTransparentTexture(Material material) {
        return Objects.equals(material, FakeBlock.FakeBlockHitbox.SOLID_BLOCK.getBlockData().getMaterial()) ||
            Objects.equals(material, FakeBlock.FakeBlockHitbox.TRANSPARENT_BLOCK
                .getBlockData().getMaterial());
    }

    public <T extends FakeBlock> T register(FakeBlock.Builder<T> fakeBlockBuilder) {
        T fakeBlock = fakeBlockBuilder.buildBlock();
        register(fakeBlockBuilder.namespacedKey, fakeBlock);
        for (FakeBlock.FakeBlockState fakeBlockState : fakeBlockBuilder.blockStates) {
            fakeBlockState.linkFakeBlock(fakeBlock);
            fakeBlockState.getProperties().makeImmutable();
            FAKE_BLOCK_STATE_ID_MAP.add(fakeBlockState);
            if(fakeBlockState.getFakeBlockDisplay().isReusingMinecraftBlockstate())
                reusedBlockStates.put(fakeBlockState.getFakeBlockDisplay().getHitBox().getBlockData(),fakeBlockState);
        }
        fakeBlock.setKey(fakeBlockBuilder.namespacedKey);
        Bukkit.getLogger().info("Registering fake block " + fakeBlockBuilder.namespacedKey);
        return fakeBlock;
    }

    @Nullable
    public static FakeBlock.FakeBlockState getFakeBlockStateFromBlockData(BlockData blockData){
        if(reusedBlockStates.containsKey(blockData))
            return reusedBlockStates.get(blockData);
        return null;
    }

    @Deprecated
    public static class FakeBlockDamage implements Listener {
        private static final AtomicInteger ID_COUNTER = new AtomicInteger(9999);
        private final Map<Integer, ItemTextureData> texturesPerDamage = new HashMap<>();
        private final Map<Block, ItemDisplay> damages = new HashMap<>();
        private final Map<ItemDisplay, Block> itemDisplayToBlockMapping = new HashMap<>();

        FakeBlockDamage() {
        }

        private void init(CustomResourcePack customResourcePack) {
            for (int i = 0; i < 10; i++) {
                final int destroyIndex = i;
                NamespacedKey key = new NamespacedKey("mccreativelab", "item/blockbreak/fake_destroy_stage" + destroyIndex);
                ItemTextureData.ModelType modelType = ItemTextureData.ModelType.createFullCubeWithSingleTexture(() -> new NamespacedKey("minecraft", "block/destroy_stage_" + destroyIndex));
                ItemTextureData itemTextureData = new ItemTextureData(key, Material.BRAIN_CORAL, ID_COUNTER.getAndIncrement(), null, modelType);
                customResourcePack.register(itemTextureData);
                texturesPerDamage.put(i, itemTextureData);

            }
        }

        @EventHandler
        public void removeEntityIfUnload(EntityRemoveFromWorldEvent e) {
            if (!(e.getEntity() instanceof ItemDisplay itemDisplay))
                return;
            if (!itemDisplayToBlockMapping.containsKey(itemDisplay))
                return;
            Block block = itemDisplayToBlockMapping.get(itemDisplay);
            itemDisplayToBlockMapping.remove(itemDisplay);
            damages.remove(block);
        }

        public void sendBlockDamage(Block block, int damage) {
            if(true)
                return;
            if (!FakeBlockRegistry.hasTransparentTexture(block.getType()))
                return;
            if (damage >= 0 && damage <= 9) {
                ItemDisplay damageDisplay;
                if (!damages.containsKey(block)) {
                    Location spawnLoc = block.getLocation().clone().add(0.5, 0.5, 0.5);
                    damageDisplay = (ItemDisplay) block.getWorld()
                                                       .spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
                    Transformation transformation = damageDisplay.getTransformation();
                    transformation.getScale().set(1.05, 1.05, 1.05);
                    damageDisplay.setTransformation(transformation);
                    damageDisplay.setPersistent(false);
                    damages.put(block, damageDisplay);
                    itemDisplayToBlockMapping.put(damageDisplay, block);
                } else
                    damageDisplay = damages.get(block);

                damageDisplay.setItemStack(texturesPerDamage.get(damage).createItem());
                ;
            } else {
                cancelBlockDamage(block);
            }
        }

        public void cancelBlockDamage(Block block) {
            if(true)
                return;
            if (!damages.containsKey(block))
                return;
            damages.get(block).setItemStack(null);
            //damages.remove(block).remove();
        }
    }
}

package de.verdox.mccreativelab.behaviour;

import de.verdox.mccreativelab.CustomBehaviour;
import de.verdox.mccreativelab.worldgen.WorldGenChunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

public interface WorldGenerationBehaviour extends Behaviour {
    CustomBehaviour<WorldGenerationBehaviour> WORLD_GENERATION_BEHAVIOUR = new CustomBehaviour<>(WorldGenerationBehaviour.class, new WorldGenerationBehaviour() {}, "MCCLab - WorldGenerationBehaviour");

    /**
     * This callback method is called everytime a block is placed due to chunk generation.
     * Consequently, this method will be called asynchronously or synchronously depending on the thread that generates it.
     * We suggest to implement this method with caution!
     * @param featureType The flag indicating the feature that was generated e.g. OreFeature, TreeFeature
     * @param blockPosition The position of the block
     * @param worldGenChunk The WorldGenChunk of the block
     * @param generatedBlockData The BlockData that was generated
     */
    default void featureBlockGenerationCallback(FeatureType featureType, BlockVector blockPosition, WorldGenChunk worldGenChunk, BlockData generatedBlockData){}
    record FeatureType(NamespacedKey namespacedKey){
        public static final FeatureType TREE_FEATURE = new FeatureType(NamespacedKey.minecraft("tree_feature"));
        public static final FeatureType ORE_FEATURE = new FeatureType(NamespacedKey.minecraft("ore_feature"));
    }
}

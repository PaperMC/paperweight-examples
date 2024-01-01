package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.block.BlockStateIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

public class UnusedBlockStates {

    private static final Map<Material, BlockStateIterator<?>> cache = new HashMap<>();

    static {
        register(Material.NOTE_BLOCK, new BlockStateIterator.NoteBlockStatesIterator());
    }

    public static BlockData getUnusedBlockState(Material material) {
        return cache.get(material).next();
    }

    private static void register(Material material, BlockStateIterator<?> blockStateIterator) {

        int counter = 0;
        while (blockStateIterator.hasNext()) {
            blockStateIterator.next();
            counter++;
        }

        Bukkit.getLogger().info("Providing " + counter + " unused block states of type " + material);

        blockStateIterator.reset();
        blockStateIterator.next();
        cache.put(material, blockStateIterator);
    }

}

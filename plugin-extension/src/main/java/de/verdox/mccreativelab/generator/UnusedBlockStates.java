package de.verdox.mccreativelab.generator;

import de.verdox.mccreativelab.util.BlockStateIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.*;

public class UnusedBlockStates {
    private static final Map<Material, List<BlockData>> cache = new HashMap<>();
    private static final Set<BlockData> usedBlockStates = new HashSet<>();

    static {
        register(Material.NOTE_BLOCK, new BlockStateIterator.NoteBlockStatesIterator());
    }

    /**
     * Get to retrieve a block state that can be reused for custom blocks.
     * The blockdata is reserved after the call to prevent double usage.
     * @param material The material of the block
     * @param id The id of the block state (Depends on the BlockStateIterator that was used to register these states)
     * @return The block data that is reserved for the method caller.
     */
    public static BlockData getUnusedBlockState(Material material, int id) {
        if(!material.isBlock())
            throw new IllegalArgumentException("Please provide a valid block material");
        if(!cache.containsKey(material))
            throw new IllegalArgumentException("You cannot use block states of type "+material.getKey());

        List<BlockData> potentialUsableBlockStates = cache.get(material);
        if(id < 0 || id > potentialUsableBlockStates.size())
            throw new IllegalArgumentException("The block "+material.getKey()+" only has "+potentialUsableBlockStates.size()+" reusable block states. Please provide a valid id from 0 to "+potentialUsableBlockStates.size()+". You provided the id "+id+".");

        BlockData blockData = cache.get(material).get(id);
        if(usedBlockStates.contains(blockData))
            throw new IllegalArgumentException("The block state with the id "+id+" of the block "+material.getKey()+" is already used! Please provide a different id.");
        usedBlockStates.add(blockData);
        return blockData;
    }

    private static void register(Material material, BlockStateIterator<?> blockStateIterator) {
        final List<BlockData> blockData = new LinkedList<>();
        int counter = 0;
        while (blockStateIterator.hasNext()) {
            blockData.add(blockStateIterator.next());
            counter++;
        }

        Bukkit.getLogger().info("Providing " + counter + " unused block states of type " + material);

        blockStateIterator.reset();
        blockStateIterator.next();
        cache.put(material, blockData);
    }

}

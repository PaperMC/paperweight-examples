package de.verdox.mccreativelab.block;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.BlockBehaviour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

public class NoteBlockBehaviour implements BlockBehaviour {

    @Override
    public BehaviourResult.Object<BlockData> blockUpdate(Location location, BlockData blockData, BlockFace direction, BlockData neighbourBlockData, Location neighbourLocation) {
        return new BehaviourResult.Object<>(Bukkit.createBlockData(Material.NOTE_BLOCK), BehaviourResult.Object.Type.REPLACE_VANILLA);
    }


}

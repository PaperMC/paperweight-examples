package de.verdox.mccreativelab.behaviour.interaction;

import de.verdox.mccreativelab.InteractionResult;
import org.bukkit.inventory.ItemStack;

public record ItemStackInteraction(InteractionResult interactionResult, ItemStack stack){
}

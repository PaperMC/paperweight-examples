package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.debug.Debug;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FakeItemCommand extends Command {
    public FakeItemCommand() {
        super("fakeItem");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player))
            return false;
        if(args.length == 0)
            return false;
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("get")){
                //player.getInventory().addItem(Debug.DEBUG_ITEM.createItemStack());
            }
            else if(args[0].equalsIgnoreCase("behaviour")){
                ItemStack stack = new ItemStack(Material.BAMBOO);
                stack.setItemBehaviour(new ItemBehaviour() {
                    @Override
                    public BehaviourResult.Object<Integer> getMaxStackSize(ItemStack stack) {
                        return new BehaviourResult.Object<>(2, BehaviourResult.Object.Type.REPLACE_VANILLA);
                    }

                    @Override
                    public BehaviourResult.Bool isFireResistant(ItemStack stack) {
                        return new BehaviourResult.Bool(false, BehaviourResult.Bool.Type.REPLACE_VANILLA);
                    }
                });
                player.getInventory().addItem(stack);
            }
        }
        return false;
    }
}

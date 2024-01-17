package de.verdox.mccreativelab.debug;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends Command {


    public DebugCommand() {
        super("debug");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(args.length >= 1){
            String argument1 = args[0];

            if(argument1.equals("fakeinv")){

                if(sender instanceof Player player) {
                    ItemStack[] contents = new ItemStack[46];

                    contents[36] = new ItemStack(Material.STONE);

                    player.doInventorySynchronization(false);
                    player.sendFakeInventoryContents(contents);
                }
            }
            else if(argument1.equals("testai") && sender instanceof Player player){


                Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                villager.getBrain(Villager.class).addActivity(VillagerAI.workPackageBuilder(Villager.Profession.FARMER, 0.5f), true);
            }
        }
        return false;
    }
}

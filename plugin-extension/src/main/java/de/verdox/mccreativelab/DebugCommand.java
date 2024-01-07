package de.verdox.mccreativelab;

import com.google.common.collect.ImmutableList;
import de.verdox.mccreativelab.ai.behaviour.AIBehaviour;
import de.verdox.mccreativelab.entity.TestVillagerBehaviour;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.EntityActivity;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends Command {


    protected DebugCommand() {
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
                villager.getBrain(Villager.class).addActivity(EntityActivity.CORE, livingEntityActivityBuilder -> {
                    livingEntityActivityBuilder.withBehaviour(0, new TestVillagerBehaviour());
                });
            }
        }
        return false;
    }
}

package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.debug.vanilla.VillagerAI;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.ActiveMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugCommand extends Command {


    public DebugCommand() {
        super("debug");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length >= 1) {
            String argument1 = args[0];

            if (argument1.equals("fakeinv")) {

                if (sender instanceof Player player) {
                    ItemStack[] contents = new ItemStack[46];

                    contents[36] = new ItemStack(Material.STONE);

                    player.doInventorySynchronization(false);
                    player.sendFakeInventoryContents(contents);
                }
            } else if (argument1.equals("testai") && sender instanceof Player player) {


                Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                villager.getBrain(Villager.class)
                        .addActivity(VillagerAI.workPackageBuilder(Villager.Profession.FARMER, 0.5f), true);


                Bukkit.getScheduler().runTaskLater(MCCreativeLabExtension.getInstance(), () -> {
                    Entity breedTarget = villager.getMemory(MemoryKey.BREED_TARGET);
                    List<Entity> nearestVisibleEntities = villager.getMemory(MemoryKey.NEAREST_VISIBLE_LIVING_ENTITIES);
                    if (nearestVisibleEntities != null)
                        System.out.println(nearestVisibleEntities);
                    if (breedTarget != null)
                        System.out.println(breedTarget);
                }, 20L * 3);
            } else if (argument1.equals("menu") && sender instanceof Player player) {
                if (ActiveMenu.hasActiveMenu(player))
                    ActiveMenu.closeActiveMenu(player);
                else
                    Debug.DEBUG_MENU.createMenuForPlayer(player);
            }
        }
        return false;
    }
}

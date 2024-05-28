package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.MCCreativeLab;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.container.CustomInventory;
import de.verdox.mccreativelab.debug.vanilla.VillagerAI;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.renderer.HudRendererImpl;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.ActiveMenu;
import de.verdox.mccreativelab.world.item.data.ItemDataContainer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DebugCommand extends Command {


    public DebugCommand() {
        super("debug");
        setPermission("mccreativelab.command.debug");
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission("mccreativelab.command.debug"))
            return false;

        if (args.length >= 1) {
            String argument1 = args[0];

            if (argument1.equals("fakeinv")) {

                if (sender instanceof Player player) {
                    ItemStack[] contents = new ItemStack[46];

                    contents[36] = new ItemStack(Material.STONE);

                    player.doInventorySynchronization(false);
                    player.sendFakeInventoryContents(contents);
                }
            } 
            else if(argument1.equals("customInv")){
                if (sender instanceof Player player) {
                    MCCreativeLab.openCustomContainerMenu(new CustomInventory() {
                        @Override
                        public Slot[] getSlots() {
                            return new Slot[]{
                                new Slot() {
                                    @Override
                                    public boolean isActive() {
                                        return false;
                                    }

                                    @Override
                                    public boolean mayPlace(ItemStack stack) {
                                        return false;
                                    }

                                    @Override
                                    public boolean mayPickUp(Player player) {
                                        return false;
                                    }
                                }
                            };
                        }

                        @Override
                        public ItemStack quickMoveStack(Player player, int slot) {
                            System.out.println("QUICK MOVE STACK "+slot);
                            return new ItemStack(Material.STONE);
                        }
                    }, player, Component.empty());
                }
            }
            else if (argument1.equals("testTPAsync")) {

                if (sender instanceof Player player) {

                    Location location = new Location(
                        player.getWorld(),
                        player.getX() + ThreadLocalRandom.current().nextInt(5000, 20000),
                        player.getY(),
                        player.getZ() + ThreadLocalRandom.current().nextInt(5000, 20000)
                    );
                    player.teleportAsync(location);
                }

            } else if (argument1.equals("reload")) {
                MCCreativeLabExtension.getInstance().reloadPlugin();
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
            } else if (argument1.equals("itemload") && sender instanceof Player player) {
                ItemStack stack = player.getInventory().getItemInMainHand();
                System.out.println("Before: " + stack);
                ItemDataContainer.from(stack);
                System.out.println("After: " + stack);
            } else if (argument1.equals("herobrine") && sender instanceof Player player) {
                Location summonLocation = player.getLocation();
                //MCCreativeLabExtension.getLegacyFeatures().herobrineFeature.testSpawnHerobrineModel(summonLocation);
            } else if (argument1.equals("printhud") && sender instanceof Player player) {
                HudRendererImpl hudRenderer = (HudRendererImpl) MCCreativeLabExtension.getHudRenderer();
                hudRenderer.printLastRender(player);
            }
        }
        return false;
    }
}

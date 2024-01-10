package de.verdox.mccreativelab.item.fuel;

import de.verdox.mccreativelab.recipe.CustomItemData;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FuelSettings implements Listener {
    private final Map<CustomItemData, Integer> fuelDurations = new HashMap<>();
    private final Set<Material> vanillaFuelMaterials = new HashSet<>();

    public FuelSettings(){
        initializeVanillaDurations();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void changeFuelBurnTime(FurnaceBurnEvent e) {
        e.setBurnTime(getBurnDuration(e.getFuel()));
    }

    private int getBurnDuration(ItemStack fuel){
        return fuelDurations.getOrDefault(CustomItemData.fromItemStack(fuel), 0);
    }

    public void register(CustomItemData customItemData, int fuelDuration) {
        if (!vanillaFuelMaterials.contains(customItemData.material()))
            throw new IllegalArgumentException("Material of customItemData must be a burnable material. " + customItemData.material() + " is not burnable as fuel!");
        fuelDurations.put(customItemData, fuelDuration);
    }

    public void register(ItemStack stack, int fuelDuration) {
        register(CustomItemData.fromItemStack(stack), fuelDuration);
    }

    public void register(Material material, int fuelDuration){
        register(new CustomItemData(material, 0), fuelDuration);
    }

    public void register(Material material, int customModelData, int fuelDuration){
        register(new CustomItemData(material, customModelData), fuelDuration);
    }

    private void registerVanillaDuration(Material material, int fuelDuration) {
        fuelDurations.put(new CustomItemData(material, 0), fuelDuration);
        vanillaFuelMaterials.add(material);
    }

    private void registerVanillaDuration(Tag<Material> blockTag, int fuelDuration) {
        for (Material value : blockTag.getValues())
            registerVanillaDuration(value, fuelDuration);

    }

    private void initializeVanillaDurations() {
        registerVanillaDuration(Material.LAVA_BUCKET, 20000);
        registerVanillaDuration(Material.COAL_BLOCK, 16000);
        registerVanillaDuration(Material.BLAZE_ROD, 2400);
        registerVanillaDuration(Material.COAL, 1600);
        registerVanillaDuration(Material.CHARCOAL, 1600);
        registerVanillaDuration(Tag.LOGS, 300);
        registerVanillaDuration(Tag.BAMBOO_BLOCKS, 300);
        registerVanillaDuration(Tag.BAMBOO_PLANTABLE_ON, 300);
        registerVanillaDuration(Tag.PLANKS, 300);
        registerVanillaDuration(Material.BAMBOO_MOSAIC, 300);
        registerVanillaDuration(Tag.WOODEN_STAIRS, 300);
        registerVanillaDuration(Material.BAMBOO_MOSAIC_STAIRS, 300);
        registerVanillaDuration(Tag.WOODEN_SLABS, 150);
        registerVanillaDuration(Material.BAMBOO_MOSAIC_SLAB, 150);
        registerVanillaDuration(Tag.WOODEN_TRAPDOORS, 300);
        registerVanillaDuration(Tag.WOODEN_PRESSURE_PLATES, 300);
        registerVanillaDuration(Tag.WOODEN_FENCES, 300);
        registerVanillaDuration(Tag.FENCE_GATES, 300);
        registerVanillaDuration(Material.NOTE_BLOCK, 300);
        registerVanillaDuration(Material.BOOKSHELF, 300);
        registerVanillaDuration(Material.CHISELED_BOOKSHELF, 300);
        registerVanillaDuration(Material.LECTERN, 300);
        registerVanillaDuration(Material.JUKEBOX, 300);
        registerVanillaDuration(Material.CHEST, 300);
        registerVanillaDuration(Material.TRAPPED_CHEST, 300);
        registerVanillaDuration(Material.CRAFTING_TABLE, 300);
        registerVanillaDuration(Material.DAYLIGHT_DETECTOR, 300);
        registerVanillaDuration(Tag.BANNERS, 300);
        registerVanillaDuration(Material.BOW, 300);
        registerVanillaDuration(Material.FISHING_ROD, 300);
        registerVanillaDuration(Material.LADDER, 300);
        registerVanillaDuration(Tag.SIGNS, 200);
        registerVanillaDuration(Tag.ALL_HANGING_SIGNS, 800);
        registerVanillaDuration(Material.WOODEN_SHOVEL, 200);
        registerVanillaDuration(Material.WOODEN_SWORD, 200);
        registerVanillaDuration(Material.WOODEN_HOE, 200);
        registerVanillaDuration(Material.WOODEN_AXE, 200);
        registerVanillaDuration(Material.WOODEN_PICKAXE, 200);
        registerVanillaDuration(Tag.WOODEN_DOORS, 200);
        registerVanillaDuration(Tag.ITEMS_BOATS, 1200);
        registerVanillaDuration(Tag.ITEMS_CHEST_BOATS, 1200);
        registerVanillaDuration(Tag.WOOL, 100);
        registerVanillaDuration(Tag.WOODEN_BUTTONS, 100);
        registerVanillaDuration(Material.STICK, 100);
        registerVanillaDuration(Tag.SAPLINGS, 100);
        registerVanillaDuration(Material.BOWL, 100);
        registerVanillaDuration(Tag.WOOL_CARPETS, 67);
        registerVanillaDuration(Material.DRIED_KELP_BLOCK, 4001);
        registerVanillaDuration(Material.CROSSBOW, 300);
        registerVanillaDuration(Material.BAMBOO, 50);
        registerVanillaDuration(Material.DEAD_BUSH, 100);
        registerVanillaDuration(Material.SCAFFOLDING, 50);
        registerVanillaDuration(Material.LOOM, 300);
        registerVanillaDuration(Material.BARREL, 300);
        registerVanillaDuration(Material.CARTOGRAPHY_TABLE, 300);
        registerVanillaDuration(Material.FLETCHING_TABLE, 300);
        registerVanillaDuration(Material.SMITHING_TABLE, 300);
        registerVanillaDuration(Material.COMPOSTER, 300);
        registerVanillaDuration(Material.AZALEA, 100);
        registerVanillaDuration(Material.FLOWERING_AZALEA, 100);
        registerVanillaDuration(Material.MANGROVE_ROOTS, 300);
    }
}

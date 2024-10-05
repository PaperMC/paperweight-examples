package de.verdox.mccreativelab;

import com.google.common.base.Supplier;
import de.verdox.mccreativelab.worldgen.WorldGenData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface MCCUtil {
    MCCUtil INSTANCE = new MCCUtilImpl();

    static MCCUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a world mostly async. Most of the calculations needed to create a world are done asynchronously. However, some still occur on the main thread.
     * Those can be manually turned off when they are not needed.
     *
     * @param plugin                     The plugin to create the map for.
     * @param worldCreator               The WorldCreator
     * @param useVanillaSpawnCalculation <p>
     *                                   Whether to use vanilla spawn calculations. This is used to find a safe spawn point for players.
     *                                   <p>
     *                                   If you do not need this, the standard spawn point 0,128,0 is chosen. This also skips the generation of the bonus loot chest!
     * @return the future
     */
    java.util.concurrent.CompletableFuture<org.bukkit.World> createWorldAsync(@NotNull org.bukkit.plugin.Plugin plugin, @NotNull org.bukkit.WorldCreator worldCreator, boolean useVanillaSpawnCalculation);

    /**
     * Checks if it is raining at a specific location
     *
     * @param location The location to check
     * @return true if it is raining at the block location
     */
    boolean isRainingAt(Location location);

    /**
     * Creates a tree at the given {@link Location} if the {@link BlockData} of the {@link Block} is of type {@link org.bukkit.block.data.type.Sapling}
     *
     * @param location Location to grow the tree
     * @return true if the tree was created successfully, otherwise false
     */
    boolean growTreeIfSapling(@NotNull Location location);

    <T> CompletableFuture<T> getFromMain(Supplier<T> data);

    CompletableFuture<Void> runOnMain(Runnable runnable);

    /**
     * Returns an iterator that iterates through all possible {@link org.bukkit.block.data.BlockData} definitions of a valid block {@link org.bukkit.Material}
     *
     * @param blockMaterial - The material of the block
     * @return - the iterator
     */
    java.util.Iterator<BlockData> iterateThroughAllBlockDataVariants(org.bukkit.Material blockMaterial);

    /**
     * Returns a stream of all possible {@link org.bukkit.block.data.BlockData} definitions of a valid block {@link org.bukkit.Material}
     *
     * @param blockMaterial - The material of the block
     * @return - the stream
     */
    java.util.stream.Stream<BlockData> streamAllBlockDataVariants(org.bukkit.Material blockMaterial);

    /**
     * Creates a world mostly async. Most of the calculations needed to create a world are done asynchronously. However, some still occur on the main thread.
     * Those can be manually turned off when they are not needed.
     *
     * @param plugin The plugin to create the map for.
     * @return the future
     */
    CompletableFuture<PersistentDataContainer> readFromWorldPDCWithoutLoadingAsync(@NotNull Plugin plugin, @NotNull WorldCreator worldCreator);

    /**
     * Creates a world mostly async. Most of the calculations needed to create a world are done asynchronously. However, some still occur on the main thread.
     * Those can be manually turned off when they are not needed.
     *
     * @param plugin The plugin to create the map for.
     * @return the pdc
     */
    PersistentDataContainer readFromWorldPDCWithoutLoading(@NotNull Plugin plugin, @NotNull WorldCreator worldCreator);

    void fillBiome(Block from, Block to, Biome biomeToFill, Predicate<Biome> biomeReplacePredicate);

    void fillBiome(Block from, Block to, java.util.function.Function<@NotNull Biome, @Nullable Biome> replacer);

    void resendChunksForBiomes(List<Chunk> chunks);

    void dropExperience(Location spawnPosition, int amount, org.bukkit.entity.ExperienceOrb.SpawnReason reason, Entity trigger, @Nullable Entity source);

    void sendFakeInventoryContents(@NotNull Player player, ItemStack[] contents);

    /**
     * Opens an inventory window with the specified inventory on the top and
     * the player's inventory on the bottom.
     *
     * @param inventory The inventory to open
     * @param title     The shown title of the inventory
     * @return The newly opened inventory view
     */
    @org.jetbrains.annotations.Nullable InventoryView openInventory(@NotNull Player player, @NotNull Inventory inventory, net.kyori.adventure.text.Component title);

    WorldGenData getWorldGenData(@NotNull World world);
}

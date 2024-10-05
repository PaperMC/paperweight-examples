package de.verdox.mccreativelab;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import de.verdox.mccreativelab.worldgen.WorldGenData;
import de.verdox.mccreativelab.worldgen.impl.CraftWorldGenData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class MCCUtilImpl implements MCCUtil {
    @Override
    public CompletableFuture<World> createWorldAsync(@NotNull Plugin plugin, @NotNull WorldCreator worldCreator, boolean useVanillaSpawnCalculation) {
        return AsyncWorldCreation.createWorldAsync(plugin, worldCreator, useVanillaSpawnCalculation);
    }

    @Override
    public boolean isRainingAt(Location location) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        return craftWorld.getHandle().isRainingAt(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public boolean growTreeIfSapling(@NotNull Location location) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        var blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        var block = craftWorld.getHandle().getBlockIfLoaded(blockPos);
        if (block == null)
            return false;
        var blockState = craftWorld.getHandle().getBlockState(blockPos);
        if (block instanceof net.minecraft.world.level.block.SaplingBlock saplingBlock) {
            saplingBlock.advanceTree(craftWorld.getHandle(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), blockState, craftWorld.getHandle().random);
            return true;
        }
        return false;
    }

    @Override
    public <T> java.util.concurrent.CompletableFuture<T> getFromMain(Supplier<T> data) {
        if (Bukkit.isPrimaryThread())
            return java.util.concurrent.CompletableFuture.completedFuture(data.get());
        java.util.concurrent.CompletableFuture<T> future = new java.util.concurrent.CompletableFuture<>();
        MinecraftServer.getServer().scheduleOnMain(() -> {
            try {
                future.complete(data.get());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                future.cancel(true);
                future.obtrudeException(throwable);
            }
        });
        return future;
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> runOnMain(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();
        MinecraftServer.getServer().scheduleOnMain(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                future.cancel(true);
                future.obtrudeException(throwable);
            }
        });
        return future;
    }

    @Override
    public java.util.Iterator<org.bukkit.block.data.BlockData> iterateThroughAllBlockDataVariants(org.bukkit.Material blockMaterial) {
        if (!blockMaterial.isBlock())
            throw new IllegalArgumentException("Please provide a valid block. " + blockMaterial.getKey().asString() + " is not a valid block type.");

        net.minecraft.world.level.block.Block vanillaBlockType = org.bukkit.craftbukkit.block.data.CraftBlockData.newData(org.bukkit.craftbukkit.block.CraftBlockType.minecraftToBukkitNew(org.bukkit.craftbukkit.block.CraftBlockType.bukkitToMinecraft(blockMaterial)), null).getState().getBlock();
        java.util.Iterator<net.minecraft.world.level.block.state.BlockState> iter = vanillaBlockType.getStateDefinition().getPossibleStates().iterator();

        return new java.util.Iterator<>() {

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public org.bukkit.block.data.BlockData next() {
                return org.bukkit.craftbukkit.block.data.CraftBlockData.createData(iter.next());
            }
        };
    }

    @Override
    public java.util.stream.Stream<org.bukkit.block.data.BlockData> streamAllBlockDataVariants(org.bukkit.Material blockMaterial) {
        if (!blockMaterial.isBlock())
            throw new IllegalArgumentException("Please provide a valid block. " + blockMaterial.getKey().asString() + " is not a valid block type.");
        net.minecraft.world.level.block.Block vanillaBlockType = org.bukkit.craftbukkit.block.data.CraftBlockData.newData(org.bukkit.craftbukkit.block.CraftBlockType.minecraftToBukkitNew(org.bukkit.craftbukkit.block.CraftBlockType.bukkitToMinecraft(blockMaterial)), null).getState().getBlock();
        return vanillaBlockType.getStateDefinition().getPossibleStates().stream().map(org.bukkit.craftbukkit.block.data.CraftBlockData::createData);
    }

    @Override
    public CompletableFuture<PersistentDataContainer> readFromWorldPDCWithoutLoadingAsync(@NotNull Plugin plugin, @NotNull WorldCreator worldCreator) {
        CompletableFuture<PersistentDataContainer> future = new CompletableFuture<>();
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            try {
                PersistentDataContainer persistentDataContainer = AsyncWorldCreation.readPDCFromWorld(worldCreator);
                MinecraftServer.getServer().scheduleOnMain(() -> future.complete(persistentDataContainer));
            } catch (Throwable e) {
                future.cancel(true);
            }

        });
        return future;
    }

    @Override
    public PersistentDataContainer readFromWorldPDCWithoutLoading(@NotNull Plugin plugin, @NotNull WorldCreator worldCreator) {
        return AsyncWorldCreation.readPDCFromWorld(worldCreator);
    }

    @Override
    public void fillBiome(Block from, Block to, Biome biomeToFill, Predicate<Biome> biomeReplacePredicate) {
        ServerLevel serverLevel = ((CraftWorld) from.getWorld()).getHandle();
        net.minecraft.server.commands.FillBiomeCommand.fill(
            serverLevel,
            new BlockPos(from.getX(), from.getY(), from.getZ()),
            new BlockPos(to.getX(), to.getY(), to.getZ()),
            CraftBiome.bukkitToMinecraftHolder(biomeToFill),
            biomeHolder -> biomeReplacePredicate.test(CraftBiome.minecraftHolderToBukkit(biomeHolder)),
            componentSupplier -> {}
        );
    }

    @Override
    public void fillBiome(Block from, Block to, java.util.function.Function<@NotNull Biome, @Nullable Biome> replacer) {
        net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox = net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(new BlockPos(from.getX(), from.getY(), from.getZ()), new BlockPos(to.getX(), to.getY(), to.getZ()));
        List<ChunkAccess> list = new ArrayList<>();
        ServerLevel world = ((CraftWorld) from.getWorld()).getHandle();

        for(int k = net.minecraft.core.SectionPos.blockToSectionCoord(boundingBox.minZ()); k <= net.minecraft.core.SectionPos.blockToSectionCoord(boundingBox.maxZ()); ++k) {
            for(int l = net.minecraft.core.SectionPos.blockToSectionCoord(boundingBox.minX()); l <= net.minecraft.core.SectionPos.blockToSectionCoord(boundingBox.maxX()); ++l) {
                ChunkAccess chunkAccess = world.getChunk(l, k, ChunkStatus.FULL, false);
                if (chunkAccess == null)
                    continue;

                list.add(chunkAccess);
            }
        }

        for(ChunkAccess chunk : list) {
            net.minecraft.world.level.biome.BiomeResolver biomeResolver = (x, y, z, noise) -> {
                int blockX = net.minecraft.core.QuartPos.toBlock(x);
                int blockY = net.minecraft.core.QuartPos.toBlock(y);
                int blockZ = net.minecraft.core.QuartPos.toBlock(z);
                Holder<net.minecraft.world.level.biome.Biome> actualBiome = chunk.getNoiseBiome(x, y, z);

                if(boundingBox.isInside(blockX, blockY, blockZ)){
                    Biome biomeToReplaceWith = replacer.apply(CraftBiome.minecraftHolderToBukkit(actualBiome));
                    if(biomeToReplaceWith != null)
                        return CraftBiome.bukkitToMinecraftHolder(biomeToReplaceWith);
                }

                return actualBiome;
            };
            chunk.fillBiomesFromNoise(biomeResolver, world.getChunkSource().randomState().sampler());
            chunk.setUnsaved(true);
        }

        world.getChunkSource().chunkMap.resendBiomesForChunks(list);
    }

    @Override
    public void resendChunksForBiomes(List<Chunk> chunks) {
        Map<ServerPlayer, List<LevelChunk>> resendInstruction = new HashMap<>();
        chunks.stream()
            .filter(Chunk::isLoaded)
            .map(chunk -> ((CraftChunk) chunk).getHandle(ChunkStatus.FULL))
            .filter(chunkAccess -> chunkAccess instanceof net.minecraft.world.level.chunk.LevelChunk)
            .map(chunkAccess -> ((net.minecraft.world.level.chunk.LevelChunk) chunkAccess))
            .forEach(levelChunk -> {
                List<ServerPlayer> players = levelChunk.level.chunkSource.chunkMap.getPlayers(levelChunk.getPos(), false);
                players.forEach(serverPlayer -> resendInstruction
                    .computeIfAbsent(serverPlayer, serverPlayer1 -> new ArrayList<>()).add(levelChunk));
            });

        resendInstruction.forEach((serverPlayer, levelChunks) -> {
            serverPlayer.connection.send(net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket.forChunks(levelChunks));
        });
    }

    @Override
    public void dropExperience(Location spawnPosition, int amount, org.bukkit.entity.ExperienceOrb.SpawnReason reason, Entity trigger, @Nullable Entity source) {
        net.minecraft.world.entity.ExperienceOrb.award(((CraftWorld)spawnPosition.getWorld()).getHandle(), new Vec3(spawnPosition.getBlockX(), spawnPosition.getBlockY(), spawnPosition.getBlockZ()), amount, reason, trigger == null ? null : ((CraftEntity) trigger).getHandle(), source == null ? null : ((CraftEntity) source).getHandle());
    }

    @Override
    public void sendFakeInventoryContents(@NotNull Player player, ItemStack[] contents) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> stacks = net.minecraft.core.NonNullList.create();
        for (int i = 0; i < contents.length; i++) {
            ItemStack bukkitStack = contents[i];
            if(bukkitStack != null)
                stacks.add(i, CraftItemStack.asNMSCopy(bukkitStack));
            else
                stacks.add(i, net.minecraft.world.item.ItemStack.EMPTY);
        }
        craftPlayer.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket(craftPlayer.getHandle().containerMenu.containerId, craftPlayer.getHandle().containerMenu.getStateId(), stacks, craftPlayer.getHandle().containerMenu.getCarried().copy()));
    }

    @Override
    public @org.jetbrains.annotations.Nullable InventoryView openInventory(@NotNull Player player, @org.jetbrains.annotations.NotNull Inventory inventory, net.kyori.adventure.text.Component title) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        if (craftPlayer.getHandle() == null) return null;
        ServerPlayer serverPlayer = craftPlayer.getHandle();
        AbstractContainerMenu formerContainer = serverPlayer.containerMenu;

        MenuProvider menuProvider = null;
        switch (inventory) {
            case CraftInventoryDoubleChest itemStacks -> menuProvider = itemStacks.tile;
            case CraftInventoryLectern itemStacks -> menuProvider = itemStacks.tile;
            case CraftInventory craft -> {
                if (craft.getInventory() instanceof MenuProvider) {
                    menuProvider = (MenuProvider) craft.getInventory();
                }
            }
            default -> {
            }
        }

        if (menuProvider != null) {
            if (menuProvider instanceof BlockEntity) {
                BlockEntity te = (BlockEntity) menuProvider;
                if (!te.hasLevel()) {
                    te.setLevel(serverPlayer.level());
                }
            }
        }

        MenuType<?> container = CraftContainer.getNotchInventoryType(inventory);
        if (menuProvider != null) {
            serverPlayer.openMenu(menuProvider);
        } else {
            openCustomInventory(inventory, serverPlayer, container, title);
        }

        if (serverPlayer.containerMenu == formerContainer) {
            return null;
        }
        serverPlayer.containerMenu.checkReachable = false;
        return serverPlayer.containerMenu.getBukkitView();
    }

    @Override
    public WorldGenData getWorldGenData(@NotNull World world) {
        return new CraftWorldGenData(((CraftWorld) world).getHandle());
    }

    private static void openCustomInventory(Inventory inventory, ServerPlayer player, MenuType<?> windowType, net.kyori.adventure.text.Component title) {
        Preconditions.checkArgument(windowType != null, "Unknown windowType");
        AbstractContainerMenu container = new CraftContainer(inventory, player, player.nextContainerCounter());

        container = CraftEventFactory.callInventoryOpenEvent(player, container);
        if (container == null) return;

        //player.connection.send(new ClientboundOpenScreenPacket(container.containerId, windowType, CraftChatMessage.fromString(title)[0])); // Paper - comment
        if (!player.isImmobile())
            player.connection.send(new ClientboundOpenScreenPacket(container.containerId, windowType, io.papermc.paper.adventure.PaperAdventure.asVanilla(title))); // Paper
        player.containerMenu = container;
        player.initMenu(container);
    }
}

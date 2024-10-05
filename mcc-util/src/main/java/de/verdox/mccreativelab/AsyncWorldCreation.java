package de.verdox.mccreativelab;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class AsyncWorldCreation {
    public static CompletableFuture<World> createWorldAsync(@NotNull Plugin plugin, @NotNull WorldCreator creator, boolean useVanillaSpawnCalculation) {
        //AsyncCatcher.catchOp("createWorldAsync must be triggered synchronously");
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");
        Preconditions.checkState(MinecraftServer.getServer().getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");

        CompletableFuture<World> future = new CompletableFuture<>();
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        String name = creator.name();

        World world = craftServer.getWorld(name);

        // Paper start
        World worldByKey = craftServer.getWorld(creator.key());

        if (world != null || worldByKey != null) {
            if (world == worldByKey) {
                future.complete(world);
                return future;
            }
            throw new IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists");
        }
        // Paper end

        final ChunkGenerator generator = creator.generator() != null ? creator.generator() : craftServer.getGenerator(name);
        final BiomeProvider biomeProvider = creator.biomeProvider() != null ? creator.biomeProvider() : craftServer.getBiomeProvider(name);

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        File folder = new File(craftServer.getWorldContainer(), name);
        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
        }

        WorldLoader.DataLoadContext worldLoader = MinecraftServer.getServer().worldLoader;

        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            try {
                // First we get the level storage access
                LevelStorageSource.LevelStorageAccess worldSession;
                try {
                    worldSession = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
                } catch (IOException | ContentValidationException ex) {
                    throw new RuntimeException(ex);
                }

                // Check if there is old world data there that needs to be converted to newer version
                Dynamic<?> dynamic = loadOldWorldIfAvailable(worldSession);

                // Load Level Data and Registry
                RegistryAccess.Frozen dimensionRegistryAccess = worldLoader.datapackDimensions();
                Pair<PrimaryLevelData, Registry<LevelStem>> result = loadLevelDataAndRegistry(craftServer, creator, worldLoader, dynamic, dimensionRegistryAccess);
                PrimaryLevelData worlddata = result.getFirst();
                net.minecraft.core.Registry<LevelStem> levelStemRegistry = result.getSecond();

                // Create ServerLevel
                ServerLevel internal = createServerLevel(creator, worlddata, levelStemRegistry, name, worldSession, dimensionRegistryAccess, actualDimension, craftServer, biomeProvider, generator);

                if (MCCUtil.getInstance().getFromMain(() -> Bukkit.getWorld(name.toLowerCase(java.util.Locale.ENGLISH))).join() == null) {
                    future.complete(null);
                    return;
                }

                initWorldAsync(worlddata, internal, craftServer, useVanillaSpawnCalculation);

                MCCUtil.getInstance().runOnMain(() -> {
                    MinecraftServer.getServer().prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);
                    Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
                }).join();
                MCCUtil.getInstance().runOnMain(() -> future.complete(internal.getWorld()));
            } catch (Throwable e) {
                Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "Error while creating world async ", e);
                e.printStackTrace();
                future.cancel(true);
            }
        });
        return future;
    }

    public static PersistentDataContainer readPDCFromWorld(WorldCreator worldCreator) {
        WorldLoader.DataLoadContext worldLoader = MinecraftServer.getServer().worldLoader;
        RegistryAccess.Frozen dimensionRegistryAccess = worldLoader.datapackDimensions();
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        net.minecraft.core.Registry<LevelStem> levelStemRegistry = dimensionRegistryAccess.registryOrThrow(Registries.LEVEL_STEM);

        ResourceKey<LevelStem> actualDimension = switch (worldCreator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + worldCreator.environment() + ")");
        };

        // First we get the level storage access
        try {
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath());
            try (LevelStorageSource.LevelStorageAccess worldSession = levelStorageSource.validateAndCreateAccess(worldCreator.name(), actualDimension)) {
                // Check if there is old world data there that needs to be converted to newer version
                Dynamic<?> dynamic = loadOldWorldIfAvailable(worldSession);

                // Load Level Data and Registry
                if (dynamic == null)
                    return null;

                LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldLoader.dataConfiguration(), levelStemRegistry, worldLoader.datapackWorldgen());
                PrimaryLevelData worldData = (PrimaryLevelData) leveldataanddimensions.worldData();

                final CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(new CraftPersistentDataTypeRegistry());

                if (getWorldDataTag(worldData) instanceof CompoundTag compoundTag)
                    persistentDataContainer.putAll(compoundTag);
                return persistentDataContainer;
            }
        } catch (IOException | ContentValidationException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static void savePDCToWorldContainer(NamespacedKey worldKey, World.Environment environment, PersistentDataContainer persistentDataContainer){
/*
        WorldLoader.DataLoadContext worldLoader = MinecraftServer.getServer().worldLoader;
        RegistryAccess.Frozen dimensionRegistryAccess = worldLoader.datapackDimensions();
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        net.minecraft.core.Registry<LevelStem> levelStemRegistry = dimensionRegistryAccess.registryOrThrow(Registries.LEVEL_STEM);

        ResourceKey<LevelStem> actualDimension = switch (environment) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + environment + ")");
        };

        try {
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath());
            LevelStorageSource.LevelStorageAccess worldSession = levelStorageSource.validateAndCreateAccess(worldKey.getKey(), actualDimension);

            Dynamic<?> dynamic = loadOldWorldIfAvailable(worldSession);

            if(dynamic == null)
                return;

            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldLoader.dataConfiguration(), levelStemRegistry, worldLoader.datapackWorldgen());
            PrimaryLevelData worldData = (PrimaryLevelData) leveldataanddimensions.worldData();



            if (worldData.pdc instanceof CompoundTag)
                persistentDataContainer.putAll((CompoundTag) worldData.pdc);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }*/
    }

    private static void initWorldAsync(PrimaryLevelData worlddata, ServerLevel internal, CraftServer craftServer, boolean useVanillaSpawnCalculation) {
        boolean isDebugWorld = worlddata.isDebugWorld();

        MCCUtil.getInstance().runOnMain(() -> {
            MinecraftServer.getServer().addLevel(internal);
            internal.setSpawnSettings(true, true);

            if (internal.generator != null)
                internal.getWorld().getPopulators().addAll(internal.generator.getDefaultPopulators(internal.getWorld()));

            WorldBorder worldborder = internal.getWorldBorder();
            worldborder.applySettings(worlddata.getWorldBorder()); // CraftBukkit - move up so that WorldBorder is set during WorldInitEvent
            craftServer.getPluginManager().callEvent(new WorldInitEvent(internal.getWorld())); // CraftBukkit - SPIGOT-5569: Call WorldInitEvent before any chunks are generated

            if (!worlddata.isInitialized()) {
                try {
                    if (useVanillaSpawnCalculation)
                        setInitialSpawn(internal, worlddata, worlddata.worldGenOptions().generateBonusChest(), isDebugWorld);
                    else
                        worlddata.setSpawn(BlockPos.ZERO.above(128), 0f);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                    try {
                        internal.fillReportDetails(crashreport);
                    } catch (Throwable ignored) {
                    }
                    throw new ReportedException(crashreport);
                } finally {
                    worlddata.setInitialized(true);
                }
            }
        }).join();
    }

    @NotNull
    private static ServerLevel createServerLevel(@NotNull WorldCreator creator, PrimaryLevelData worlddata, Registry<LevelStem> levelStemRegistry, String name, LevelStorageSource.LevelStorageAccess worldSession, RegistryAccess.Frozen dimensionRegistryAccess, ResourceKey<LevelStem> actualDimension, CraftServer craftServer, BiomeProvider biomeProvider, ChunkGenerator generator) {
        worlddata.customDimensions = levelStemRegistry;
        worlddata.checkName(name);
        worlddata.setModdedInfo(MinecraftServer.getServer().getServerModName(), MinecraftServer.getServer().getModdedStatus().shouldReportAsModified());

        if (MinecraftServer.getServer().options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(worldSession, DataFixers.getDataFixer(), MinecraftServer.getServer().options.has("eraseCache"), () -> true, dimensionRegistryAccess, MinecraftServer.getServer().options.has("recreateRegionFiles"));
        }

        long j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed()); // Paper - use world seed
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        LevelStem levelStem = levelStemRegistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), levelStem.type().value(), levelStem.generator(), craftServer.getHandle().getServer().registryAccess());

        BiomeProvider providerForLevel;
        if (biomeProvider == null && generator != null)
            providerForLevel = generator.getDefaultBiomeProvider(worldInfo);
        else providerForLevel = biomeProvider;


        ResourceKey<Level> worldKey = createLevelKey(creator, craftServer, name);

        // If set to not keep spawn in memory (changed from default) then adjust rule accordingly
        if (creator.keepSpawnLoaded() == net.kyori.adventure.util.TriState.FALSE) { // Paper
            worlddata.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }
        ServerLevel internal = MCCUtil.getInstance().getFromMain(() -> new ServerLevel(MinecraftServer.getServer(), MinecraftServer.getServer().executor, worldSession, worlddata, worldKey, levelStem, craftServer.getServer().progressListenerFactory.create(worlddata.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
            worlddata.isDebugWorld(), j, creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(), true, MinecraftServer.getServer().overworld().getRandomSequences(), creator.environment(), generator, providerForLevel)).join();
        return internal;
    }

    @NotNull
    private static ResourceKey<Level> createLevelKey(@NotNull WorldCreator creator, CraftServer craftServer, String name) {
        ResourceKey<Level> worldKey;
        String levelName = craftServer.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            worldKey = Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            worldKey = Level.END;
        } else {

            worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(creator.key().getNamespace().toLowerCase(Locale.ENGLISH), creator.key().getKey().toLowerCase(Locale.ENGLISH))); // Paper
        }
        return worldKey;
    }

    private static Pair<PrimaryLevelData, net.minecraft.core.Registry<LevelStem>> loadLevelDataAndRegistry(CraftServer craftServer, WorldCreator creator, WorldLoader.DataLoadContext worldLoader, Dynamic<?> dynamic, RegistryAccess.Frozen dimensionRegistryAccess) {
        net.minecraft.core.Registry<LevelStem> levelStemRegistry = dimensionRegistryAccess.registryOrThrow(Registries.LEVEL_STEM);

        PrimaryLevelData worlddata;
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldLoader.dataConfiguration(), levelStemRegistry, worldLoader.datapackWorldgen());

            worlddata = (PrimaryLevelData) leveldataanddimensions.worldData();
            dimensionRegistryAccess = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
        } else {
            LevelSettings worldsettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worlddimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            worldsettings = new LevelSettings(creator.name(), GameType.byId(craftServer.getDefaultGameMode().getValue()), creator.hardcore(), Difficulty.EASY, false, new GameRules(), worldLoader.dataConfiguration());
            worlddimensions = properties.create(worldLoader.datapackWorldgen());

            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(levelStemRegistry);
            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldLoader.datapackWorldgen().allRegistriesLifecycle());

            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
            dimensionRegistryAccess = worlddimensions_b.dimensionsRegistryAccess();
        }

        return Pair.of(worlddata, dimensionRegistryAccess.registryOrThrow(Registries.LEVEL_STEM));
    }

    private static Dynamic<?> loadOldWorldIfAvailable(LevelStorageSource.LevelStorageAccess worldSession) {
        Dynamic<?> dynamic;
        if (worldSession.hasWorldData()) {
            net.minecraft.world.level.storage.LevelSummary worldinfo;

            try {
                dynamic = worldSession.getDataTag();
                worldinfo = worldSession.getSummary(dynamic);
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                LevelStorageSource.LevelDirectory convertable_b = worldSession.getLevelDirectory();

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
                MinecraftServer.LOGGER.info("Attempting to use fallback");

                try {
                    dynamic = worldSession.getDataTagFallback();
                    worldinfo = worldSession.getSummary(dynamic);
                } catch (NbtException | ReportedNbtException | IOException ioexception1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
                    return null;
                }

                worldSession.restoreLevelDataFromOld();
            }

            if (worldinfo.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return null;
            }

            if (!worldinfo.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return null;
            }
        } else {
            dynamic = null;
        }
        return dynamic;
    }

    private static void setInitialSpawn(ServerLevel world, ServerLevelData worldProperties, boolean bonusChest, boolean debugWorld) {
        if (debugWorld) {
            worldProperties.setSpawn(BlockPos.ZERO.above(80), 0.0F);
        } else {
            ServerChunkCache chunkproviderserver = world.getChunkSource();
            ChunkPos chunkcoordintpair = new ChunkPos(chunkproviderserver.randomState().sampler().findSpawnPosition());
            // CraftBukkit start
            if (world.generator != null) {
                Random rand = new Random(world.getSeed());
                org.bukkit.Location spawn = world.generator.getFixedSpawnLocation(world.getWorld(), rand);

                if (spawn != null) {
                    if (spawn.getWorld() != world.getWorld()) {
                        throw new IllegalStateException("Cannot set spawn point for " + worldProperties.getLevelName() + " to be in another world (" + spawn.getWorld().getName() + ")");
                    } else {
                        worldProperties.setSpawn(new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()), spawn.getYaw());
                        return;
                    }
                }
            }
            // CraftBukkit end
            int i = chunkproviderserver.getGenerator().getSpawnHeight(world);

            if (i < world.getMinBuildHeight()) {
                BlockPos blockposition = chunkcoordintpair.getWorldPosition();

                i = world.getHeight(Heightmap.Types.WORLD_SURFACE, blockposition.getX() + 8, blockposition.getZ() + 8);
            }

            worldProperties.setSpawn(chunkcoordintpair.getWorldPosition().offset(8, i, 8), 0.0F);
            int j = 0;
            int k = 0;
            int l = 0;
            int i1 = -1;

            for (int j1 = 0; j1 < Mth.square(11); ++j1) {
                if (j >= -5 && j <= 5 && k >= -5 && k <= 5) {
                    BlockPos blockposition1 = PlayerRespawnLogic.getSpawnPosInChunk(world, new ChunkPos(chunkcoordintpair.x + j, chunkcoordintpair.z + k));

                    if (blockposition1 != null) {
                        worldProperties.setSpawn(blockposition1, 0.0F);
                        break;
                    }
                }

                if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
                    int k1 = l;

                    l = -i1;
                    i1 = k1;
                }

                j += l;
                k += i1;
            }

            if (bonusChest) {
                world.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((iregistry) -> {
                    return iregistry.getHolder(MiscOverworldFeatures.BONUS_CHEST);
                }).ifPresent((holder_c) -> {
                    ((ConfiguredFeature) holder_c.value()).place(world, chunkproviderserver.getGenerator(), world.random, worldProperties.getSpawnPos());
                });
            }

        }
    }

    private static Tag getWorldDataTag(PrimaryLevelData primaryLevelData) {
        //return primaryLevelData.pdc;
        return null;
    }
}

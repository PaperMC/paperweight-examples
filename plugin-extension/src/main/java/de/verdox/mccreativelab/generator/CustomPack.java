package de.verdox.mccreativelab.generator;

import org.bukkit.Bukkit;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class CustomPack<C extends CustomPack<C>> {
    protected final String packName;
    protected final int packFormat;
    protected final String description;
    protected final AssetPath savePath;
    protected final AssetPath pathToSavePackDataTo;
    private final List<Resource<C>> addedResources = new LinkedList<>();
    private final Set<Resource<C>> distinctResources = new HashSet<>();
    private boolean wasModified = false;
    private boolean noNewInstallations;
    private boolean isReloading;
    private ConfigurableResourceStorage<C> configurableResourceStorage = new ConfigurableResourceStorage<>();

    public CustomPack(String packName, int packFormat, String description, AssetPath savePath) {
        this.packName = packName;
        this.packFormat = packFormat;
        this.description = description;
        this.savePath = savePath;
        this.pathToSavePackDataTo = AssetPath.buildPath(packName).withNewParentPath(savePath);
    }

    public void clearResources() {
        addedResources.clear();
        distinctResources.clear();
        this.wasModified = false;
    }

    public File installPack(boolean reload) throws IOException {
        isReloading = reload;
        try{
            Bukkit.getLogger().info("Deleting folder " + pathToSavePackDataTo.toPath().toFile());
            FileUtils.deleteDirectory(pathToSavePackDataTo.toPath().toFile());
            createDescriptionFile();
            includeThirdPartyFiles();

            // We use a copy of the resource set to allow potential registrations through these resources as well
            for (Resource<C> cResource : Set.copyOf(addedResources))
                cResource.beforeResourceInstallation((C) this);

            reloadResourcesFromConfigs();

            noNewInstallations = true;
            for (Resource<C> cResource : addedResources)
                cResource.installResourceToPack((C) this);

            for (Resource<C> cResource : addedResources)
                cResource.afterResourceInstallation((C) this);
        }
        finally {
            noNewInstallations = false;
            isReloading = false;
        }


        return pathToSavePackDataTo.toPath().toFile();
    }

    private void reloadResourcesFromConfigs() throws IOException {
        for (Resource<C> addedResource : addedResources) {
            if (addedResource instanceof ConfigurableResource<C> configurableResource)
                configurableResourceStorage.loadResourceFromStorage(configurableResource);
        }
    }

    public boolean wasModified() {
        return wasModified;
    }

    public final void register(Resource<C> resource) {
        if (noNewInstallations)
            throw new IllegalStateException("Do not use customPack.register in installResourceToPack method!");
        if(isReloading) // If we only trigger a pack reload we won't register new resources
            return;
        Objects.requireNonNull(resource);
        wasModified = true;
        addedResources.add(resource);
        distinctResources.add(resource);
        resource.onRegister((C) this);
        onRegister(resource);
    }

    public final void registerIfNotAlready(Resource<C> resource){
        if(distinctResources.contains(resource))
            return;
        register(resource);
    }

    protected void onRegister(Resource<C> resource) {
    }

    public final void registerNullable(@Nullable Resource<C> resource) {
        if (resource == null)
            return;
        register(resource);
    }

    protected abstract void createDescriptionFile() throws IOException;

    protected void includeThirdPartyFiles() {
    }

    public abstract String mainFolder();

    public String getPackName() {
        return packName;
    }

    public int getPackFormat() {
        return packFormat;
    }

    public String getDescription() {
        return description;
    }

    public AssetPath getSavePath() {
        return savePath;
    }

    public AssetPath getPathToSavePackDataTo() {
        return pathToSavePackDataTo;
    }
}

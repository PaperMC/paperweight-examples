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
    private boolean wasModified = false;
    private boolean noNewInstallations;

    public CustomPack(String packName, int packFormat, String description, AssetPath savePath) {
        this.packName = packName;
        this.packFormat = packFormat;
        this.description = description;
        this.savePath = savePath;
        this.pathToSavePackDataTo = AssetPath.buildPath(packName).withNewParentPath(savePath);
    }

    public void clearResources() {
        addedResources.clear();
        this.wasModified = false;
    }

    public File installPack() throws IOException {

        FileUtils.deleteDirectory(pathToSavePackDataTo.toPath().toFile());
        createDescriptionFile();
        includeThirdPartyFiles();

        // We use a copy of the resource set to allow potential registrations through these resources as well
        for (Resource<C> cResource : Set.copyOf(addedResources))
            cResource.beforeResourceInstallation((C) this);

        noNewInstallations = true;
        for (Resource<C> cResource : addedResources)
            cResource.installResourceToPack((C) this);

        for (Resource<C> cResource : addedResources)
            cResource.afterResourceInstallation((C) this);
        noNewInstallations = false;
        return pathToSavePackDataTo.toPath().toFile();
    }

    public boolean wasModified() {
        return wasModified;
    }

    public void register(Resource<C> resource) {
        if (noNewInstallations)
            throw new IllegalStateException("Do not use customPack.register in installResourceToPack method!");
        Objects.requireNonNull(resource);
        wasModified = true;
        addedResources.add(resource);
        resource.onRegister((C) this);
        Bukkit.getLogger().info("Registering resource " + resource.key());
    }

    public final void registerNullable(@Nullable Resource<C> resource) {
        if (resource == null)
            return;
        register(resource);
    }

    protected abstract void createDescriptionFile();

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

package de.verdox.mccreativelab.generator;

import org.bukkit.Bukkit;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class CustomPack<C extends CustomPack<C>> {
    protected final String packName;
    protected final int packFormat;
    protected final String description;
    protected final AssetPath savePath;
    protected final AssetPath pathToSavePackDataTo;
    private final Set<Resource<C>> resourceSet = new HashSet<>();
    private boolean wasModified = false;

    public CustomPack(String packName, int packFormat, String description, AssetPath savePath){
        this.packName = packName;
        this.packFormat = packFormat;
        this.description = description;
        this.savePath = savePath;
        this.pathToSavePackDataTo = AssetPath.buildPath(packName).withNewParentPath(savePath);
    }
    public void clearResources(){
        resourceSet.clear();
    }
    public File installPack(){
        try {
            FileUtils.deleteDirectory(pathToSavePackDataTo.toPath().toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        createDescriptionFile();
        includeThirdPartyFiles();

        for (Resource<C> cResource : resourceSet) {
            try {
                //Bukkit.getLogger().info("Installing "+cResource.getClass().getSimpleName()+" resource "+cResource.key());
                cResource.installToDataPack((C) this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return pathToSavePackDataTo.toPath().toFile();
    }

    public boolean wasModified() {
        return wasModified;
    }

    public void register(Resource<C> resource){
        wasModified = true;
        resourceSet.add(resource);
    }
    protected abstract void createDescriptionFile();
    protected void includeThirdPartyFiles(){}

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

package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.AssetType;

public class ResourcePackAssetTypes {
    /**
     * Used as path for the sounds.json file of a namespace
     */
    public static final AssetType<CustomResourcePack> SOUND_FILE = new AssetType<>(AssetPath.buildPath(""));
    /**
     * Used to define BlockState Textures
     */
    public static final AssetType<CustomResourcePack> BLOCK_STATES = new AssetType<>(AssetPath.buildPath("blockstates"));
    /**
     * Used to define fonts
     */
    public static final AssetType<CustomResourcePack> FONT = new AssetType<>(AssetPath.buildPath("font"));
    /**
     * Used to define languages
     */
    public static final AssetType<CustomResourcePack> LANG = new AssetType<>(AssetPath.buildPath("lang"));
    /**
     * Used to define block and item models
     */
    public static final AssetType<CustomResourcePack> MODELS = new AssetType<>(AssetPath.buildPath("models"));
    /**
     * Used to store sound files
     */
    public static final AssetType<CustomResourcePack> SOUNDS = new AssetType<>(AssetPath.buildPath("sounds"));
    /**
     * Used to manipulate minecraft internal shaders
     * By now you can't add your own shaders
     */
    public static final AssetType<CustomResourcePack> SHADERS = new AssetType<>(AssetPath.buildPath("shaders"));
    /**
     * Contains end.txt and splahes.txt
     */
    public static final AssetType<CustomResourcePack> TEXTS = new AssetType<>(AssetPath.buildPath("texts"));
    /**
     * Contains all textures
     */
    public static final AssetType<CustomResourcePack> TEXTURES = new AssetType<>(AssetPath.buildPath("textures"));
    /**
     * A global sound file for this namespace that defines all sounds included in the pack
     */
    public static final AssetType<CustomResourcePack> NAMESPACE_SOUND_FILE = new AssetType<>(AssetPath.buildPath("sounds.json"));
}

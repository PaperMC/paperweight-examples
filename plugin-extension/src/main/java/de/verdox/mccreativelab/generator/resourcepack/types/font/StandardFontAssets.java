package de.verdox.mccreativelab.generator.resourcepack.types.font;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import org.bukkit.NamespacedKey;

public class StandardFontAssets {
    public static final Asset<CustomResourcePack> nonLatinStandard = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/font/nonlatin_european.png"));
    public static final AssetBasedResourcePackResource ascii = new AssetBasedResourcePackResource(new NamespacedKey("mccreativelab","hud/standard_ascii"), new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/font/ascii.png")), ResourcePackAssetTypes.TEXTURES, "png");
    public static final AssetBasedResourcePackResource accented = new AssetBasedResourcePackResource(new NamespacedKey("mccreativelab","hud/standard_accented"), new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/font/accented.png")), ResourcePackAssetTypes.TEXTURES, "png");
    public static final BitMap asciiBitmap = new BitMap(ascii,8, 0, Characters.ASCII);
    public static final BitMap accentedBitMap = new BitMap(accented, 12, 0, Characters.ACCENTED);
}

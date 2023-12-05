package de.verdox.mccreativelab.generator.resourcepack.types.font;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;

public class StandardFontAssets {
    public static final Asset<CustomResourcePack> nonLatinStandard = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/font/nonlatin_european.png"));
    public static final Asset<CustomResourcePack> ascii = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/font/ascii.png"));
    public static final Asset<CustomResourcePack> accented = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/font/accented.png"));
    public static final BitMap asciiBitmap = new BitMap(ascii,8, 0, Characters.ASCII);
    public static final BitMap accentedBitMap = new BitMap(accented, 12, 0, Characters.ACCENTED);
}

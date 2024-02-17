package de.verdox.mccreativelab.generator.resourcepack.types.font;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.BitMapReader;
import org.bukkit.NamespacedKey;

import javax.imageio.ImageIO;
import java.io.IOException;

public record BitMap(AssetBasedResourcePackResource bitmapImageAsset, int height, int ascent, float scale, BitMapReader bitMapReader, String... character) implements FontElement{

    public BitMap(AssetBasedResourcePackResource bitmapImageAsset, int height, int ascent, String... character) {
        this(bitmapImageAsset, height, ascent, 1, character);
    }

    public BitMap(AssetBasedResourcePackResource bitmapImageAsset, int height, int ascent, float scale, String... character) {
        this(bitmapImageAsset, height, ascent, scale, new BitMapReader(bitmapImageAsset.getAsset().assetInputStream(), height, character, scale), character);
    }

    public BitMap(AssetBasedResourcePackResource bitmapImageAsset, int height, int ascent, BitMapReader bitMapReader, String... character) {
        this(bitmapImageAsset, height, ascent, 1, bitMapReader, character);
    }

    public BitMap withHeight(int height) {
        return new BitMap(bitmapImageAsset, height, ascent, character);
    }
    public BitMap withAscent(int ascent) {
        return new BitMap(bitmapImageAsset, height, ascent, bitMapReader, character);
    }
    public BitMap withScale(float scale) {
        return new BitMap(bitmapImageAsset, height, ascent, scale, character);
    }
    public int getPixelHeight() throws IOException {
        if (!bitmapImageAsset.getAsset().isInputStreamValid())
            throw new IOException("Asset is not a bitmap image");
        return ImageIO.read(bitmapImageAsset.getAsset().assetInputStream().get()).getHeight();
    }
    public int getPixelWidth() throws IOException {
        if (!bitmapImageAsset.getAsset().isInputStreamValid())
            throw new IOException("Asset is not a bitmap image");
        return ImageIO.read(bitmapImageAsset.getAsset().assetInputStream().get()).getWidth();
    }

    @Override
    public void buildToProviders(NamespacedKey namespacedKey, JsonArrayBuilder providers) {
        if(character.length == 0)
            return;

        var charArray = JsonArrayBuilder.create();

        for (String s : character) {
            charArray.add(s);
        }


        var bitmapDetails = JsonObjectBuilder.create()
                                             .add("type", "bitmap")
                                             .add("file", bitmapImageAsset.getKey() +".png")
                                             .add("ascent", ascent)
                                             .add("height", (int) (height * scale))
                                             .add("chars", charArray);

        providers.add(bitmapDetails);
    }
}

package de.verdox.mccreativelab.generator.resourcepack.types.font;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.AssetUtil;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Font extends ResourcePackResource {
    private final List<Space> spaces = new LinkedList<>();
    private final List<BitMap> bitMaps = new LinkedList<>();

    public Font(NamespacedKey namespacedKey) {
        super(namespacedKey);
    }

    public int getPixelWidth(String text) {
        int width = 0;

        var found = false;
        for (int codePoint : text.codePoints().toArray()) {
            for (Space space : spaces) {
                if (found)
                    break;
                if (space.hasCodePoint(codePoint)) {
                    width += space.getPixelWidth(codePoint);
                    found = true;
                }
            }

            for (BitMap bitMap : bitMaps) {
                if (found)
                    break;
                if (bitMap.bitMapReader().hasCodePoint(codePoint)) {
                    found = true;
                    width += bitMap.bitMapReader().getPixelWidth(codePoint, 0) + 1;
                }

            }
            found = false;
        }
        return width;
    }

    public void addSpace(Space space) {
        this.spaces.add(space);
    }

    public BitMap addBitMap(BitMap bitMap) {
        this.bitMaps.add(bitMap);
        return bitMap;
    }

    public List<BitMap> getBitMaps() {
        return bitMaps;
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        JsonArrayBuilder providers = JsonArrayBuilder.create();

        for (int i = 0; i < bitMaps.size(); i++) {
            NamespacedKey bitMapKey = new NamespacedKey(key().namespace(), key().value() + "_bitmap_" + i);
            BitMap bitMap = bitMaps.get(i);
            bitMap.bitmapImageAsset().installAsset(customPack, bitMapKey, ResourcePackAssetTypes.TEXTURES, "png");
            bitMap.buildToProviders(bitMapKey, providers);
        }

        spaces.forEach(space -> space.buildToProviders(null, providers));

        JsonObject jsonObject = JsonObjectBuilder.create().add("providers", providers).build();
        AssetUtil.createJsonAssetAndInstall(jsonObject, customPack, key(), ResourcePackAssetTypes.FONT);
    }
}

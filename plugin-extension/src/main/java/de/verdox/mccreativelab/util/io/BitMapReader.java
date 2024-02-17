package de.verdox.mccreativelab.util.io;

import de.verdox.mccreativelab.generator.resourcepack.types.font.BitMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BitMapReader {
    private final BufferedImage bufferedImage;
    private final int heightOfChar;
    private final String[] charArray;
    private final float scale;
    private final Map<Integer, CharacterDimensions> characterDimensions;

    public BitMapReader(BufferedImage bufferedImage, BitMap bitMap){
        this(bufferedImage, bitMap.height(), bitMap.character(), bitMap.scale());
    }
    public BitMapReader(BufferedImage bufferedImage, int heightOfChar, String[] charArray, float scale) {
        this.bufferedImage = bufferedImage;
        this.heightOfChar = heightOfChar;
        this.charArray = charArray;
        this.scale = scale;
        this.characterDimensions = calculateDimensions();
    }

    public BitMapReader(Supplier<InputStream> supplier, BitMap bitMap){
        this(supplier, bitMap.height(), bitMap.character(), bitMap.scale());
    }

    public BitMapReader(Supplier<InputStream> supplier, int heightOfChar, String[] charArray, float scale) {

        try (InputStream stream = supplier.get()) {
            if (stream == null) {
                throw new RuntimeException("Could not find file in stream for BitMapReader");
            }
            this.bufferedImage = ImageIO.read(stream);
            this.scale = scale;
            this.heightOfChar = heightOfChar;
            this.charArray = charArray;
            this.characterDimensions = calculateDimensions();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasCodePoint(int codePoint) {
        return characterDimensions.containsKey(codePoint);
    }

    public int getPixelWidth(int codePoint, int defaultValue) {
        var dimension = characterDimensions.getOrDefault(codePoint, null);
        return dimension != null ? (dimension.width) : defaultValue;
    }

    public Map<Integer, CharacterDimensions> getCharacterDimensions() {
        return new HashMap<>(characterDimensions);
    }

    private Map<Integer, CharacterDimensions> calculateDimensions() {
        var map = new HashMap<Integer, CharacterDimensions>();

        for (int y = 0; y < charArray.length; y++) {

            var charRow = charArray[y];

            var startPixelY = y * pixelHeightPerChar();
            var endPixelY = startPixelY + pixelHeightPerChar() - 1;

            var codePointArray = charRow.codePoints().toArray();
            for (int x = 0; x < codePointArray.length; x++) {
                var codePointInt = codePointArray[x];

                var startPixelX = x * pixelWidthPerChar();
                var endPixelX = startPixelX + pixelWidthPerChar() - 1;

                map.put(codePointInt, getDimension(startPixelX, endPixelX, startPixelY, endPixelY));
            }
        }

        return map;
    }

    private CharacterDimensions getDimension(int startX, int endX, int startY, int endY) {
        var minX = endX;
        var maxX = startX;

        var minY = endY;
        var maxY = startY;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                var rgb = bufferedImage.getRGB(x, y);
                var color = new Color(rgb, true);
                if (color.getAlpha() < 0.1)
                    continue;
                if(color.getRed() == 0 && color.getBlue() == 0 && color.getGreen() == 0)
                    continue;
                if (x > maxX)
                    maxX = x;
                if (x < minX)
                    minX = x;
                if (y > maxY)
                    maxY = y;
                if (y < minY)
                    minY = y;
            }
        }
        return new CharacterDimensions(Math.round((Math.max(1, 1 + maxX - minX) * scale)), Math.round((Math.max(1, 1 + maxY - minY) * scale)));
    }

    public int pixelWidthPerChar() {
        return bufferedImage.getWidth() / amountCharsOnXAxis();
    }

    public int pixelHeightPerChar() {
        return bufferedImage.getHeight() / amountRows();
    }

    public int amountCharsOnXAxis() {
        return charArray[0].length();
    }

    public int amountRows() {
        return charArray.length;
    }

    @Override
    public String toString() {
        return "BitMapReader{" +
                "bufferedImage=" + bufferedImage +
                ", heightOfChar=" + heightOfChar +
                ", charArray=" + Arrays.toString(charArray) +
                ", characterDimensions=" + characterDimensions +
                '}';
    }

    public record CharacterDimensions(int width, int height) {
        @Override
        public String toString() {
            return "{" + width +
                    " - " + height +
                    '}';
        }
    }
}

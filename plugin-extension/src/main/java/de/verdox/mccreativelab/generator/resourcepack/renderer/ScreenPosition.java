package de.verdox.mccreativelab.generator.resourcepack.renderer;

/**
 *
 * @param x - The x Coordinate at the players screen. Left to right
 * @param y - The y Coordinate at the players screen. Bottom to top.
 * @param xOffset - The relative x offset calculated from the x coordinate
 * @param yOffset
 * @param layer
 * @param textType
 */
public record ScreenPosition(int x, int y, float xOffset, float yOffset, int layer, TextType textType) {
    public ScreenPosition(int x, int y, float xOffset, float yOffset, int layer) {
        this(x, y, xOffset, yOffset, layer, TextType.ACTION_BAR);
    }

    public ScreenPosition withTextType(TextType type) {
        return new ScreenPosition(x(), y(), xOffset(), yOffset(), layer(), type);
    }

    public ScreenPosition addToY(int val) {
        return new ScreenPosition(x(), y() + val, xOffset(), yOffset(), layer(), textType());
    }

    public ScreenPosition addToYOffset(float val) {
        return new ScreenPosition(x(), y(), xOffset(), yOffset() + val, layer(), textType());
    }

    public ScreenPosition addToXOffset(float val) {
        return new ScreenPosition(x(), y(), xOffset() + val, yOffset(), layer(), textType());
    }

    public ScreenPosition withLayer(int val) {
        return new ScreenPosition(x(), y(), xOffset(), yOffset(), val, textType());
    }

    public ScreenPosition withX(int val) {
        return new ScreenPosition(val, y(), xOffset(), yOffset(), layer(), textType());
    }

    public ScreenPosition withY(int val) {
        return new ScreenPosition(x(), val, xOffset(), yOffset(), layer(), textType());
    }

    public ScreenPosition withXOffset(float val) {
        return new ScreenPosition(x(), y(), val, yOffset(), layer(), textType());
    }

    public ScreenPosition withYOffset(float val) {
        return new ScreenPosition(x(), y(), xOffset(), val, layer(), textType());
    }

    public static ScreenPosition calculateTopLeftCornerOfInventorySlotIndex(int index, TextType textType) {
        var indexZeroPos = TextType.getTopLeftCorner(textType).addToXOffset(7).addToYOffset(-17);

        var x = index % 9;
        var y = index / 9;

        return indexZeroPos.addToYOffset(-((y) * 18)).addToXOffset( (x) * 18);

        //return new ScreenPosition(50, 50, -9, +3, 1, textType);
    }
}

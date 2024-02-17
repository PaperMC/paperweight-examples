package de.verdox.mccreativelab.generator.resourcepack.types.rendered.util;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.CustomGUIBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;

/**
 * @param x        - The x Coordinate at the players screen. Left to right
 * @param y        - The y Coordinate at the players screen. Bottom to top.
 * @param xOffset  - The relative x offset calculated from the x coordinate
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
        var indexZeroPos = TextType.getTopLeftCorner(textType)
                                   .addToXOffset(8)
                                   .addToYOffset(-18);

        var x = index % 9;
        var y = index / 9;

        return indexZeroPos.addToXOffset(x * 18)
                           .addToYOffset(y * -18);

        //return new ScreenPosition(50, 50, -9, +3, 1, textType);
    }

    public static ScreenPosition getScreenPositionOfSlot(int x, int y, CustomGUIBuilder customGUIBuilder, SlotOffset slotOffset, int textHeight) {
        ScreenPosition screenPosition = getFirstSlot(customGUIBuilder, slotOffset, textHeight);
        screenPosition = screenPosition
            .addToXOffset(x * 18)
            .addToYOffset(y * -18);
        return screenPosition;
    }

    public static ScreenPosition getScreenPositionOfSlot(int x, int y, CustomGUIBuilder customGUIBuilder, SlotOffset slotOffset) {
        return getScreenPositionOfSlot(x, y, customGUIBuilder, slotOffset, 0);
    }

    private static ScreenPosition getFirstSlot(CustomGUIBuilder customGUIBuilder, SlotOffset slotOffset, int textHeight) {
        return customGUIBuilder.getTopLeftPos()
                               .addToXOffset(8)
                               .addToXOffset(slotOffset.getXOffset())
                               .addToYOffset(-18)
                               .withLayer(2)
                               .addToYOffset(textHeight <= 0 ? slotOffset.getYOffset() : slotOffset.getYOffset(textHeight));
    }

    public enum SlotOffset {
        TOP_LEFT_CORNER(0, 0),
        TOP_MID_CORNER(8, 0),
        TOP_RIGHT_CORNER(16, 0),

        MID_LEFT_CORNER(0, -8),
        MID_MID_CORNER(8, -8),
        MID_RIGHT_CORNER(16, -8),

        BOTTOM_LEFT_CORNER(0, -16),
        BOTTOM_MID_CORNER(8, -16),
        BOTTOM_RIGHT_CORNER(16, -16),
        ;
        private final float xOffset;
        private final float yOffset;

        SlotOffset(float xOffset, float yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public float getYOffset(int textHeight) {
            return (textHeight * 1f / 2) + yOffset;
        }

        public float getYOffset() {
            return yOffset;
        }

        public float getXOffset() {
            return xOffset;
        }
    }

    public static JsonObjectBuilder toJson(ScreenPosition screenPosition){
        return JsonObjectBuilder.create()
            .add("x", screenPosition.x)
            .add("y", screenPosition.y)
            .add("xOffset", screenPosition.xOffset)
            .add("yOffset", screenPosition.yOffset)
            .add("layer", screenPosition.layer);
    }

    public static ScreenPosition fromJson(ScreenPosition screenPosition, JsonObject jsonObject){
        int x = jsonObject.get("x").getAsJsonPrimitive().getAsInt();
        int y = jsonObject.get("y").getAsJsonPrimitive().getAsInt();
        float xOffset = jsonObject.get("xOffset").getAsJsonPrimitive().getAsFloat();
        float yOffset = jsonObject.get("yOffset").getAsJsonPrimitive().getAsFloat();
        int layer = jsonObject.get("layer").getAsJsonPrimitive().getAsInt();
        return screenPosition.withX(x).withY(y).withXOffset(xOffset).withYOffset(yOffset).withLayer(layer);
    }
}

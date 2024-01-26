package de.verdox.mccreativelab.features.legacy.herobrine;

public enum ModelPart {
    HEAD(6,9,0),
    EYES(6,9,0),
    BODY,


    LEFT_ARM,
    LEFT_ARM_SLIM,
    RIGHT_ARM,
    RIGHT_ARM_SLIM,
    LEFT_LEG,
    RIGHT_LEG,
    ;
    private final float xOffset;
    private final float yOffset;
    private final float zOffset;

    ModelPart(float xOffset, float yOffset, float zOffset){
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    ModelPart(){
        this(0,0,0);
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public float getZOffset() {
        return zOffset;
    }
}

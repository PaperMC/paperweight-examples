package de.verdox.mccreativelab.util;

import java.util.concurrent.TimeUnit;

public class CropUtil {
    public static float calculatePercentageMalus(int randomTickSpeed, int cropAges, TimeUnit estimatedGrowthDuration, long time) {
        float bestCropChance = 0.33f;
        var wantedTimeInTicks = estimatedGrowthDuration.toSeconds(time) * 20;

        var result = (cropAges * 4096) / bestCropChance * randomTickSpeed * wantedTimeInTicks;
        if (result < 0)
            result = 0;
        if (result > 1)
            result = 1;
        return result;
    }

    public static void main(String[] args) {
        var result = möp(4137, 0.0000151f);

        System.out.println(TimeUnit.SECONDS.toMinutes((long) (result[0] / 20)) + " | " + TimeUnit.SECONDS.toMinutes((long) (result[1] / 20)));
    }


    public static float[] möp(float expectedValue, float probability) {
        var b = 3 * Math.sqrt(expectedValue * (1 - probability));
        System.out.println(b);
        return new float[]{(float) (expectedValue + b), (float) (expectedValue - b)};
    }
}

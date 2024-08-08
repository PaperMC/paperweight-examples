package de.verdox.mccreativelab.random;

public interface VanillaRandomSource {
    void setSeed(long seed);

    int nextInt();

    int nextInt(int bound);

    int nextIntBetweenInclusive(int min, int max);

    long nextLong();

    boolean nextBoolean();

    float nextFloat();

    double nextDouble();

    double nextGaussian();

    double triangle(double mode, double deviation);

    void consumeCount(int count);

    int nextInt(int min, int max);
}

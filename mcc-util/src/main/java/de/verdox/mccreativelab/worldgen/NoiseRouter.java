package de.verdox.mccreativelab.worldgen;

public interface NoiseRouter {
    DensityFunction barrierNoise();
    DensityFunction fluidLevelFloodednessNoise();
    DensityFunction fluidLevelSpreadNoise();
    DensityFunction lavaNoise();
    DensityFunction temperature();
    DensityFunction vegetation();
    DensityFunction continents();
    DensityFunction erosion();
    DensityFunction depth();
    DensityFunction ridges();
    DensityFunction initialDensityWithoutJaggedness();
    DensityFunction finalDensity();
    DensityFunction veinToggle();
    DensityFunction veinRidged();
    DensityFunction veinGap();
}

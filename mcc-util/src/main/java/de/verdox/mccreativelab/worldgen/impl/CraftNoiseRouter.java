package de.verdox.mccreativelab.worldgen.impl;

import de.verdox.mccreativelab.worldgen.DensityFunction;
import de.verdox.mccreativelab.worldgen.NoiseRouter;

public class CraftNoiseRouter implements NoiseRouter {
    private final net.minecraft.world.level.levelgen.NoiseRouter handle;

    public CraftNoiseRouter(net.minecraft.world.level.levelgen.NoiseRouter handle){
        this.handle = handle;
    }

    @Override
    public DensityFunction barrierNoise() {
        return new CraftDensityFunction(handle.barrierNoise());
    }

    @Override
    public DensityFunction fluidLevelFloodednessNoise() {
        return new CraftDensityFunction(handle.fluidLevelFloodednessNoise());
    }

    @Override
    public DensityFunction fluidLevelSpreadNoise() {
        return new CraftDensityFunction(handle.fluidLevelSpreadNoise());
    }

    @Override
    public DensityFunction lavaNoise() {
        return new CraftDensityFunction(handle.lavaNoise());
    }

    @Override
    public DensityFunction temperature() {
        return new CraftDensityFunction(handle.temperature());
    }

    @Override
    public DensityFunction vegetation() {
        return new CraftDensityFunction(handle.vegetation());
    }

    @Override
    public DensityFunction continents() {
        return new CraftDensityFunction(handle.continents());
    }

    @Override
    public DensityFunction erosion() {
        return new CraftDensityFunction(handle.erosion());
    }

    @Override
    public DensityFunction depth() {
        return new CraftDensityFunction(handle.depth());
    }

    @Override
    public DensityFunction ridges() {
        return new CraftDensityFunction(handle.ridges());
    }

    @Override
    public DensityFunction initialDensityWithoutJaggedness() {
        return new CraftDensityFunction(handle.initialDensityWithoutJaggedness());
    }

    @Override
    public DensityFunction finalDensity() {
        return new CraftDensityFunction(handle.finalDensity());
    }

    @Override
    public DensityFunction veinToggle() {
        return new CraftDensityFunction(handle.veinToggle());
    }

    @Override
    public DensityFunction veinRidged() {
        return new CraftDensityFunction(handle.veinRidged());
    }

    @Override
    public DensityFunction veinGap() {
        return new CraftDensityFunction(handle.veinGap());
    }
}

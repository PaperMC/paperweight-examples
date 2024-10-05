package de.verdox.mccreativelab.worldgen.impl;

import de.verdox.mccreativelab.worldgen.NoiseRouter;
import de.verdox.mccreativelab.worldgen.WorldGenData;
import net.minecraft.server.level.ServerLevel;

public class CraftWorldGenData implements WorldGenData {
    private final ServerLevel serverLevel;

    public CraftWorldGenData(ServerLevel serverLevel){
        this.serverLevel = serverLevel;
    }

    @Override
    public NoiseRouter getNoiseRouter() {
        return new CraftNoiseRouter(this.serverLevel.chunkSource.randomState().router());
    }
}

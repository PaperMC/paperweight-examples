package de.verdox.mccreativelab.entity;

import de.verdox.mccreativelab.ai.behavior.PaperCustomAIBehavior;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Villager;

import java.util.Map;

public class TestVillagerBehaviorCustom extends PaperCustomAIBehavior<Villager> {
    public TestVillagerBehaviorCustom() {
        super(Villager.class, Map.of());
    }

    @Override
    public void start(World world, Villager entity, long time) {
        Bukkit.getLogger().info("Starting TestBehaviour");
    }

    @Override
    public void tick(World world, Villager entity, long time) {

    }

    @Override
    public void stop(World world, Villager entity, long time) {
        Bukkit.getLogger().info("Stopping TestBehaviour");
    }

    @Override
    public boolean canStillUse(World world, Villager entity, long time) {
        return true;
    }

    @Override
    public boolean checkExtraStartConditions(World world, Villager entity, long time) {
        return true;
    }
}

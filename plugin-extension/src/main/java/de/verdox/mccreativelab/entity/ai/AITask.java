package de.verdox.mccreativelab.entity.ai;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.CompletableFuture;

public class AITask<E extends LivingEntity> {
    private final World world;
    private final E entity;
    private final CompletableFuture<Boolean> future = new CompletableFuture<>();
    private boolean currentlyCalculating;
    private boolean used;

    public AITask(World world, E entity) {
        this.world = world;
        this.entity = entity;
    }

    public void doTaskAsync(TriFunction<World, E, Long, Boolean> asyncTask) {
        doTaskAsync(asyncTask, 0);
    }

    public void doTaskAsync(TriFunction<World, E, Long, Boolean> asyncTask, int tickDelay) {
        if (currentlyCalculating)
            return;
        used = true;
        currentlyCalculating = true;
        Bukkit.getScheduler().runTaskLaterAsynchronously(MCCreativeLabExtension.getInstance(), () -> doAction(asyncTask), tickDelay);
    }

    public void doTaskSync(TriFunction<World, E, Long, Boolean> asyncTask) {
        if (!Bukkit.isPrimaryThread())
            doTaskSync(asyncTask, 0);
        if (currentlyCalculating)
            return;
        used = true;
        currentlyCalculating = true;
        doAction(asyncTask);
    }

    public void doTaskSync(TriFunction<World, E, Long, Boolean> asyncTask, int tickDelay) {
        if (currentlyCalculating)
            return;
        used = true;
        currentlyCalculating = true;
        Bukkit.getScheduler().runTaskLater(MCCreativeLabExtension.getInstance(), () -> doAction(asyncTask), tickDelay);
    }

    public CompletableFuture<Boolean> getFuture() {
        return future;
    }

    public boolean isCurrentlyCalculating() {
        return currentlyCalculating;
    }

    public boolean isUsed() {
        return used;
    }

    private void doAction(TriFunction<World, E, Long, Boolean> asyncTask) {
        try {
            future.complete(asyncTask.apply(world, entity, world.getGameTime()));
        } catch (Throwable e) {
            Bukkit.getLogger().warning("Error occured while doing aiTask");
            future.complete(false);
            e.printStackTrace();
        } finally {
            if (Bukkit.isPrimaryThread())
                currentlyCalculating = false;
            else
                Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), () -> currentlyCalculating = false);
        }
    }

}

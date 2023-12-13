package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

import javax.annotation.Nullable;

public abstract class EntityMetadataPredicate<T> {
    private final String key;

    public EntityMetadataPredicate(String key) {
        this.key = key;
    }

    public boolean isAllowed(Entity entity) {
        T storedValue = getValue(entity);
        T currentValue = getCurrentValue(entity);

        return storedValue == null || test(storedValue, currentValue);
    }

    public void reset(Entity entity) {
        entity.setMetadata(key, new FixedMetadataValue(MCCreativeLabExtension.getInstance(), getCurrentValue(entity)));
    }

    @Nullable
    private T getValue(Entity entity) {
        if (entity.hasMetadata(key))
            return (T) entity.getMetadata(key).get(0).value();
        return null;
    }

    protected abstract T getCurrentValue(Entity entity);

    protected abstract boolean test(T storedValue, T currentValue);

    public static class TickDelay extends EntityMetadataPredicate<Integer> {
        private final int tickDelay;

        public TickDelay(String key, int tickDelay) {
            super(key);
            this.tickDelay = tickDelay;
        }

        @Override
        protected Integer getCurrentValue(Entity entity) {
            return Bukkit.getCurrentTick();
        }

        @Override
        protected boolean test(Integer storedValue, Integer currentValue) {
            return currentValue - storedValue >= tickDelay;
        }

        @Override
        public void reset(Entity entity) {
            super.reset(entity);
        }
    }

    public static class DistanceTravelled extends EntityMetadataPredicate<Location> {
        private final double minDistance;

        public DistanceTravelled(String key, double minDistance) {
            super(key);
            this.minDistance = minDistance;
        }

        @Override
        protected Location getCurrentValue(Entity entity) {
            return entity.getLocation();
        }

        @Override
        protected boolean test(Location storedValue, Location currentValue) {
            return storedValue.distanceSquared(currentValue) >= minDistance;
        }
    }
}

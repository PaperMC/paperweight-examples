package de.verdox.mccreativelab.wrapper.entity;

import de.verdox.mccreativelab.serialization.NBTSerializer;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.wrapper.MCCWrapped;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MCCEntityType extends MCCWrapped {
    static MCCEntityType wrap(EntityType entityType) {
        return new Vanilla(entityType);
    }

    CompletableFuture<MCCEntity> summon(@NotNull Location location, @NotNull CreatureSpawnEvent.SpawnReason spawnReason, @Nullable Consumer<MCCEntity> callback);

    class Vanilla extends MCCWrapped.Impl<EntityType> implements MCCEntityType {

        public static final NBTSerializer<MCCEntityType.Vanilla> INSTANCE = new NBTSerializer<>() {
            @Override
            public void serialize(Vanilla data, NBTContainer nbtContainer) {
                nbtContainer.set("type", data.getHandle().name());
            }

            @Override
            public Vanilla deserialize(NBTContainer nbtContainer) {
                if (!nbtContainer.has("type"))
                    return null;
                return new Vanilla(EntityType.valueOf(nbtContainer.getString("type")));
            }
        };

        protected Vanilla(EntityType handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getKey();
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if (mccWrapped instanceof Vanilla vanilla)
                return vanilla.getHandle().equals(getHandle());
            return false;
        }

        @Override
        public CompletableFuture<MCCEntity> summon(@NotNull Location location, CreatureSpawnEvent.@NotNull SpawnReason spawnReason, @Nullable Consumer<MCCEntity> callback) {
            CompletableFuture<MCCEntity> completableFuture = new CompletableFuture<>();
            location.getWorld().getChunkAtAsync(location).whenComplete((chunk, throwable) -> {
                chunk.getWorld().spawnEntity(location, getHandle(), spawnReason, entity -> {
                    MCCEntity mccEntity = MCCEntity.wrap(entity);
                    if (callback != null)
                        callback.accept(mccEntity);
                    completableFuture.complete(mccEntity);
                });

            });
            return completableFuture;
        }
    }
}

package de.verdox.mccreativelab.wrapper.entity;

import de.verdox.mccreativelab.wrapper.MCCWrapped;
import de.verdox.mccreativelab.wrapper.block.MCCBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface MCCEntity extends MCCWrapped {
    static MCCEntity wrap(Entity entity){
        return new Vanilla(entity);
    }

    MCCEntityType getType();

    class Vanilla extends MCCWrapped.Impl<Entity> implements MCCEntity {
        protected Vanilla(Entity handle) {
            super(handle);
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return getHandle().getType().getKey();
        }

        @Override
        public MCCEntityType getType() {
            return MCCEntityType.wrap(getHandle().getType());
        }

        @Override
        public boolean matches(MCCWrapped mccWrapped) {
            if(mccWrapped instanceof MCCBlockData.Vanilla vanilla)
                return vanilla.getHandle().equals(getHandle());
            return false;
        }
    }
}

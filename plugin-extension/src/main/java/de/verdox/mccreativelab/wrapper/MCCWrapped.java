package de.verdox.mccreativelab.wrapper;

import org.bukkit.Keyed;

import java.util.Objects;

/**
 * A wrapped object
 */
public interface MCCWrapped extends Keyed {
    Object getHandle();

    boolean matches(MCCWrapped mccWrapped);

    abstract class Impl<T> implements MCCWrapped {
        private T handle;

        protected Impl(T handle) {
            Objects.requireNonNull(handle);
            this.handle = handle;
        }

        @Override
        public T getHandle() {
            return handle;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                "handle=" + handle +
                '}';
        }
    }
}

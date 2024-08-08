package de.verdox.mccreativelab;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Used as wrapper class for custom implemented behaviour.
 * This is normally used to change constants in the minecraft gameplay.
 * @param <T>
 */
public class CustomBehaviour<T> {
    private final ProxyInterface<T> proxyInterface;
    private final String key;
    public CustomBehaviour(Class<? extends T> type, T defaultImplementation, String key){
        this.key = key;
        this.proxyInterface = new ProxyInterface<>(type, defaultImplementation);
    }

    public String getKey() {
        return key;
    }

    /**
     * Used to set a new behaviour.
     * @param behaviour The new implementation
     */
    public void setBehaviour(@NotNull T behaviour) {
        Objects.requireNonNull(behaviour);
        this.proxyInterface.addImplementation(behaviour);
    }

    @ApiStatus.Internal
    public T getBehaviour() {
        return proxyInterface.getImplementation();
    }

    /**
     * Checks if a custom implementation exists
     * @return true if a custom implementation exists
     */
    public boolean isImplemented(){
        return proxyInterface.isImplemented();
    }
}

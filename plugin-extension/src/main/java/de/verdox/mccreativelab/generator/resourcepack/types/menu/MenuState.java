package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MenuState {
    private final Map<PlayerKeyInput, List<Consumer<ActiveMenu>>> keyListeners = new HashMap<>();
    private final List<Consumer<ActiveMenu>> chainedOperations = new LinkedList<>();
    private Consumer<ActiveMenu> onEnableState;
    private Consumer<ActiveMenu> onDisableState;
    private final boolean visibleOnOpen;

    public MenuState(boolean visibleOnOpen){
        this.visibleOnOpen = visibleOnOpen;
    }

    public boolean isVisibleOnOpen() {
        return visibleOnOpen;
    }

    public MenuState clearKeyInputs(@Nullable PlayerKeyInput playerKeyInput) {
        if (playerKeyInput != null)
            keyListeners.remove(playerKeyInput);
        else
            keyListeners.clear();
        return this;
    }

    public MenuState clearAllKeyInputs() {
        return clearKeyInputs(null);
    }

    public MenuState clearDefaultOperations() {
        chainedOperations.clear();
        return this;
    }

    public MenuState onKey(PlayerKeyInput playerKeyInput, Consumer<ActiveMenu> consumer) {
        keyListeners.computeIfAbsent(playerKeyInput, playerKeyInput1 -> new LinkedList<>()).add(consumer);
        return this;
    }

    public MenuState addDefaultOperation(Consumer<ActiveMenu> consumer) {
        chainedOperations.add(consumer);
        return this;
    }

    public MenuState onDisable(Consumer<ActiveMenu> onDisableState) {
        this.onDisableState = onDisableState;
        return this;
    }

    public MenuState onEnable(Consumer<ActiveMenu> onEnableState) {
        this.onEnableState = onEnableState;
        return this;
    }

    @NotNull
    List<Consumer<ActiveMenu>> getChainedOperations() {
        return chainedOperations;
    }

    @NotNull
    List<Consumer<ActiveMenu>> getMenuOperationsOnKey(PlayerKeyInput keyInput) {
        return new LinkedList<>(keyListeners.computeIfAbsent(keyInput, playerKeyInput1 -> new LinkedList<>()));
    }

    @NotNull
    Consumer<ActiveMenu> getOnDisableState() {
        return onDisableState != null ? onDisableState : (activeMenu) -> {
        };
    }

    @NotNull
    Consumer<ActiveMenu> getOnEnableState() {
        return onEnableState != null ? onEnableState : (activeMenu) -> {
        };
    }
}

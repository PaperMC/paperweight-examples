package de.verdox.mccreativelab.generator.resourcepack.types.hud;

import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ComponentRendered;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CustomHud extends ComponentRendered<CustomHud, ActiveHud> {
    private @Nullable Consumer<ActiveHud> initialSetup;
    private @Nullable Consumer<ActiveHud> whileOpen;

    public CustomHud(NamespacedKey namespacedKey) {
        super(namespacedKey);
    }

    public @Nullable Consumer<ActiveHud> getInitialSetup() {
        return initialSetup;
    }

    public @Nullable Consumer<ActiveHud> getWhileOpen() {
        return whileOpen;
    }

    public CustomHud whileHudOpen(Consumer<ActiveHud> whileOpen) {
        this.whileOpen = whileOpen;
        return this;
    }

    public CustomHud withInitialHudSetup(Consumer<ActiveHud> initialSetup) {
        this.initialSetup = initialSetup;
        return this;
    }
}

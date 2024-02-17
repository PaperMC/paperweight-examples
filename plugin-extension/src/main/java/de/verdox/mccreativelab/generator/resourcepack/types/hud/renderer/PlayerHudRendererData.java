package de.verdox.mccreativelab.generator.resourcepack.types.hud.renderer;

import de.verdox.mccreativelab.generator.resourcepack.types.hud.ActiveHud;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerHudRendererData {
    private final Map<NamespacedKey, ActiveHud> cache = new ConcurrentHashMap<>();
    private final AtomicBoolean needsUpdate = new AtomicBoolean(true);
    private Component lastRendered;
    private final Player player;
    public PlayerHudRendererData(Player player) {
        this.player = player;
    }

    public Component getLastRendered() {
        return lastRendered;
    }

    @Nullable
    public ActiveHud getActiveHud(CustomHud customHud) {
        return cache.getOrDefault(customHud.key(), null);
    }

    @NotNull
    public ActiveHud getOrStartActiveHud(CustomHud customHud) {
        addToRendering(customHud);
        return Objects.requireNonNull(getActiveHud(customHud));
    }

    public void forceUpdate() {
        this.needsUpdate.set(true);
    }

    public void addToRendering(CustomHud customHud) {
        var activeHud = new ActiveHud(player, customHud);
        if (cache.containsKey(activeHud.getComponentRendered().key()))
            return;
        cache.put(activeHud.getComponentRendered().key(), activeHud);
        this.needsUpdate.set(true);
    }

    public synchronized void sendUpdate() {
        var component = buildRendering();
        if (component == null)
            return;
        player.sendActionBar(component);
        this.needsUpdate.set(false);
    }

    public boolean removeFromRendering(CustomHud customHud) {
        if (!cache.containsKey(customHud.key()))
            return false;
        cache.remove(customHud.key());
        this.needsUpdate.set(true);
        return true;
    }

    public Component buildRendering() {
        if (this.needsUpdate.get()) {

            this.needsUpdate.set(false);
        }

        Component component = null;
        for (ActiveHud value : cache.values()) {
            if(value.getComponentRendered().getWhileOpen() != null)
                value.getComponentRendered().getWhileOpen().accept(value);
            value.forEachElementBehavior((activeHudRenderedHudElementBehavior, rendered) -> activeHudRenderedHudElementBehavior.whileOpen(value, player, rendered));
            var rendering = value.render();
            if (component == null)
                component = rendering;
            else
                component = component.append(rendering);
        }
        lastRendered = component;

        return lastRendered;
    }
}

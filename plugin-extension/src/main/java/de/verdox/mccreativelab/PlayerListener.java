package de.verdox.mccreativelab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerListener {
    public static PlayerListener create(Player player) {
        return new PlayerListener(UUID.randomUUID(), player);
    }
    private final Set<Consumer<PlayerQuitEvent>> onQuit = new HashSet<>();
    private final Set<Consumer<BlockBreakEvent>> onBlockBreak = new HashSet<>();
    private final UUID uuid;
    private final Player player;

    private PlayerListener(UUID uuid, Player player) {
        this.uuid = uuid;
        this.player = player;
        if (!player.isOnline())
            return;
        player.setMetadata("player_listener_" + uuid.toString(), new FixedMetadataValue(MCCreativeLabExtension.getInstance(), this));
        Bukkit.getPluginManager().registerEvents(new CustomListener(), MCCreativeLabExtension.getInstance());
    }

    public PlayerListener onQuit(Consumer<PlayerQuitEvent> event){
        onQuit.add(event);
        return this;
    }

    public PlayerListener onBlockBreak(Consumer<BlockBreakEvent> event){
        onBlockBreak.add(event);
        return this;
    }

    private <E extends Event> boolean consumeEvent(Set<Consumer<E>> consumerSet, E event, Player player) {
        if (!player.equals(this.player))
            return false;
        consumerSet.forEach(eConsumer -> eConsumer.accept(event));
        return true;
    }

    private <E extends PlayerEvent> boolean consumeEvent(Set<Consumer<E>> consumerSet, E event) {
        if (!event.getPlayer().equals(this.player))
            return false;
        consumerSet.forEach(eConsumer -> eConsumer.accept(event));
        return true;
    }

    private class CustomListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void consume(PlayerQuitEvent e) {
            if (!consumeEvent(onQuit, e))
                return;
            HandlerList.unregisterAll(CustomListener.this);
            player.removeMetadata("player_listener_" + uuid.toString(), MCCreativeLabExtension.getInstance());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void consume(BlockBreakEvent e) {
            consumeEvent(onBlockBreak, e, player);
        }
    }

}

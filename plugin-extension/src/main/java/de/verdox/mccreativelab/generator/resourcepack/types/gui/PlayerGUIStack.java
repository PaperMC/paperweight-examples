package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.event.GUICloseEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.Stack;

/**
 * Tracks the GUIs opened by a player
 */
public class PlayerGUIStack implements Listener {

    private final Stack<StackElement> stack = new Stack<>();
    private final Player player;

    static PlayerGUIStack load(Player player) {
        if(!player.hasMetadata("playerGUIStack"))
            player.setMetadata("playerGUIStack", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), new PlayerGUIStack(player)));

        return (PlayerGUIStack) player.getMetadata("playerGUIStack").get(0).value();
    }

    public PlayerGUIStack(Player player) {
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, MCCreativeLabExtension.getInstance());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!e.getPlayer().equals(this.player))
            return;
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(GUICloseEvent e) {
        if (stack.isEmpty() || e.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW))
            return;
        if(e.getReason().equals(InventoryCloseEvent.Reason.PLAYER)){
            popAndOpenLast(e.getPlayer(), e.getActiveGUI());
        }
        else
            clear();
    }

    public void popAndOpenLast(Player player, ActiveGUI activeGUI) {
        StackElement stackElement = stack.pop();

        if (stackElement.activeGUI.getComponentRendered().equals(activeGUI.getComponentRendered()))
            return;

        stackElement.activeGUI.openToPlayer(player);
    }

    public void trackGUI(ActiveGUI activeGUI) {
        StackElement stackElement = new StackElement(activeGUI, activeGUI.tempData);
        stack.push(stackElement);
    }

    public void clear() {
        stack.clear();
    }

    private record StackElement(ActiveGUI activeGUI, Map<String, Object> tempData) {

    }
}

package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.event.GUICloseEvent;
import de.verdox.mccreativelab.util.nbt.NBTContainer;
import de.verdox.mccreativelab.util.nbt.NBTPersistent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * Tracks the GUIs opened by a player
 */
public class PlayerGUIStack implements NBTPersistent, Listener {

    private final Stack<StackElement> stack = new Stack<>();
    private final Player player;

    static PlayerGUIStack load(Player player) {
        return player.getPersistentDataContainer().getPersistentDataObjectCache().loadOrSupplyPersistentDataObject(new NamespacedKey("mccreativelab", "gui_stack"), () -> new PlayerGUIStack(player));
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
            StackElement stackElement = stack.pop();

            if (stackElement.customGUIBuilder.equals(e.getActiveGUI().getComponentRendered()))
                return;

            stackElement.customGUIBuilder().createMenuForPlayer(e.getPlayer(), activeGUI -> {
                activeGUI.tempData.putAll(stackElement.tempData);
            });
        }
        else
            clear();
    }

    public void trackGUI(ActiveGUI activeGUI) {
        CustomGUIBuilder customGUIBuilder = activeGUI.getComponentRendered();
        StackElement stackElement = new StackElement(customGUIBuilder, activeGUI.tempData);
        stack.push(stackElement);
    }

    public void clear() {
        stack.clear();
    }


    @Override
    public void saveNBTData(NBTContainer storage) {

    }

    @Override
    public void loadNBTData(NBTContainer storage) {

    }

    private record StackElement(CustomGUIBuilder customGUIBuilder, Map<String, Object> tempData) {

    }
}

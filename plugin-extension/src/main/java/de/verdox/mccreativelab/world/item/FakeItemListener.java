package de.verdox.mccreativelab.world.item;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.registry.Reference;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class FakeItemListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventVanillaDurabilityChange(PlayerItemDamageEvent e){
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(e.getItem());
        if(fakeItemReference == null)
            return;
        e.setCancelled(fakeItemReference.unwrapValue().getFakeItemProperties().isPreventNormalDurabilityChange());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventItemDrop(PlayerDropItemEvent e){
        ItemStack stack = e.getItemDrop().getItemStack();
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(stack);
        if(fakeItemReference == null)
            return;
        e.setCancelled(fakeItemReference.unwrapValue().getFakeItemProperties().isPreventDrop());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventItemDrop(EntityDropItemEvent e){
        ItemStack stack = e.getItemDrop().getItemStack();
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(stack);
        if(fakeItemReference == null)
            return;
        e.setCancelled(fakeItemReference.unwrapValue().getFakeItemProperties().isPreventDrop());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventItemClick(InventoryClickEvent e){
        if(e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE))
            return;
        if(e.getCurrentItem() == null)
            return;
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(e.getCurrentItem());
        if(fakeItemReference == null)
            return;
        fakeItemReference.unwrapValue().onClick(e);
        e.setCancelled(fakeItemReference.unwrapValue().getFakeItemProperties().isPreventInventoryClick());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventItemClick(InventoryMoveItemEvent e){
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(e.getItem());
        if(fakeItemReference == null)
            return;
        e.setCancelled(fakeItemReference.unwrapValue().getFakeItemProperties().isPreventInventoryClick());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventItemClick(InventoryDragEvent e){
        if(e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE))
            return;
        Reference<? extends FakeItem> fakeItemReference = MCCreativeLabExtension.getFakeItemRegistry().getFakeItem(e.getCursor());
        if(fakeItemReference == null)
            return;
        e.setCancelled(fakeItemReference.unwrapValue().getFakeItemProperties().isPreventInventoryClick());
    }
}

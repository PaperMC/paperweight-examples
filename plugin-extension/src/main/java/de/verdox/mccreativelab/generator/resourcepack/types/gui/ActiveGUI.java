package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.renderer.ActiveHud;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class ActiveGUI implements Listener {
    private final CustomGUIBuilder customGUIBuilder;
    private final ActiveHud activeHud;
    private long lastShift = System.currentTimeMillis();
    private static final long SHIFT_COOLDOWN_MILLIS = 20;

    private final Map<Integer, ClickableItem> clickableItems = new HashMap<>();
    private final Map<String, Object> tempData = new HashMap<>();
    private final Map<String, ActiveGUIElement<?>> activeGUIElements = new HashMap<>();
    private boolean isOpen;
    private Inventory inventory;
    private boolean isUpdating;
    private Component rendering;
    private BukkitTask updateTask;
    private volatile boolean isUpdatingRendering;
    private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private ItemStack cursor;

    ActiveGUI(CustomGUIBuilder customGUIBuilder, ActiveHud activeHud, Component initialRendering, @Nullable Consumer<ActiveGUI> initialSetup) {
        this.customGUIBuilder = customGUIBuilder;
        this.activeHud = activeHud;
        Bukkit.getPluginManager().registerEvents(this, MCCreativeLabExtension.getInstance());
        PlayerFakeEmptyInventory.registerStoredInventoryListener(MCCreativeLabExtension.getInstance());

        customGUIBuilder.clickableButtons.forEach((s, clickableButton) -> activeGUIElements.put(s, new ActiveGUIElement.ClickableButton(this, clickableButton)));

        this.rendering = initialRendering;

        this.activeHud.showAll();
        forceUpdate();

        if (initialSetup != null)
            initialSetup.accept(this);
        forceUpdate();

        if (customGUIBuilder.onOpen != null) {
            customGUIBuilder.onOpen.accept(this);
            forceUpdate();
        }

        if (customGUIBuilder.whileOpen != null && customGUIBuilder.updateInterval > 0) {
            updateTask = Bukkit.getScheduler().runTaskTimer(MCCreativeLabExtension.getInstance(), () -> {
                if (isOpen) {
                    customGUIBuilder.whileOpen.accept(this);
                }
            }, 20L, customGUIBuilder.updateInterval);
        }
    }

    public ActiveGUI(CustomGUIBuilder customGUIBuilder, ActiveHud activeHud, Component initialRendering) {
        this(customGUIBuilder, activeHud, initialRendering, null);
    }

    public <T extends ActiveGUIElement<?>> boolean executeOnElement(String id, Class<? extends T> type, Consumer<T> execution) {
        var element = activeGUIElements.getOrDefault(id, null);
        if (element != null && type.isAssignableFrom(element.getClass())) {
            element.setVisible(true);
            execution.accept(type.cast(element));
            forceUpdate();
            return true;
        }
        return false;
    }

    public Inventory getVanillaInventory() {
        return inventory;
    }

    public <R> ActiveGUI addTemporaryData(String key, R value) {
        tempData.put(key, value);
        return this;
    }

    public <R> R getTemporaryDataOrDefault(String key, Class<? extends R> type, R defaultVal) {
        return type.cast(tempData.getOrDefault(key, defaultVal));
    }

    @EventHandler
    private void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().equals(inventory)))
            return;
        if (isOpen)
            return;
        if (isUpdating) return;
        isOpen = true;
        if (this.customGUIBuilder.isUsePlayerSlots()) {
            ((Player) e.getPlayer()).doInventorySynchronization(false);
        }
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().equals(inventory)))
            return;
        if (!isOpen)
            return;
        if (isUpdating) return;
        if (updateTask != null)
            updateTask.cancel();
        isOpen = false;
        if (customGUIBuilder.onClose != null)
            customGUIBuilder.onClose.accept(this);

        HandlerList.unregisterAll(this);

        var player = (Player) e.getPlayer();
        player.doInventorySynchronization(true);
        player.updateInventory();
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;
        if (!player.equals(activeHud.getPlayer()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        if (!e.getView().getTopInventory().equals(this.inventory))
            return;


        if (isUpdating) {
            e.setCancelled(true);
            return;
        }

        var clickableItem = clickableItems.getOrDefault(e.getRawSlot(), null);
        if ((customGUIBuilder.isSlotBlocked(e.getRawSlot()) && this.inventory.equals(e.getClickedInventory())) || clickableItem != null)
            e.setCancelled(true);
        // Prevent inventory clicks if is using player slots
        if (customGUIBuilder.isUsePlayerSlots() && Objects.equals(e.getClickedInventory(), e.getView()
                                                                                            .getBottomInventory()))
            e.setCancelled(true);

        if (clickableItem != null && clickableItem.getOnClick() != null)
            clickableItem.getOnClick().accept(e, this);

        if (e.isShiftClick() && e.getView().getBottomInventory().equals(e.getClickedInventory())) {
            if (customGUIBuilder.isUsePlayerSlots() || System.currentTimeMillis() - lastShift < SHIFT_COOLDOWN_MILLIS) {
                e.setCancelled(true);
                return;
            }
            lastShift = System.currentTimeMillis();

            shiftItemToInventory(e.getView()
                                  .getBottomInventory(), this.inventory, e.getSlot(), customGUIBuilder.getBlockedSlots());
            executeClickCallback(e, clickableItem);

            e.setCancelled(true);
            return;
        }

        executeClickCallback(e, clickableItem);
    }

    private void executeClickCallback(InventoryClickEvent e, ClickableItem clickableItem) {
        if (customGUIBuilder.clickConsumer == null)
            return;


        Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), () -> {
            customGUIBuilder.clickConsumer.accept(clickableItem, e, this);
            if (!e.isCancelled() && e.getClickedInventory() != null && e.getClickedInventory()
                                                                        .equals(e.getView().getBottomInventory())) {
                cursor = e.getCurrentItem();
            }
        });
    }

    @EventHandler
    private void onClick(InventoryDragEvent e) {
        if (!(e.getInventory().equals(inventory)))
            return;
        if (isUpdating) {
            e.setCancelled(true);
            return;
        }
        ;
        var rawSlotUsed = e.getRawSlots().stream().anyMatch(customGUIBuilder::isSlotBlocked);
        if (rawSlotUsed)
            e.setCancelled(true);
    }


    public ActiveHud getActiveHud() {
        return activeHud;
    }

    public CustomGUIBuilder getCustomGUIBuilder() {
        return customGUIBuilder;
    }

/*    public ActiveGUI setItem(int index, ClickableItem clickableItem) {
        for (int x = 0; x < clickableItem.getXSize(); x++) {
            for (int y = 0; y < clickableItem.getYSize(); y++) {
                int slotIndex = index + x + (9 * y);
                if (slotIndex < inventory.getSize()) {
                    inventory.setItem(slotIndex, clickableItem.getButtonItems().get(x, y));
                    clickableItems.put(slotIndex, clickableItem);
                }
            }
        }
        return this;
    }*/

    public void removeClickableItemFromSlot(int index, ClickableItem clickableItem) {
        for (int x = 0; x < clickableItem.getXSize(); x++) {
            for (int y = 0; y < clickableItem.getYSize(); y++) {
                int slotIndex = index + x + (9 * y);
                if (clickableItems.containsKey(slotIndex) && !clickableItems.get(slotIndex).equals(clickableItem))
                    break;
                inventory.setItem(slotIndex, null);
                clickableItems.remove(slotIndex);
            }
        }


    }

    public ActiveGUI removeItem(int index, ClickableItem clickableItem){
        this.inventory.setItem(index, null);

        for (int x = 0; x < clickableItem.getXSize(); x++) {
            for (int y = 0; y < clickableItem.getYSize(); y++) {
                int slotIndex = index + x + (9 * y);

                if (slotIndex < inventory.getSize()) {

                    if(clickableItems.containsKey(slotIndex) && clickableItems.get(slotIndex).equals(clickableItem)) {
                        this.inventory.setItem(slotIndex, null);
                        clickableItems.remove(slotIndex);
                    }

                }
            }
        }
        return this;
    }

    public ActiveGUI removeItem(int index){
        this.inventory.setItem(index, null);
        clickableItems.remove(index);
        return this;
    }

    public ActiveGUI setItem(int index, ClickableItem clickableItem) {
        this.inventory.setItem(index, clickableItem.getStack());

        for (int x = 0; x < clickableItem.getXSize(); x++) {
            for (int y = 0; y < clickableItem.getYSize(); y++) {
                int slotIndex = index + x + (9 * y);
                if (slotIndex == index) {
                    // Set real stack
                    var stack = clickableItem.getStack();
                    if (slotIndex < inventory.getSize()) {
                        inventory.setItem(slotIndex, stack);
                        clickableItems.put(slotIndex, clickableItem);
                    }
                } else {
                    if (slotIndex < inventory.getSize()) {
                        inventory.setItem(slotIndex, clickableItem.getStack().clone());
                        clickableItems.put(slotIndex, clickableItem);
                    }
                }
            }
        }
        return this;
    }

    public void forceUpdate() {
        if (!Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), this::forceUpdate);
        if (isUpdating)
            return;

        this.rendering = this.activeHud.render();

        Player player = activeHud.getPlayer();
        player.updateInventory();
        var itemAtCursor = activeHud.getPlayer().getOpenInventory().getCursor() != null ? player
                .getOpenInventory()
                .getCursor()
                .clone() : null;
/*        if (!isOpen || !activeHud.getPlayer().getItemOnCursor().getType().isAir())
            return;*/

        try {
            ItemStack[] oldContent;
            if (this.inventory != null) {
                if (!isOpen)
                    return;
                isUpdating = true;
                oldContent = this.inventory.getContents();
            } else oldContent = null;

            if (customGUIBuilder.getType() != null)
                this.inventory = Bukkit.createInventory(player, customGUIBuilder.getType(), rendering);
            else
                this.inventory = Bukkit.createInventory(player, customGUIBuilder.getChestSize() * 9, rendering);

            if (oldContent != null)
                this.inventory.setContents(oldContent);

            openUpdatedInventory(player, itemAtCursor);

/*            clickableItems.forEach((integer, clickableItem) -> {
                var x = integer % 9;
                var y = integer / 9;
                var itemToSet = clickableItem.getButtonItems().get(x, y);
                if (!Objects.equals(itemToSet, inventory.getItem(integer)))
                    inventory.setItem(integer, itemToSet);
            });*/
        } finally {
            isUpdating = false;
        }
    }

    private void openUpdatedInventory(Player player, ItemStack itemAtCursor) {
        var view = player.openInventory(this.inventory);

        if (itemAtCursor != null) {
            if (view != null && !itemAtCursor.getType().isAir()) {
                player.getInventory().removeItem(itemAtCursor);
                view.setCursor(itemAtCursor);
                player.updateInventory();
            }
        }
    }

    public void shiftItemToInventory(Inventory sourceInventory, Inventory targetInventory, int sourceSlot, Set<Integer> blockedSlots) {
        int targetSlot = 0;

        ItemStack itemStack = sourceInventory.getItem(sourceSlot);

        // Überprüfe, ob das Item im Quell-Slot vorhanden ist
        if (itemStack == null) {
            return; // Beende die Funktion, wenn kein Item im Quell-Slot ist
        }

        // Überprüfe, ob alle Slots blockiert sind
        boolean allSlotsBlocked = true;
        for (int i = 0; i < targetInventory.getSize(); i++) {
            if (!blockedSlots.contains(i)) {
                allSlotsBlocked = false;
                break;
            }
        }

        if (allSlotsBlocked)
            return; // Beende die Funktion, wenn alle Slots blockiert sind

        // Iteriere über alle Slots im Ziel-Inventar
        for (int i = 0; i < targetInventory.getSize(); i++) {
            // Überprüfe, ob der aktuelle Slot blockiert ist
            if (blockedSlots.contains(i))
                continue; // Überspringe den aktuellen Slot und gehe zum nächsten

            ItemStack currentSlotItem = targetInventory.getItem(i);

            // Überprüfe, ob der aktuelle Slot leer ist
            if (currentSlotItem == null) {
                targetInventory.setItem(i, itemStack);
                sourceInventory.setItem(sourceSlot, null); // Entferne das Item aus dem Quell-Inventar
                return; // Das Item wurde verschoben, beende die Funktion
            }

            // Überprüfe, ob das Item im aktuellen Slot vom gleichen Typ und stapelbar ist
            if (currentSlotItem.isSimilar(itemStack) && currentSlotItem.getAmount() < currentSlotItem.getMaxStackSize()) {
                int spaceLeft = currentSlotItem.getMaxStackSize() - currentSlotItem.getAmount();
                int amountToMove = Math.min(spaceLeft, itemStack.getAmount());

                currentSlotItem.setAmount(currentSlotItem.getAmount() + amountToMove);
                itemStack.setAmount(itemStack.getAmount() - amountToMove);

                if (itemStack.getAmount() == 0) {
                    sourceInventory.setItem(sourceSlot, null); // Entferne das Item aus dem Quell-Inventar
                    return; // Das gesamte Item wurde verschoben, beende die Funktion
                }
            }

            targetSlot++;
        }

        // Falls alle Slots blockiert waren oder kein passender Slot gefunden wurde,
        // wird das Item nicht verschoben
    }
}

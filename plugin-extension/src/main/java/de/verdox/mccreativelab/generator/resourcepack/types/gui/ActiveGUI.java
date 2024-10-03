package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.OnlyServerSoftware;
import de.verdox.mccreativelab.event.GUICloseEvent;
import de.verdox.mccreativelab.event.GUIOpenEvent;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.element.active.ActiveGUIElement;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.util.player.fakeinv.FakeInventory;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ActiveGUI extends ActiveComponentRendered<ActiveGUI, CustomGUIBuilder> {
    public static final boolean NEW_GUI_UPDATE_STRATEGY = false;
    private final Consumer<ActiveGUI> initialSetup;
    private long lastShift = System.currentTimeMillis();
    private static final long SHIFT_COOLDOWN_MILLIS = 20;

    private final Map<String, ActiveGUIElement<?>> activeGUIElements = new HashMap<>();
    private final Map<Integer, ActiveGUIElement<?>> guiElementsBySlot = new HashMap<>();

    private ScheduledTask updateTask;
    private final FrontEndListener frontEndListener = new FrontEndListener();
    private FrontEndRenderer frontEndRenderer;

    private final AtomicBoolean isUpdating = new AtomicBoolean();
    private final AtomicReference<Inventory> inventory = new AtomicReference<>();

    private final Map<Integer, ClickableItem> indexToClickableItemMapping = new HashMap<>();

    private final Set<Player> inventoryUpdateWhitelist = new HashSet<>();

    public ActiveGUI(CustomGUIBuilder customGUIBuilder, @Nullable Consumer<ActiveGUI> initialSetup) {
        super(customGUIBuilder);
        if (!customGUIBuilder.isInstalled())
            throw new IllegalArgumentException("The custom gui " + customGUIBuilder.getKey().asString() + " was not registered to the MCCreativeLab custom resource pack.");


        this.initialSetup = initialSetup;

        customGUIBuilder.guiElements.forEach((s, guiElement) -> {
            var activeElement = guiElement.toActiveElement(this);
            activeGUIElements.put(s, activeElement);
        });


        this.inventory.set(createInventory(render()));

        if (initialSetup != null) {
            initialSetup.accept(this);
            forEachElementBehavior((activeGUIRenderedRenderedElementBehavior, rendered) -> activeGUIRenderedRenderedElementBehavior.onOpen(this, rendered), true);
            forEachGUIElementBehavior((guiElementBehavior, activeGUIElement) -> guiElementBehavior.onOpen(this, activeGUIElement));
        }
    }


    /**
     * This constructor is only available for the ServerSoftware since with MCCreativeLab we are able to reopen an existing inventory with a new title without creating a new inventory object.
     *
     * @param customGUIBuilder The customGUIBuilder this belongs to
     * @param inventory        The inventory that is linked to this CustomGUI
     * @param initialSetup     The initial setup
     */
    @OnlyServerSoftware
    public ActiveGUI(CustomGUIBuilder customGUIBuilder, Inventory inventory, @Nullable Consumer<ActiveGUI> initialSetup) {
        super(customGUIBuilder);
        MCCreativeLabExtension.needsServerSoftware();
        if ((customGUIBuilder.getType() != null && !inventory.getType().equals(customGUIBuilder.getType()) || customGUIBuilder.getChestSize() != inventory.getSize()))
            throw new IllegalArgumentException("Trying to link an existing inventory with type " + customGUIBuilder.getType() + " and size " + customGUIBuilder.getChestSize() + " to existing inventory with type " + inventory.getType() + " and size " + inventory.getSize());
        if (!customGUIBuilder.isInstalled())
            throw new IllegalArgumentException("The custom gui " + customGUIBuilder.getKey().asString() + " was not registered to the MCCreativeLab custom resource pack.");
        this.initialSetup = initialSetup;

        customGUIBuilder.guiElements.forEach((s, guiElement) -> {
            var activeElement = guiElement.toActiveElement(this);
            activeGUIElements.put(s, activeElement);
        });


        this.inventory.set(inventory);

        if (initialSetup != null) {
            initialSetup.accept(this);
            forEachElementBehavior((activeGUIRenderedRenderedElementBehavior, rendered) -> activeGUIRenderedRenderedElementBehavior.onOpen(this, rendered), true);
            forEachGUIElementBehavior((guiElementBehavior, activeGUIElement) -> guiElementBehavior.onOpen(this, activeGUIElement));
        }
    }

    private void startFrontEnd(CustomGUIBuilder customGUIBuilder) {
        if (updateTask != null && updateTask.getExecutionState().equals(ScheduledTask.ExecutionState.RUNNING))
            return;

        Bukkit.getPluginManager().registerEvents(frontEndListener, MCCreativeLabExtension.getInstance());

        frontEndRenderer = new FrontEndRenderer();
        frontEndRenderer.start();

        updateTask = Bukkit.getAsyncScheduler().runAtFixedRate(MCCreativeLabExtension.getInstance(), scheduledTask -> {
            synchronized (viewers) {
                if (viewers.isEmpty()) {
                    scheduledTask.cancel();
                    HandlerList.unregisterAll(frontEndListener);
                    frontEndRenderer.stopRenderer();
                    //Bukkit.getLogger().info("Stopping dynamic frontend renderer for gui " + getComponentRendered().getKey().asString());
                    return;
                }
            }
            if (customGUIBuilder.updateInterval > 0)
                forceUpdate();
        }, 0, customGUIBuilder.updateInterval > 0 ? customGUIBuilder.updateInterval * 50L : 1, TimeUnit.MILLISECONDS);

        if (customGUIBuilder.updateInterval < 0) {
            Bukkit.getAsyncScheduler().runNow(MCCreativeLabExtension.getInstance(), scheduledTask -> {
/*                if (customGUIBuilder.whileOpen != null) customGUIBuilder.whileOpen.accept(this);
                forEachElementBehavior((activeGUIRenderedRenderedElementBehavior, rendered) -> activeGUIRenderedRenderedElementBehavior.whileOpen(this, rendered));
                forEachGUIElementBehavior((guiElementBehavior, activeGUIElement) -> guiElementBehavior.whileOpen(this, activeGUIElement));*/
                forceUpdate();
            });
        }


/*        if (customGUIBuilder.updateInterval > 0) {

            //Bukkit.getLogger().info("Starting dynamic frontend renderer for gui " + getComponentRendered().getKey().asString());
            updateTask = Bukkit.getAsyncScheduler().runAtFixedRate(MCCreativeLabExtension.getInstance(), scheduledTask -> {
                synchronized (viewers) {
                    if (viewers.isEmpty()) {
                        scheduledTask.cancel();
                        HandlerList.unregisterAll(frontEndListener);
                        frontEndRenderer.stopRenderer();
                        //Bukkit.getLogger().info("Stopping dynamic frontend renderer for gui " + getComponentRendered().getKey().asString());
                        return;
                    }
                }
*//*                if (customGUIBuilder.whileOpen != null) customGUIBuilder.whileOpen.accept(this);
                forEachElementBehavior((activeGUIRenderedRenderedElementBehavior, rendered) -> activeGUIRenderedRenderedElementBehavior.whileOpen(this, rendered));
                forEachGUIElementBehavior((guiElementBehavior, activeGUIElement) -> guiElementBehavior.whileOpen(this, activeGUIElement));*//*
                forceUpdate();
            }, 0, customGUIBuilder.updateInterval * 50L, TimeUnit.MILLISECONDS);
        } else {
            //Bukkit.getLogger().info("Rendering static gui frontend " + getComponentRendered().getKey().asString());
            Bukkit.getAsyncScheduler().runNow(MCCreativeLabExtension.getInstance(), scheduledTask -> {
*//*                if (customGUIBuilder.whileOpen != null) customGUIBuilder.whileOpen.accept(this);
                forEachElementBehavior((activeGUIRenderedRenderedElementBehavior, rendered) -> activeGUIRenderedRenderedElementBehavior.whileOpen(this, rendered));
                forEachGUIElementBehavior((guiElementBehavior, activeGUIElement) -> guiElementBehavior.whileOpen(this, activeGUIElement));*//*
                forceUpdate();
            });
        }*/
    }

    public final void addClickableItem(int index, ClickableItem clickableItem) {
        this.getVanillaInventory().setItem(index, clickableItem.getStack());
        indexToClickableItemMapping.put(index, clickableItem);
    }

    public void openToPlayer(Player player) {
        if (!new GUIOpenEvent(player, this).callEvent())
            return;

        ActiveGUI currentActiveGUI = PlayerGUIData.getCurrentActiveGUI(player);
        if (currentActiveGUI != null) {
            if (currentActiveGUI.equals(this))
                return;

            //Bukkit.getLogger().info("Before opening " + getComponentRendered().getKey().asString() + " we must remove the player from " + currentActiveGUI.getComponentRendered().getKey().asString());
            currentActiveGUI.removePlayerFromGUI(player, InventoryCloseEvent.Reason.OPEN_NEW);
        }

        addPlayerToGUI(player);
    }

    private void addPlayerToGUI(Player player) {
        synchronized (viewers) {
            if (viewers.contains(player))
                return;
            //Bukkit.getLogger().info("Adding player " + player.getName() + " to gui " + getComponentRendered().getKey().asString());
            PlayerGUIData.trackCurrentActiveGUI(player, this);
            viewers.add(player);
        }
        startFrontEnd(getComponentRendered());
        if (getComponentRendered().onOpen != null) {
            getComponentRendered().onOpen.accept(this);
        }
    }

    private void removePlayerFromGUI(Player player, InventoryCloseEvent.Reason reason) {
        if (!new GUICloseEvent(player, this, reason).callEvent())
            return;
        synchronized (viewers) {
            //Bukkit.getLogger().info("Removing player " + player.getName() + " from gui " + getComponentRendered().getKey().asString());
            viewers.remove(player);


            if (this.equals(PlayerGUIData.getCurrentActiveGUI(player))) {
                PlayerGUIData.trackCurrentActiveGUI(player, null);
            }

            FakeInventory.stopFakeInventoryOfPlayer(player);
            player.updateInventory();
        }
    }

    public final void forEachGUIElementBehavior(BiConsumer<GUIElementBehavior<ActiveGUIElement<?>>, ActiveGUIElement<?>> forEach) {
        this.activeGUIElements.forEach((s, activeGUIElement) -> {
            GUIElementBehavior<ActiveGUIElement<?>> guiElementBehavior = (GUIElementBehavior<ActiveGUIElement<?>>) getComponentRendered()
                .getGuiElementBehaviors().getOrDefault(activeGUIElement.getGuiElement(), null);
            if (guiElementBehavior != null)
                forEach.accept(guiElementBehavior, activeGUIElement);
        });
        //forceUpdate();
    }

    public final <H extends ActiveGUIElement<?>> boolean editGUIElement(String id, Class<? extends H> type, Consumer<H> execution) {
        return edit(id, activeGUIElements, type, h -> {
            h.setVisible(true);
            execution.accept(h);
        });
    }

    void trackGUIInStack(Player player) {
        PlayerGUIStack.load(player).trackGUI(this);
    }

    public Inventory getVanillaInventory() {
        return inventory.get();
    }

    public void placeGuiElementInSlot(int slotIndex, @Nullable ActiveGUIElement<?> activeGUIElement) {
        if (activeGUIElement != null && !this.equals(activeGUIElement.getActiveGUI()))
            throw new IllegalArgumentException("Trying to add gui element that does not belong to this gui.");
        if (activeGUIElement != null)
            guiElementsBySlot.put(slotIndex, activeGUIElement);
        else
            guiElementsBySlot.remove(slotIndex);
    }

    public @Nullable ActiveGUIElement<?> getGUIElementAtIndex(int slotIndex) {
        return guiElementsBySlot.getOrDefault(slotIndex, null);
    }

    @Override
    protected void doUpdate() {
        frontEndRenderer.offer(() -> {
            if (getComponentRendered().whileOpen != null) getComponentRendered().whileOpen.accept(this);
            forEachElementBehavior((activeGUIRenderedRenderedElementBehavior, rendered) -> activeGUIRenderedRenderedElementBehavior.whileOpen(this, rendered), false);
            forEachGUIElementBehavior((guiElementBehavior, activeGUIElement) -> guiElementBehavior.whileOpen(this, activeGUIElement));

            isUpdating.set(true);
            try {
                int viewerCount;
                synchronized (viewers) {
                    viewerCount = ActiveGUI.this.viewers.size();
                }
                if (viewerCount == 0)
                    return;

                //Bukkit.getLogger().info("[Renderer - " + ActiveGUI.this.getComponentRendered().getKey().asString() + "] Rendering for " + viewerCount);
                if (NEW_GUI_UPDATE_STRATEGY) {
                    Inventory newInventory = null;
                    if (ActiveGUI.this.inventory.get() == null) {
                        newInventory = createInventory(render());
                        ActiveGUI.this.inventory.set(newInventory);
                    }

                    for (HumanEntity viewer : this.inventory.get().getViewers()) {
                        sendNewTitle(viewer.getOpenInventory(), render());
                    }
                } else {
                    Component rendering = render();
                    // If we don't use the MCCLab Server Software we need to recreate the inventory every time.
                    Inventory newInventory = null;
                    if (ActiveGUI.this.inventory.get() == null || !MCCreativeLabExtension.isServerSoftware()) {
                        newInventory = createInventory(rendering);

                        if (ActiveGUI.this.inventory.get() != null)
                            newInventory.setContents(ActiveGUI.this.inventory.get().getContents());
                    }

                    // If we have a newInventory replace the old one with the new one.
                    if (newInventory != null)
                        ActiveGUI.this.inventory.set(newInventory);


                    //this.inventory.getViewers().get(0).getOpenInventory().setTitle();

                    CompletableFuture<Void> waitForSync = new CompletableFuture<>();


                    Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), () -> {
                        synchronized (viewers) {
                            Iterator<Player> iterator = viewers.iterator();

                            while (iterator.hasNext()) {
                                Player player = iterator.next();
                                if (!ActiveGUI.this.equals(PlayerGUIData.getCurrentActiveGUI(player))) {
                                    iterator.remove();
                                    continue;
                                }
                                player.getOpenInventory().getCursor();
                                var itemAtCursor = player.getOpenInventory().getCursor().clone();
                                openUpdatedInventory(player, itemAtCursor);
                            }
                        }
                        waitForSync.complete(null);
                    });
                    waitForSync.join();
                }
            } finally {
                isUpdating.set(false);
            }
        });
    }

    private @NotNull Inventory createInventory(Component rendering) {
        Inventory newInventory;
        if (getComponentRendered().getType() != null) {
            newInventory = Bukkit.createInventory(new ActiveGUIHolder(this), getComponentRendered().getType(), rendering);
        } else
            newInventory = Bukkit.createInventory(new ActiveGUIHolder(this), getComponentRendered().getChestSize() * 9, rendering);
        return newInventory;
    }

    private void openUpdatedInventory(Player player, ItemStack itemAtCursor) {
        synchronized (viewers) {
            if (!viewers.contains(player) && !FakeInventory.hasFakeInventory(player) && getComponentRendered().isUsePlayerSlots())
                FakeInventory.setFakeInventoryOfPlayer(player);
        }

        InventoryView view;
        synchronized (inventoryUpdateWhitelist) {
            inventoryUpdateWhitelist.add(player);


            if (MCCreativeLabExtension.isServerSoftware())
                view = player.openInventory(this.inventory.get(), render());
            else view = player.openInventory(this.inventory.get());
            if (itemAtCursor != null) {
                if (view != null && !itemAtCursor.getType().isAir() && !getComponentRendered().isUsePlayerSlots()) {
                    player.getInventory().removeItem(itemAtCursor);
                    view.setCursor(itemAtCursor);
                    //player.updateInventory();
                }
            }
            viewers.add(player);
            inventoryUpdateWhitelist.remove(player);

        }
    }

    private void shiftItemToInventory(Inventory sourceInventory, Inventory targetInventory, int sourceSlot, Set<Integer> blockedSlots) {
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

    @Override
    public Set<Player> getViewers() {
        synchronized (viewers) {
            return super.getViewers();
        }
    }

    private class FrontEndListener implements Listener {
        @EventHandler
        private void onClick(InventoryClickEvent e) {
            try {
                Player player = (Player) e.getWhoClicked();
                synchronized (viewers) {
                    if (!viewers.contains((Player) e.getWhoClicked()))
                        return;
                }

                if (indexToClickableItemMapping.containsKey(e.getRawSlot())) {
                    e.setCancelled(true);

                    ClickableItem clickableItem = indexToClickableItemMapping.get(e.getRawSlot());
                    clickableItem.getOnClick().accept(e, ActiveGUI.this);

                    if (clickableItem.getBuilder().clearGUIStackAndClose) {
                        PlayerGUIStack.load(player).clear();
                        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    } else if (clickableItem.getBuilder().popGUIStack) {
                        PlayerGUIStack.load(player).popAndOpenLast(player, ActiveGUI.this);
                        //player.closeInventory(InventoryCloseEvent.Reason.PLAYER);
                    }

                    if (getComponentRendered().clickConsumer != null)
                        getComponentRendered().clickConsumer.accept(e, ActiveGUI.this);

                    return;
                }


                ActiveGUIElement<?> activeGUIElement = guiElementsBySlot.getOrDefault(e.getRawSlot(), null);


                if (getComponentRendered().isSlotBlocked(e.getRawSlot()) && !e.getView().getBottomInventory().equals(e.getClickedInventory())) {
                    e.setCancelled(true);
                    if (activeGUIElement != null)
                        activeGUIElement.onClick(e, e.getRawSlot() % 9, e.getRawSlot() / 9);
                }
                // Prevent inventory clicks if is using player slots
                if (getComponentRendered().isUsePlayerSlots() && Objects.equals(e.getClickedInventory(), e.getView().getBottomInventory()))
                    e.setCancelled(true);

                if (e.isShiftClick() && e.getView().getBottomInventory().equals(e.getClickedInventory())) {
                    if (getComponentRendered().isUsePlayerSlots() || System.currentTimeMillis() - lastShift < SHIFT_COOLDOWN_MILLIS) {
                        e.setCancelled(true);
                        return;
                    }
                    lastShift = System.currentTimeMillis();

                    shiftItemToInventory(e.getView()
                        .getBottomInventory(), ActiveGUI.this.inventory.get(), e.getSlot(), getComponentRendered().getBlockedSlots());
                    e.setCancelled(true);
                }

                if (getComponentRendered().clickConsumer != null)
                    getComponentRendered().clickConsumer.accept(e, ActiveGUI.this);
            } finally {
                forceUpdate();
            }
        }

        @EventHandler
        private void onClick(InventoryDragEvent e) {
            synchronized (viewers) {
                if (!viewers.contains((Player) e.getWhoClicked()))
                    return;
            }
            var rawSlotUsed = e.getRawSlots().stream().anyMatch(getComponentRendered()::isSlotBlocked);
            if (rawSlotUsed)
                e.setCancelled(true);
        }

        @EventHandler
        private void onClose(InventoryCloseEvent e) {
            synchronized (viewers) {
                if (!viewers.contains((Player) e.getPlayer()))
                    return;
            }

            // A player is in this update whitelist if we reopen the updated inventory to the player
            // Since this forced InventoryCloseEvent should not be
            synchronized (inventoryUpdateWhitelist) {
                if (inventoryUpdateWhitelist.contains((Player) e.getPlayer()))
                    return;
            }
            removePlayerFromGUI((Player) e.getPlayer(), e.getReason());
        }
    }

    private class FrontEndRenderer extends Thread {
        private final LinkedBlockingQueue<Runnable> updateQueue = new LinkedBlockingQueue<>();
        private boolean running = true;

        public void stopRenderer() {
            this.running = false;
        }

        public void offer(Runnable runnable) {
            updateQueue.offer(runnable);
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Runnable update = updateQueue.take();
                    update.run();
                }
            } catch (Throwable e) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occured while rendering the active gui " + getComponentRendered().key().asString(), e);
            }
        }
    }

    private static class PlayerGUIData {
        public static void trackCurrentActiveGUI(Player player, @Nullable ActiveGUI activeGUI) {
            if (activeGUI != null) {
                player.setMetadata("active_gui", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), activeGUI));
            } else {
                player.removeMetadata("active_gui", MCCreativeLabExtension.getInstance());
            }
        }

        @Nullable
        public static ActiveGUI getCurrentActiveGUI(Player player) {
            if (!player.hasMetadata("active_gui"))
                return null;
            return (ActiveGUI) player.getMetadata("active_gui").get(0).value();
        }
    }

    private record ActiveGUIHolder(ActiveGUI activeGUI) implements InventoryHolder {

        @Override
        public @NotNull Inventory getInventory() {
            return activeGUI.inventory.get();
        }
    }

    public void sendNewTitle(InventoryView view, Component component) {
        view.setTitle(ThreadLocalRandom.current().nextInt(1000) + "");
/*        final net.minecraft.server.level.ServerPlayer entityPlayer = (net.minecraft.server.level.ServerPlayer) ((CraftHumanEntity) view.getPlayer()).getHandle();
        final int containerId = entityPlayer.containerMenu.containerId;
        final net.minecraft.world.inventory.MenuType<?> windowType = CraftContainer.getNotchInventoryType(view.getTopInventory());
        entityPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundOpenScreenPacket(containerId, windowType, PaperAdventure.asVanilla(component)));
        ((Player) view.getPlayer()).updateInventory();*/
    }

    private MenuType toMenuType(InventoryType inventoryType) {
        return switch (inventoryType) {
            case LOOM -> MenuType.LOOM;
            case ANVIL -> MenuType.ANVIL;
            case BARREL, CHEST, SHULKER_BOX, ENDER_CHEST -> MenuType.GENERIC_9X3;
            case BEACON -> MenuType.BEACON;
            case BREWING -> MenuType.BREWING_STAND;
            case BLAST_FURNACE -> MenuType.BLAST_FURNACE;
            case CARTOGRAPHY -> MenuType.CARTOGRAPHY_TABLE;
            case CRAFTER -> MenuType.CRAFTER_3X3;
            case DISPENSER, DROPPER -> MenuType.GENERIC_3X3;
            case ENCHANTING -> MenuType.ENCHANTMENT;
            case FURNACE -> MenuType.FURNACE;
            case GRINDSTONE -> MenuType.GRINDSTONE;
            case HOPPER -> MenuType.HOPPER;
            case MERCHANT -> MenuType.MERCHANT;
            case SMOKER -> MenuType.SMOKER;
            case STONECUTTER -> MenuType.STONECUTTER;
            case PLAYER -> MenuType.GENERIC_9X4;
            case SMITHING, SMITHING_NEW -> MenuType.SMITHING;
            case WORKBENCH -> MenuType.CRAFTING;
            case CHISELED_BOOKSHELF, COMPOSTER, CREATIVE, CRAFTING, DECORATED_POT, JUKEBOX, LECTERN ->
                throw new IllegalStateException(inventoryType + " cannot be instantiated as GUI");
        };
    }
}

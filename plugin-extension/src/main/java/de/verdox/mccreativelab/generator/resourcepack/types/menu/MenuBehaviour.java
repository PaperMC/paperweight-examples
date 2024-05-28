package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.events.PlayerMenuCloseEvent;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.events.PlayerMenuOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class MenuBehaviour implements Listener {
    private static final int tickCooldown = 3;
    private final Player player;
    private final ActiveMenu activeMenu;
    private final BiConsumer<PlayerKeyInput, ActiveMenu> consumer;
    private final JavaPlugin platformPlugin;
    private final Runnable onEnd;
    private BukkitTask posUpdaterTask;
    private BukkitTask effectTask;
    private Location locationBefore;
    private int lastTickButtonPressed;
    private int lastScrollTick;
    private int currentScrollMode = 0;
    private int heldSlotBefore;

    public MenuBehaviour(JavaPlugin platformPlugin, Player player, ActiveMenu activeMenu, BiConsumer<PlayerKeyInput, ActiveMenu> consumer, Runnable onEnd) {
        this.platformPlugin = platformPlugin;
        this.onEnd = onEnd;
        Bukkit.getPluginManager().registerEvents(this, platformPlugin);
        this.player = player;
        this.activeMenu = activeMenu;
        this.consumer = consumer;
    }

    public void start() {
        //player.doInventorySynchronization(false);
        ItemStack[] fakeContents = new ItemStack[46];

        heldSlotBefore = player.getInventory().getHeldItemSlot();

        Bukkit.getPluginManager().callEvent(new PlayerMenuOpenEvent(player, activeMenu));

        locationBefore = player.getLocation().clone();

        if (activeMenu.getCustomMenu().doFakeTime)
            player.setPlayerTime(6000, false);
        if (activeMenu.getCustomMenu().doFakeWeather)
            player.setPlayerWeather(WeatherType.CLEAR);
        player.setMetadata("hasMenuOpen", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), false));

        Location locationOnOpen = getLocationOnOpen();


        this.posUpdaterTask = Bukkit.getScheduler().runTaskTimer(platformPlugin, () -> {
            player.getInventory().setHeldItemSlot(4);
            fakeContents[45] = activeMenu.getActiveBackgroundPicture();
            player.sendFakeInventoryContents(fakeContents);

            if(!activeMenu.getCustomMenu().doYawPitchLock && !activeMenu.getCustomMenu().doPositionLoc)
                return;

            if (player.getLocation().getYaw() == 0 && player.getLocation().getPitch() == -90 && activeMenu.getCustomMenu().doYawPitchLock)
                return;

            if(locationOnOpen != null)
                player.teleport(locationOnOpen);
        }, 0L, 1L);

        if (activeMenu.getCustomMenu().doEffects) {
            this.effectTask = Bukkit.getScheduler().runTaskTimer(platformPlugin, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 3, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, -1, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1, false, false, false));
            }, 0L, 20L);
        }
    }

    private @Nullable Location getLocationOnOpen() {
        Location location = null;

        if (activeMenu.getCustomMenu().doYawPitchLock)
            location = new Location(player.getLocation().getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getY(), player.getLocation().getBlockZ() + 0.5, 0, -90);
        else if(activeMenu.getCustomMenu().doPositionLoc)
            location = new Location(player.getLocation().getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getY(), player.getLocation().getBlockZ() + 0.5, player.getYaw(), player.getPitch());
        return location;
    }

    public void close() {

        player.resetPlayerTime();
        player.resetPlayerWeather();

        Bukkit.getPluginManager().callEvent(new PlayerMenuCloseEvent(player, activeMenu));

        player.getInventory().setHeldItemSlot(heldSlotBefore);

        //player.doInventorySynchronization(true);
        player.updateInventory();

        HandlerList.unregisterAll(this);
        if (posUpdaterTask != null)
            posUpdaterTask.cancel();
        if (effectTask != null)
            effectTask.cancel();
        player.teleport(locationBefore);
        if (onEnd != null)
            onEnd.run();

        player.removeMetadata("hasMenuOpen", MCCreativeLabExtension.getInstance());
    }

    private boolean isRightPlayer(Player player) {
        return player.getUniqueId().equals(this.player.getUniqueId());
    }

    private boolean isRightPlayer(Entity player) {
        return player.getUniqueId().equals(this.player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onQuit(PlayerQuitEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;

        close();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDamage(EntityDamageByBlockEvent e) {
        if (!isRightPlayer(e.getEntity()))
            return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDamage(EntityDamageByEntityEvent e) {
        if (!isRightPlayer(e.getEntity()))
            return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() == null || !isRightPlayer(e.getTarget()))
            return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onQuit(PlayerKickEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        close();
    }

    @EventHandler
    private void pickupItem(EntityPickupItemEvent e) {
        if (!isRightPlayer(e.getEntity()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (!isRightPlayer((Player) e.getWhoClicked()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onInventoryClick(InventoryInteractEvent e) {
        if (!isRightPlayer((Player) e.getWhoClicked()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onSwap(PlayerSwapHandItemsEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    private void playerHeldItemEvent(PlayerItemHeldEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        e.setCancelled(true);

        var difference = Bukkit.getCurrentTick() - lastScrollTick;
        lastScrollTick = Bukkit.getCurrentTick();
        if (difference <= 2) {
            if (currentScrollMode == 1)
                consumer.accept(PlayerKeyInput.SCROLL_UP, activeMenu);
            else if (currentScrollMode == -1)
                consumer.accept(PlayerKeyInput.SCROLL_DOWN, activeMenu);
        } else {
            var slotDifference = e.getNewSlot() - e.getPreviousSlot();

            if (slotDifference >= 0 && slotDifference <= 3) {
                consumer.accept(PlayerKeyInput.SCROLL_DOWN, activeMenu);
                currentScrollMode = -1;
            } else if (slotDifference <= 0 && slotDifference >= -3) {
                consumer.accept(PlayerKeyInput.SCROLL_UP, activeMenu);
                currentScrollMode = 1;
            } else
                currentScrollMode = 0;
        }


    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
            triggerKeyInput(PlayerKeyInput.LEFT_CLICK, e);
        else if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            triggerKeyInput(PlayerKeyInput.RIGHT_CLICK, e);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        close();
    }

    @EventHandler
    private void onJump(PlayerJumpEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        triggerKeyInput(PlayerKeyInput.SPACE, e);
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (!isRightPlayer(e.getPlayer()))
            return;
        var direction = e.getTo().clone().add(-e.getFrom().getX(), -e.getFrom().getY(), -e.getFrom().getZ());

        var currentTick = Bukkit.getServer().getCurrentTick();
        e.setTo(new Location(e.getFrom().getWorld(), e.getFrom().getX(), e.getTo().getY(), e.getFrom().getZ(), e
            .getFrom().getYaw(), e.getFrom().getPitch()));

        var lastTick = currentTick - lastTickButtonPressed;
        lastTickButtonPressed = currentTick;

        if (lastTick < tickCooldown)
            return;


        if (direction.getX() > 0)
            triggerKeyInput(PlayerKeyInput.A, e);
        if (direction.getX() < 0)
            triggerKeyInput(PlayerKeyInput.D, e);
        if (direction.getZ() > 0)
            triggerKeyInput(PlayerKeyInput.W, e);
        if (direction.getZ() < 0)
            triggerKeyInput(PlayerKeyInput.S, e);
    }

    private void triggerKeyInput(PlayerKeyInput playerKeyInput, Cancellable cancellable) {
        if (activeMenu.getCustomMenu().getCancelledGameInputs().contains(playerKeyInput))
            cancellable.setCancelled(true);
        consumer.accept(playerKeyInput, activeMenu);
    }

}

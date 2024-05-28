package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.ActiveHud;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group.Button;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group.HudMultiLineText;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudTexture;
import io.vertx.core.impl.ConcurrentHashSet;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ActiveMenu {
    public static final Map<UUID, ActiveMenu> activeMenus = new HashMap<>();
    private final Player player;
    private final CustomMenu customMenu;
    private final MenuBehaviour behaviour;
    private final Set<MenuState> enabledStates = new ConcurrentHashSet<>();
    private ItemStack activeBackgroundPicture;

    ActiveMenu(Player player, CustomMenu customMenu) {
        this.player = player;
        this.customMenu = customMenu;
        closeActiveMenu(player);
        behaviour = new MenuBehaviour(MCCreativeLabExtension.getInstance(), player, this, this::execute, this::onEnd);
        behaviour.start();
        if (customMenu.getMenuHud() != null)
            MCCreativeLabExtension.getHudRenderer().getOrStartActiveHud(player, customMenu.getMenuHud());
        if (activeMenus.containsKey(player.getUniqueId())) {
            activeMenus.get(player.getUniqueId()).close();
        }
        activeMenus.put(player.getUniqueId(), this);
        hideEverything();
        customMenu.getStates().forEach((s, menuState) -> {
            if (!menuState.isVisibleOnOpen())
                return;
            this.enableState(s);
        });
    }

    ItemStack getActiveBackgroundPicture() {
        return activeBackgroundPicture;
    }

    public static void closeActiveMenu(Player player) {
        var activeMenu = activeMenus.getOrDefault(player.getUniqueId(), null);
        if (activeMenu == null)
            return;
        activeMenu.close();
    }

    public static boolean hasActiveMenu(Player player){
        return activeMenus.containsKey(player.getUniqueId());
    }

    public CustomMenu getCustomMenu() {
        return customMenu;
    }

    public Player getPlayer() {
        return player;
    }

    public void close() {
        player.stopSound(SoundCategory.MASTER);
        behaviour.close();
    }

    @Nullable
    public ActiveHud getActiveHud() {
        if (customMenu.getMenuHud() != null)
            return MCCreativeLabExtension.getHudRenderer().getOrStartActiveHud(player, customMenu.getMenuHud());
        return null;
    }

    public void changeButton(String id, Consumer<Button.RenderedButton> consumer) {
                if (getActiveHud() == null)
            return;
        getActiveHud().editRenderedElement(id, Button.RenderedButton.class, consumer);
    }

    public void changeText(String id, Consumer<SingleHudText.RenderedSingleHudText> consumer) {
        if (getActiveHud() == null)
            return;
        getActiveHud().editRenderedElement(id, SingleHudText.RenderedSingleHudText.class, consumer);
    }

    public void changeMultiLineText(String id, Consumer<HudMultiLineText.RenderedGroupMultiLineText> consumer) {
        if (getActiveHud() == null)
            return;
        getActiveHud().editRenderedElement(id, HudMultiLineText.RenderedGroupMultiLineText.class, consumer);
    }

    public void changeTexture(String id, Consumer<SingleHudTexture.RenderedSingleHudTexture> consumer) {
        if (getActiveHud() == null)
            return;
        getActiveHud().editRenderedElement(id, SingleHudTexture.RenderedSingleHudTexture.class, consumer);
    }

    public void setBackgroundPicture(String id) {
        if (!customMenu.getBackgroundPictures().containsKey(id))
            throw new IllegalArgumentException("No background picture found with id " + id);

        ItemStack newBackgroundPicture = customMenu.getBackgroundPictures().get(id).createItem();
        if(activeBackgroundPicture != null && activeBackgroundPicture.equals(newBackgroundPicture))
            return;

        activeBackgroundPicture = newBackgroundPicture;
        updateBackgroundPicture();
    }

    private void updateBackgroundPicture() {
    }

    public void clearBackgroundPicture() {
        activeBackgroundPicture = null;
    }

    public void enableState(String id) {
        if (id == null)
            return;
        if (!customMenu.getStates().containsKey(id))
            throw new NullPointerException("state id " + id + " does not exist");
        var state = customMenu.getStates().get(id);
        enableState(state);
        MCCreativeLabExtension.getHudRenderer().forceUpdate(player);

    }

    public void disableState(String id) {
        if (id == null)
            return;
        if (!customMenu.getStates().containsKey(id))
            throw new NullPointerException("state id " + id + " does not exist");
        var state = customMenu.getStates().get(id);
        disableState(state);
        updateBackgroundPicture();
        MCCreativeLabExtension.getHudRenderer().forceUpdate(player);
    }

    private void enableState(MenuState state) {
        state.getOnEnableState().accept(this);
        enabledStates.add(state);
        updateBackgroundPicture();
        MCCreativeLabExtension.getHudRenderer().forceUpdate(player);
    }

    private void disableState(MenuState state) {
        enabledStates.remove(state);
        state.getOnDisableState().accept(this);
        updateBackgroundPicture();
        MCCreativeLabExtension.getHudRenderer().forceUpdate(player);
    }

    private void execute(PlayerKeyInput keyInput, ActiveMenu activeMenu) {
        new HashSet<>(enabledStates).forEach(menuState -> menuState.getMenuOperationsOnKey(keyInput)
                                                                   .forEach(consumer -> consumer.accept(activeMenu)));
        updateBackgroundPicture();
        MCCreativeLabExtension.getHudRenderer().forceUpdate(player);
    }

    private void onEnd() {
        if (this.customMenu.getOnClose() != null)
            this.customMenu.getOnClose().accept(this);
        hideEverything();
        if (customMenu.getMenuHud() != null)
            MCCreativeLabExtension.getHudRenderer().stopActiveHud(player, customMenu.getMenuHud());
        activeMenus.remove(player.getUniqueId());
    }

    private void hideEverything() {
        clearBackgroundPicture();
        for (MenuState menuState : enabledStates)
            disableState(menuState);
        var activeHud = getActiveHud();
        if (activeHud == null)
            return;
        activeHud.hideAll();
        MCCreativeLabExtension.getHudRenderer().forceUpdate(player);
    }
}


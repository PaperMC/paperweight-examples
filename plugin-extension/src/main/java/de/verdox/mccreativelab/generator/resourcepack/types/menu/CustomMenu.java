package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomModelDataProvider;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CustomMenu extends ResourcePackResource {
    private Consumer<ActiveMenu> onClose;
    private final Map<String, ItemTextureData> backgroundPictures = new HashMap<>();
    private final Map<String, MenuState> states = new HashMap<>();
    private final Set<PlayerKeyInput> cancelledGameInputs = new HashSet<>();
    private boolean built = false;
    @Nullable
    private CustomHud menuHud;

    boolean doEffects = true;
    boolean doFakeWeather = true;
    boolean doFakeTime = true;
    boolean doYawPitchLock = true;
    boolean doPositionLoc = true;
    boolean hideOtherPlayers = false;


    public CustomMenu(@NotNull NamespacedKey namespacedKey) {
        super(namespacedKey);
    }

    public CustomMenu onClose(Consumer<ActiveMenu> onClose) {
        this.onClose = onClose;
        return this;
    }

    public CustomMenu createState(String id, Consumer<MenuState> setup) {
        return createState(id, true, setup);
    }

    public CustomMenu cancelGameActionOn(PlayerKeyInput playerKeyInput) {
        this.cancelledGameInputs.add(playerKeyInput);
        return this;
    }

    public CustomMenu hideOtherPlayers(boolean hideOtherPlayers) {
        this.hideOtherPlayers = hideOtherPlayers;
        return this;
    }

    public CustomMenu createState(String id, boolean visibleOnOpen, Consumer<MenuState> setup) {
        if (states.containsKey(id))
            throw new IllegalStateException("state id " + id + " already taken");
        var state = new MenuState(visibleOnOpen);
        setup.accept(state);
        states.put(id, state);
        return this;
    }

    public CustomMenu withBackgroundPicture(String id, Asset<CustomResourcePack> picture, Resolution resolution) {
        if (isBuilt()) {
            Bukkit.getLogger().warning("Can't modify a menu when it has been built");
            return this;
        }
        if (backgroundPictures.containsKey(id))
            throw new IllegalArgumentException("id " + id + " already taken");

        NamespacedKey backgroundPictureKey = new NamespacedKey(getKey().namespace(), "item/menu/"+getKey().value() + "/background/" + id);
        ItemTextureData.ModelType modelType = Resolution.createModel(resolution, backgroundPictureKey);
        ItemTextureData itemTextureData = new ItemTextureData(backgroundPictureKey, Material.FIREWORK_STAR, CustomModelDataProvider.drawCustomModelData(Material.FIREWORK_STAR), picture, modelType);
        backgroundPictures.put(id, itemTextureData);
        return this;
    }

    public CustomMenu setupCustomHud(Consumer<CustomHud> customHudSetup) {
        if (isBuilt()) {
            Bukkit.getLogger().warning("Can't modify a menu when it has been built");
            return this;
        }
        if (menuHud == null) {
            menuHud = new CustomHud(new NamespacedKey(getKey().namespace(), getKey().getKey() + "/hud"));
            MCCreativeLabExtension.getCustomResourcePack().register(menuHud);
        }
        customHudSetup.accept(menuHud);
        return this;
    }

    public boolean isBuilt() {
        return built;
    }

    public void build() {
        if (isBuilt())
            return;
        built = true;
    }

    public ActiveMenu createMenuForPlayer(Player player) {
        return new ActiveMenu(player, this);
    }

    CustomHud getMenuHud() {
        return menuHud;
    }

    Map<String, ItemTextureData> getBackgroundPictures() {
        return new HashMap<>(backgroundPictures);
    }

    Map<String, MenuState> getStates() {
        return states;
    }

    Consumer<ActiveMenu> getOnClose() {
        return onClose;
    }

    public Set<PlayerKeyInput> getCancelledGameInputs() {
        return cancelledGameInputs;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        backgroundPictures.forEach((s, itemTextureData) -> customPack.register(itemTextureData));
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        build();
    }

    public CustomMenu setDoEffects(boolean doEffects) {
        this.doEffects = doEffects;
        return this;
    }

    public CustomMenu setDoYawPitchLock(boolean doYawPitchLock) {
        this.doYawPitchLock = doYawPitchLock;
        return this;
    }

    public CustomMenu setDoPositionLoc(boolean doPositionLoc) {
        this.doPositionLoc = doPositionLoc;
        return this;
    }

    public CustomMenu setDoFakeWeather(boolean doFakeWeather) {
        this.doFakeWeather = doFakeWeather;
        return this;
    }

    public CustomMenu setDoFakeTime(boolean doFakeTime) {
        this.doFakeTime = doFakeTime;
        return this;
    }
}

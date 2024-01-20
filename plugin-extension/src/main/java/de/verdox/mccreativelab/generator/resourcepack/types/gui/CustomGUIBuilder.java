package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.renderer.TextType;
import de.verdox.mccreativelab.generator.resourcepack.types.CustomHud;
import de.verdox.mccreativelab.util.io.StringAlign;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CustomGUIBuilder extends CustomHud {
    private InventoryType type;
    Consumer<ActiveGUI> onOpen;
    Consumer<ActiveGUI> onClose;
    Consumer<ActiveGUI> whileOpen;
    int updateInterval = 20;
    TriConsumer<ClickableItem, InventoryClickEvent, ActiveGUI> clickConsumer;
    private final Set<Integer> blockedSlots = new HashSet<>();
    final Map<String, GUIElement.ClickableButton> clickableButtons = new HashMap<>();
    private int chestSize;
    private boolean allSlotsBlocked;
    private boolean usePlayerSlots;

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public CustomGUIBuilder(@NotNull NamespacedKey namespacedKey, @NotNull InventoryType type) {
        super(namespacedKey);
        this.type = type;
    }

    public CustomGUIBuilder(@NotNull NamespacedKey namespacedKey, int amountChestRows) {
        super(namespacedKey);
        this.chestSize = amountChestRows;
    }

    public CustomGUIBuilder withOverlayTexture(Asset<CustomResourcePack> texture) throws IOException {
        return withOverlayTexture(texture, getTopLeftPos());
    }

    public CustomGUIBuilder usePlayerSlots() {
        this.usePlayerSlots = true;
        return this;
    }

    public ScreenPosition getTopLeftPos() {
        if (type != null)
            return TextType.getTopLeftCorner(type);
        else
            return TextType.getTopLeftCorner(chestSize);
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        super.beforeResourceInstallation(customPack);
    }

    public CustomGUIBuilder withOverlayTexture(Asset<CustomResourcePack> texture, ScreenPosition screenPosition) throws IOException {
        return withTexture("overlay", texture,
                // 13 hoch / 8 nach links
                screenPosition);
    }

    //TODO: Beim erstellen von Objekten im CustomGUI muss man auswählen können ob man den Index im Spieler Inventar oder im GUI haben will
    // TOOD: Je nachdem wird dann automatisch die usePlayerSlots Flag auf true gesetzt

    public CustomGUIBuilder withClickableButton(String buttonName, StringAlign.Alignment textAlignment, float textScale, @Nullable Asset<CustomResourcePack> buttonTexture, int clickSizeX, int clickSizeY, int index, @Nullable Consumer<ItemMeta> metaSetup) throws IOException {

        var clickableItem = new ClickableItem.Builder()
            .withClickSize(clickSizeX, clickSizeY)
            .withItemMeta(metaSetup);

        var screenPosOfTexture = ScreenPosition.calculateTopLeftCornerOfInventorySlotIndex(index, getCorrectTextType());

        String textFieldID = "clickable_button_" + buttonName + "_text";
        withText(textFieldID, screenPosOfTexture.withLayer(3).addToXOffset(3)
                                                        .addToYOffset(-6), textAlignment, 0, textScale);
        String textureName = "clickable_button_" + buttonName + "_texture";
        withTexture(textureName, buttonTexture, screenPosOfTexture.withLayer(2));

        var button = new GUIElement.ClickableButton(clickableItem.build(), index, textureName, textFieldID);

        clickableButtons.put(buttonName, button);

        return this;
    }

    @Override
    public CustomGUIBuilder withButton(String buttonName, StringAlign.Alignment alignment, float textScale, @Nullable Asset<CustomResourcePack> whenSelected, @Nullable Asset<CustomResourcePack> whenEnabled, @Nullable Asset<CustomResourcePack> whenDisabled, ScreenPosition buttonPos, ScreenPosition textPos) throws IOException {
        return (CustomGUIBuilder) super.withButton(buttonName, alignment, textScale, whenSelected, whenEnabled, whenDisabled, convertScreenPosition(buttonPos), convertScreenPosition(textPos));
    }

    @Override
    public CustomGUIBuilder withTexture(String textureName, Asset<CustomResourcePack> textureAsset, ScreenPosition screenPosition) throws IOException {
        return (CustomGUIBuilder) super.withTexture(textureName, textureAsset, convertScreenPosition(screenPosition));
    }

    @Override
    public CustomHud withPartlyVisibleTexture(String textureField, ScreenPosition screenPosition, Asset<CustomResourcePack> texture, int parts) throws IOException {
        return super.withPartlyVisibleTexture(textureField, convertScreenPosition(screenPosition), texture, parts);
    }

    @Override
    public CustomGUIBuilder withMultiLineText(String multiLineID, int lines, int charsPerLine, int pixelsBetweenLines, StringAlign.Alignment alignment, ScreenPosition startPos, float scale) {
        return (CustomGUIBuilder) super.withMultiLineText(multiLineID, lines, charsPerLine, pixelsBetweenLines, alignment, convertScreenPosition(startPos), scale);
    }

    @Override
    public CustomGUIBuilder withText(String textFieldID, ScreenPosition screenPosition, StringAlign.Alignment alignment, int pixelAlignmentWidth, float scale) {
        return (CustomGUIBuilder) super.withText(textFieldID, convertScreenPosition(screenPosition), alignment, pixelAlignmentWidth, scale);
    }

    public CustomGUIBuilder onOpen(Consumer<ActiveGUI> onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    public CustomGUIBuilder whileOpen(Consumer<ActiveGUI> whileOpen) {
        this.whileOpen = whileOpen;
        return this;
    }

    public CustomGUIBuilder onClose(Consumer<ActiveGUI> onClose) {
        this.onClose = onClose;
        return this;
    }

    public CustomGUIBuilder withClick(TriConsumer<ClickableItem, InventoryClickEvent, ActiveGUI> clickConsumer) {
        this.clickConsumer = clickConsumer;
        return this;
    }

    public CustomGUIBuilder withBlockedSlots(int... blockedSlots) {
        for (int blockedSlot : blockedSlots)
            this.blockedSlots.add(blockedSlot);
        return this;
    }

    public CustomGUIBuilder withBlockedPattern(int startRow, int endRow, int... blockedSlots) {
        for (int i = startRow; i <= endRow; i++)
            for (int blockedSlot : blockedSlots)
                withBlockedSlots(blockedSlot + (i * 9));
        return this;
    }

    public CustomGUIBuilder blockAllSlots() {
        allSlotsBlocked = true;
        return this;
    }

    private ScreenPosition convertScreenPosition(ScreenPosition screenPosition) {
        return screenPosition.withTextType(getCorrectTextType());
    }

    public TextType getCorrectTextType() {
        return type != null ? TextType.getByInventoryType(type) : TextType.getByChestSize(chestSize);
    }

    public boolean isSlotBlocked(int slot) {
        return allSlotsBlocked || this.blockedSlots.contains(slot);
    }

    boolean isUsePlayerSlots() {
        return usePlayerSlots;
    }

    @Nullable
    InventoryType getType() {
        return type;
    }

    public int getChestSize() {
        return chestSize;
    }

    public Set<Integer> getBlockedSlots() {
        if (allSlotsBlocked) {
            var set = new HashSet<Integer>();
            if (type != null) {
                for (int i = 0; i < type.getDefaultSize(); i++)
                    set.add(i);
            } else {
                for (int i = 0; i < this.chestSize * 9; i++)
                    set.add(i);
            }
            return set;
        }
        return new HashSet<>(blockedSlots);
    }

    public void createMenuForPlayer(Player player) {
        createMenuForPlayer(player, null);
    }

    public void createMenuForPlayer(Player player, @Nullable Consumer<ActiveGUI> initialSetup) {
        Bukkit.getScheduler().runTaskAsynchronously(MCCreativeLabExtension.getInstance(), () -> {

            var active = MCCreativeLabExtension.getInstance().getHudRenderer().getOrStartActiveHud(player, this);
            active.showAll();
            var rendering = active.render();
            Bukkit.getScheduler().runTask(MCCreativeLabExtension.getInstance(), () -> {
                new ActiveGUI(this, active, rendering, activeGUI -> {
                    if (initialSetup != null)
                        initialSetup.accept(activeGUI);
                    //clickableButtons.forEach((integer, clickableButton) -> activeGUI.setItem(integer, clickableButton.clickableItem));
                });
            });
        });
    }

}

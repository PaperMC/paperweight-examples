package de.verdox.mccreativelab.generator.resourcepack.types;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.HudElement;
import de.verdox.mccreativelab.generator.resourcepack.renderer.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.group.Button;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.group.HudMultiLineText;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.group.PartlyVisibleTexture;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudTexture;
import de.verdox.mccreativelab.generator.resourcepack.types.font.BitMap;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Space;
import de.verdox.mccreativelab.generator.resourcepack.types.font.StandardFontAssets;
import de.verdox.mccreativelab.util.io.AssetUtil;
import de.verdox.mccreativelab.util.io.BitMapReader;
import de.verdox.mccreativelab.util.io.ImageUtil;
import de.verdox.mccreativelab.util.io.StringAlign;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class CustomHud extends ShaderRendered {
    private static final int ascentRange = 1000;
    private final Font hudTexturesFont;
    private char textureChars = '\uEff1';
    private static int idCounter = 100;
    private final Map<String, HudElement> elements = new HashMap<>();
    private final Set<Font> hudTextFonts = new HashSet<>();

    public CustomHud(NamespacedKey namespacedKey) {
        super(namespacedKey);
        this.hudTexturesFont = new Font(namespacedKey);

        var map = new HashMap<String, Integer>();
        //map.put("", 0);
        map.put(" ", 1);

        for (int i = 1; i <= 10; i++)
            map.put(drawNextChar() + "", (int) Math.pow(2, i));
        hudTexturesFont.addSpace(new Space(map));
    }

    public Map<String, HudElement> getElements() {
        return elements;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        customPack.register(this.hudTexturesFont);
        for (Font hudTextFont : hudTextFonts)
            customPack.register(hudTextFont);
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        installShaderFileToPack(customPack);
    }

    public CustomHud withTexture(String textureName, Asset<CustomResourcePack> textureAsset, ScreenPosition screenPosition) throws IOException {
        var id = drawNextFreeID();
        var bitMap = new BitMap(textureAsset, ImageUtil.getPixelHeight(textureAsset.assetInputStream()), calculateAscentID(id), String.valueOf(drawNextChar()));

        hudTexturesFont.addBitMap(bitMap);
        checkIfIDTaken(textureName);

        var dimensions = new BitMapReader(textureAsset.assetInputStream(), bitMap)
            .getCharacterDimensions().values()
            .stream().findFirst()
            .orElse(new BitMapReader.CharacterDimensions(bitMap.getPixelWidth(),
                bitMap.getPixelHeight())
            );
        registerElement(textureName, new SingleHudTexture(bitMap, bitMap.character()[0], dimensions.width(), dimensions.height(), screenPosition));
        createGuiSizedShaderInstruction(bitMap.ascent(), screenPosition);
        return this;
    }
    public CustomHud withText(String textID, ScreenPosition screenPosition, StringAlign.Alignment alignment, int pixelAlignmentWidth, float scale) {
        NamespacedKey textFieldKey = new NamespacedKey(key().namespace(), key().value() + "_" + textID.toLowerCase(Locale.ROOT));
        Font textFont = new Font(textFieldKey);

        textFont.addSpace(new Space(Map.of(" ", (int) (4 * scale), "\u200c", 0)));

        var ascii = textFont.addBitMap(StandardFontAssets.asciiBitmap.withHeight(8)
                                                                     .withAscent(calculateAscentID(drawNextFreeID()))
                                                                     .withScale(scale));

        var accented = textFont.addBitMap(StandardFontAssets.accentedBitMap.withHeight(12)
                                                                           .withAscent(calculateAscentID(drawNextFreeID()))
                                                                           .withScale(scale));

        checkIfIDTaken(textID);
        registerElement(textID, new SingleHudText(textFont, screenPosition, alignment, pixelAlignmentWidth));

        createGuiSizedShaderInstruction(ascii.ascent(), screenPosition);
        createGuiSizedShaderInstruction(accented.ascent(), screenPosition.addToYOffset((int) (((12 - 8) - 0.3) * scale)));

        hudTextFonts.add(textFont);
        return this;
    }
    public CustomHud withButton(String buttonName, StringAlign.Alignment alignment, float textScale, @Nullable Asset<CustomResourcePack> whenSelected, @Nullable Asset<CustomResourcePack> whenEnabled, @Nullable Asset<CustomResourcePack> whenDisabled, ScreenPosition buttonPos, ScreenPosition textPos) throws IOException {
        checkIfIDTaken(buttonName);

        SingleHudTexture selectedHudTexture = null;
        SingleHudTexture enabledHudTexture = null;
        SingleHudTexture disabledHudTexture = null;

        if (whenSelected != null) {
            withTexture(buttonName + "_selected", whenSelected, buttonPos.addToXOffset(AssetUtil.getPixelWidthIfImage(whenSelected) / 2 * -1));
            selectedHudTexture = (SingleHudTexture) elements.get(buttonName + "_selected");
        }

        if (whenEnabled != null) {
            withTexture(buttonName + "_enabled", whenEnabled, buttonPos.addToXOffset(AssetUtil.getPixelWidthIfImage(whenEnabled) / 2 * -1));
            enabledHudTexture = (SingleHudTexture) elements.get(buttonName + "_enabled");
        }

        if (whenDisabled != null) {
            withTexture(buttonName + "_disabled", whenSelected, buttonPos.addToXOffset(AssetUtil.getPixelWidthIfImage(whenDisabled) / 2 * -1));
            disabledHudTexture = (SingleHudTexture) elements.get(buttonName + "_disabled");
        }

        withText(buttonName + "_buttontext", textPos.withLayer(buttonPos.layer() + 1), alignment, 0, textScale);
        SingleHudText buttonText = (SingleHudText) elements.get(buttonName + "_buttontext");

        registerElement(buttonName, new Button(buttonText, selectedHudTexture, enabledHudTexture, disabledHudTexture));
        return this;
    }
    public CustomHud withMultiLineText(String multiLineID, int lines, int charsPerLine, int pixelsBetweenLines, StringAlign.Alignment alignment, ScreenPosition startPos, float scale) {
        var textElements = new LinkedList<SingleHudText>();
        for (int i = 0; i < lines; i++) {
            var textID = multiLineID.concat("_" + i);
            withText(textID, startPos.addToYOffset(i * pixelsBetweenLines), alignment, 0, scale);
            textElements.add((SingleHudText) elements.get(textID));
        }
        checkIfIDTaken(multiLineID);
        registerElement(multiLineID, new HudMultiLineText(textElements, charsPerLine, alignment));
        return this;
    }
    public CustomHud withPartlyVisibleTexture(String textureField, ScreenPosition screenPosition, Asset<CustomResourcePack> originalPicture, int parts) throws IOException {
        var splitImages = AssetUtil.createPartlyVisibleCopys(originalPicture, parts);

        var textureList = new LinkedList<SingleHudTexture>();
        for (int i = 0; i < splitImages.size(); i++) {
            var resource = splitImages.get(i);
            withTexture(textureField + "_" + i, resource, screenPosition);
            textureList.add((SingleHudTexture) elements.get(textureField + "_" + i));
        }
        registerElement(textureField, new PartlyVisibleTexture(textureList));
        return this;
    }

    public Font getHudTexturesFont() {
        return hudTexturesFont;
    }

    private int calculateAscentID(int id) {
        return ((id * ascentRange) + 500) * -1;
    }

    private char drawNextChar() {
        return textureChars++;
    }

    private int drawNextFreeID() {
        return idCounter++;
    }

    private void registerElement(String key, HudElement element) {
        this.elements.put(key, element);
    }

    private void checkIfIDTaken(String id) {
        if (this.elements.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already taken in custom hud " + key());
    }
}

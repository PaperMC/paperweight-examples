package de.verdox.mccreativelab.generator.resourcepack.types.rendered;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.ConfigurableResource;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.HudElement;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group.Button;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group.HudMultiLineText;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.group.PartlyVisibleTexture;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudTexture;
import de.verdox.mccreativelab.generator.resourcepack.types.font.BitMap;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Space;
import de.verdox.mccreativelab.generator.resourcepack.types.font.StandardFontAssets;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.AssetUtil;
import de.verdox.mccreativelab.util.io.BitMapReader;
import de.verdox.mccreativelab.util.io.ImageUtil;
import de.verdox.mccreativelab.util.io.StringAlign;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.*;
public abstract class ComponentRendered<C extends ComponentRendered<C,T>, T extends ActiveComponentRendered<T,C>> extends ShaderRendered implements ConfigurableResource<CustomResourcePack> {
    private static final int ascentRange = 1000;
    private final Font hudTexturesFont;
    private char textureChars = '\uEff1';
    private static int idCounter = 100;
    private final Map<String, HudElement> elements = new HashMap<>();
    private final Set<Font> hudTextFonts = new HashSet<>();
    private final Map<HudElement, RenderedElementBehavior<T, ? extends HudElement.Rendered<?>>> behaviors = new HashMap<>();

    public ComponentRendered(NamespacedKey namespacedKey) {
        super(namespacedKey);
        this.hudTexturesFont = new Font(namespacedKey);

        var map = new HashMap<String, Integer>();
        //map.put("", 0);
        map.put(" ", 1);

        for (int i = 1; i <= 10; i++)
            map.put(drawNextChar() + "", (int) Math.pow(2, i));
        hudTexturesFont.addSpace(new Space(map));
    }

    protected ScreenPosition convertScreenPosition(ScreenPosition screenPosition){
        return screenPosition;
    }

    public Map<HudElement, RenderedElementBehavior<T, ? extends HudElement.Rendered<?>>> getBehaviors() {
        return behaviors;
    }

    public Map<String, HudElement> getElements() {
        return elements;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        customPack.register(this.hudTexturesFont);
        for (BitMap bitMap : this.hudTexturesFont.getBitMaps())
            customPack.registerIfNotAlready(bitMap.bitmapImageAsset());
        for (Font hudTextFont : hudTextFonts) {
            customPack.register(hudTextFont);
            for (BitMap bitMap : hudTextFont.getBitMaps()) {
                customPack.registerIfNotAlready(bitMap.bitmapImageAsset());
            }
            for (BitMap bitMap : hudTexturesFont.getBitMaps()) {
                customPack.registerIfNotAlready(bitMap.bitmapImageAsset());
            }
        }
    }

    @Override
    public void afterResourceInstallation(CustomResourcePack customPack) throws IOException {
        clearShaderInstructions();
        elements.forEach((s, hudElement) -> {
            if(hudElement instanceof SingleHudTexture singleHudTexture)
                createGuiSizedShaderInstruction(singleHudTexture.bitMap().ascent(), singleHudTexture.screenPosition());
            else if (hudElement instanceof SingleHudText singleHudText){
                BitMap ascii = singleHudText.font().getBitMaps().get(0);
                BitMap accented = singleHudText.font().getBitMaps().get(1);

                createGuiSizedShaderInstruction(ascii.ascent(), singleHudText.screenPosition());
                createGuiSizedShaderInstruction(accented.ascent(), singleHudText.screenPosition().addToYOffset((int) (((12 - 8) - 0.3) * singleHudText.scale())));
            }
        });
        installShaderInstructions();
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {}

    public C withTexture(String textureName, Asset<CustomResourcePack> textureAsset, ScreenPosition screenPosition, @Nullable RenderedElementBehavior<T, SingleHudTexture.RenderedSingleHudTexture> behavior) throws IOException {
        var id = drawNextFreeID();
        AssetBasedResourcePackResource assetBasedResourcePackResource = new AssetBasedResourcePackResource(new NamespacedKey(getKey().getNamespace(), getKey().getKey()+"/textures/"+textureName), textureAsset, ResourcePackAssetTypes.TEXTURES, "png");
        var bitMap = new BitMap(assetBasedResourcePackResource, ImageUtil.getPixelHeight(textureAsset.assetInputStream()), calculateAscentID(id), String.valueOf(drawNextChar()));
        screenPosition = convertScreenPosition(screenPosition);

        hudTexturesFont.addBitMap(bitMap);
        checkIfIDTaken(textureName);

        var dimensions = new BitMapReader(textureAsset.assetInputStream(), bitMap)
            .getCharacterDimensions().values()
            .stream().findFirst()
            .orElse(new BitMapReader.CharacterDimensions(bitMap.getPixelWidth(),
                bitMap.getPixelHeight())
            );

        //TODO: Algorithmus funktioniert nicht, deswegen lesen wir die Daten direkt aus dem Bild
        // Jedoch haben wir das Problem, dass transparente Ränder in den Bildern falsche width Werte liefern.
        int width = dimensions.width();
        int height = dimensions.height();

        width = ImageIO.read(textureAsset.assetInputStream().get()).getWidth();
        height = ImageIO.read(textureAsset.assetInputStream().get()).getHeight();

        registerElement(textureName, new SingleHudTexture(bitMap, bitMap.character()[0], width, height, screenPosition), behavior);
        //createGuiSizedShaderInstruction(bitMap.ascent(), screenPosition);
        return (C) this;
    }
    public C withTexture(String textureName, Asset<CustomResourcePack> textureAsset, ScreenPosition screenPosition) throws IOException{
        return withTexture(textureName, textureAsset, screenPosition, null);
    }

    public C withText(String textID, ScreenPosition screenPosition, StringAlign.Alignment alignment, float scale, @Nullable RenderedElementBehavior<T, SingleHudText.RenderedSingleHudText> behavior) {
        NamespacedKey textFieldKey = new NamespacedKey(key().namespace(), key().value() + "_" + textID.toLowerCase(Locale.ROOT));
        Font textFont = new Font(textFieldKey);
        screenPosition = convertScreenPosition(screenPosition);

        textFont.addSpace(new Space(Map.of(" ", (int) (4 * scale), "\u200c", 0)));

        var ascii = textFont.addBitMap(StandardFontAssets.asciiBitmap.withHeight(8)
                                                                     .withAscent(calculateAscentID(drawNextFreeID()))
                                                                     .withScale(scale));

        var accented = textFont.addBitMap(StandardFontAssets.accentedBitMap.withHeight(12)
                                                                           .withAscent(calculateAscentID(drawNextFreeID()))
                                                                           .withScale(scale));

        checkIfIDTaken(textID);
        registerElement(textID, new SingleHudText(textFont, screenPosition, alignment, scale), behavior);

        //createGuiSizedShaderInstruction(ascii.ascent(), screenPosition);
        //createGuiSizedShaderInstruction(accented.ascent(), screenPosition.addToYOffset((int) (((12 - 8) - 0.3) * scale)));

        hudTextFonts.add(textFont);
        return (C) this;
    }
    public C withText(String textID, ScreenPosition screenPosition, StringAlign.Alignment alignment, float scale){
        return withText(textID, screenPosition, alignment, scale, null);
    }
    public C withText(String textID, ScreenPosition screenPosition, StringAlign.Alignment alignment, @Nullable RenderedElementBehavior<T, SingleHudText.RenderedSingleHudText> behavior){
        return withText(textID, screenPosition, alignment, 1, behavior);
    }

    public C withButton(String buttonName, StringAlign.Alignment alignment, float textScale, @Nullable Asset<CustomResourcePack> whenSelected, @Nullable Asset<CustomResourcePack> whenEnabled, @Nullable Asset<CustomResourcePack> whenDisabled, ScreenPosition buttonPos, ScreenPosition textPos, @Nullable RenderedElementBehavior<T, Button.RenderedButton> behavior) throws IOException {
        checkIfIDTaken(buttonName);

        SingleHudTexture selectedHudTexture = null;
        SingleHudTexture enabledHudTexture = null;
        SingleHudTexture disabledHudTexture = null;

        if (whenSelected != null) {
            withTexture(buttonName + "_selected", whenSelected, buttonPos.addToXOffset(AssetUtil.getPixelWidthIfImage(whenSelected) / 2 * -1), null);
            selectedHudTexture = (SingleHudTexture) elements.get(buttonName + "_selected");
        }

        if (whenEnabled != null) {
            withTexture(buttonName + "_enabled", whenEnabled, buttonPos.addToXOffset(AssetUtil.getPixelWidthIfImage(whenEnabled) / 2 * -1), null);
            enabledHudTexture = (SingleHudTexture) elements.get(buttonName + "_enabled");
        }

        if (whenDisabled != null) {
            withTexture(buttonName + "_disabled", whenSelected, buttonPos.addToXOffset(AssetUtil.getPixelWidthIfImage(whenDisabled) / 2 * -1), null);
            disabledHudTexture = (SingleHudTexture) elements.get(buttonName + "_disabled");
        }

        withText(buttonName + "_buttontext", textPos.withLayer(buttonPos.layer() + 1), alignment, textScale, null);
        SingleHudText buttonText = (SingleHudText) elements.get(buttonName + "_buttontext");

        registerElement(buttonName, new Button(buttonText, selectedHudTexture, enabledHudTexture, disabledHudTexture), behavior);
        return (C) this;
    }

    public C withMultiLineText(String multiLineID, int lines, int charsPerLine, int pixelsBetweenLines, StringAlign.Alignment alignment, ScreenPosition startPos, float scale, @Nullable RenderedElementBehavior<T, HudMultiLineText.RenderedGroupMultiLineText> behavior) {
        checkIfIDTaken(multiLineID);
        var textElements = new LinkedList<SingleHudText>();
        startPos = convertScreenPosition(startPos);
        for (int i = 0; i < lines; i++) {
            var textID = multiLineID.concat("_" + i);
            withText(textID, startPos.addToYOffset(i * (pixelsBetweenLines * -1)), alignment, scale, null);
            textElements.add((SingleHudText) elements.get(textID));
        }
        registerElement(multiLineID, new HudMultiLineText(textElements, charsPerLine, alignment), behavior);
        return (C) this;
    }

    public C withPartlyVisibleTexture(String textureField, ScreenPosition screenPosition, Asset<CustomResourcePack> originalPicture, int parts, @Nullable RenderedElementBehavior<T, PartlyVisibleTexture.RenderedPartlyVisibleTexture> behavior) throws IOException {
        checkIfIDTaken(textureField);
        var splitImages = AssetUtil.createPartlyVisibleCopys(originalPicture, parts);
        screenPosition = convertScreenPosition(screenPosition);

        var textureList = new LinkedList<SingleHudTexture>();
        for (int i = 0; i < splitImages.size(); i++) {
            var resource = splitImages.get(i);
            withTexture(textureField + "_" + i, resource, screenPosition, null);
            textureList.add((SingleHudTexture) elements.get(textureField + "_" + i));
        }
        registerElement(textureField, new PartlyVisibleTexture(textureList), behavior);
        return (C) this;
    }

    public Font getHudTexturesFont() {
        return hudTexturesFont;
    }

    @Override
    public JsonObject serializeToJson() {
        JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();
        elements.forEach((s, hudElement) -> jsonObjectBuilder.add(s, JsonObjectBuilder.create(hudElement.serializeToJson())));
        return jsonObjectBuilder.build();
    }

    @Override
    public void deserializeFromJson(JsonObject jsonObject) {
        for (String s : jsonObject.keySet()) {
            JsonObject jsonObject1 = jsonObject.getAsJsonObject(s);
            if(elements.containsKey(s))
                elements.get(s).deserializeFromJson(jsonObject1);
        }

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

    private void registerElement(String key, HudElement element, @Nullable RenderedElementBehavior<T, ? extends HudElement.Rendered<?>> behavior) {
        this.elements.put(key, element);
        if (behavior != null)
            this.behaviors.put(element, behavior);
    }

    private void checkIfIDTaken(String id) {
        if (this.elements.containsKey(id))
            throw new IllegalArgumentException("Id " + id + " already taken in custom hud " + key());
    }
}

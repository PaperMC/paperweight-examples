package de.verdox.mccreativelab.generator.resourcepack.types.rendered;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.TextType;
import de.verdox.mccreativelab.generator.resourcepack.types.font.StandardFontAssets;
import org.bukkit.NamespacedKey;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public abstract class ShaderRendered extends ResourcePackResource {
    private static final Map<ShaderRendered, List<String>> shaderInstructions = new HashMap<>();
    public static final int ascentRange = 1000;
    private final List<String> individualShaderInstructions = new LinkedList<>();

    public ShaderRendered(NamespacedKey namespacedKey) {
        super(namespacedKey);
    }

    protected void installShaderInstructions(){
        shaderInstructions.put(this, individualShaderInstructions);
    }

    protected void createGuiSizedShaderInstruction(int ascent, ScreenPosition screenPosition) {
        var list = new LinkedList<String>();

        float xOnScreen = screenPosition.textType().xOffset() + (screenPosition.xOffset());
        float yOnScreen = screenPosition.textType().yOffset() + (screenPosition.yOffset()) * -1;
        // If it is layer 1 (i=0) include own width to correct spacing. Else it is only relative to the upper layer
        var caseID = (ascent / ascentRange) * -1;
        String xString = xOnScreen >= 0 ? "-" + Math.abs(xOnScreen) : "+" + Math.abs(xOnScreen);
        String yString = yOnScreen >= 0 ? "-" + Math.abs(yOnScreen) : "+" + Math.abs(yOnScreen);

        list.add("\t\t\t\tcase " + caseID + ":");
        list.add("\t\t\t\t\txOffset = int(guiSize.x * (-" + Math.abs(screenPosition.x()) + ".0/100))" + xString + ";");
        list.add("\t\t\t\t\tyOffset = int(guiSize.y * (" + Math.abs(screenPosition.y()) + ".0/100))" + yString + ";");

        list.add("\t\t\t\t\tpos.x -= (guiSize.x * 0.5);");
        if (!screenPosition.textType().equals(TextType.ACTION_BAR))
            list.add("\t\t\t\t\tpos.y += (guiSize.y * 0.5);");

        list.add("\t\t\t\t\tlayer = " + screenPosition.layer() + ";");
        list.add("\t\t\t\t\tbreak;");
        individualShaderInstructions.addAll(list);
    }

    protected void clearShaderInstructions(){
        individualShaderInstructions.clear();
    }

    public static void installShaderFileToPack(CustomResourcePack customResourcePack) throws IOException {
        NamespacedKey shaderKey = new NamespacedKey("minecraft","core/rendertype_text");
        Asset<CustomResourcePack> shaderInitFileAsset = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/shader/rendertype_text.json"));
        Asset<CustomResourcePack> pixelShaderAsset = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/shader/rendertype_text.fsh"));
        Asset<CustomResourcePack> vertexShaderAsset = new Asset<>(() -> StandardFontAssets.class.getResourceAsStream("/resourcepack/shader/rendertype_text.vsh"));

        shaderInitFileAsset.installAsset(customResourcePack, shaderKey, ResourcePackAssetTypes.SHADERS, "json");
        pixelShaderAsset.installAsset(customResourcePack, shaderKey, ResourcePackAssetTypes.SHADERS, "fsh");
        File vertexShaderFile = vertexShaderAsset.installAsset(customResourcePack, shaderKey, ResourcePackAssetTypes.SHADERS, "vsh");

        var vertexShaderFileLines = new LinkedList<>(Files.readAllLines(vertexShaderFile.toPath()));

        var indexToInsert = -1;
        for (int i = 0; i < vertexShaderFileLines.size(); i++) {
            var line = vertexShaderFileLines.get(i);
            if (line.contains("switch (int(id)) {")) {
                indexToInsert = i + 1;
                break;
            }
        }

        if(indexToInsert == -1)
            throw new IllegalStateException("vsh shader file is damaged");

        int lineCounter = 0;
        for (String instruction : shaderInstructions.values().stream().flatMap(Collection::stream).toList()) {
            vertexShaderFileLines.add(lineCounter + indexToInsert, instruction);
            lineCounter++;
        }

/*        for (int i = 0; i < shaderInstructions.size(); i++) {
            var instruction = shaderInstructions.get(this);
            vertexShaderFileLines.add(i + indexToInsert, instruction);
        }*/

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(vertexShaderFile))) {
            for (String shaderFileLine : vertexShaderFileLines) {
                writer.write(shaderFileLine);
                writer.newLine();
            }
            writer.flush();
        }
    }
}

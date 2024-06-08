package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.io.AssetUtil;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ModelBuilder extends ResourcePackResource {
    public static ModelBuilder create(NamespacedKey namespacedKey) {
        return new ModelBuilder(namespacedKey);
    }
    private final Map<String, AssetBasedResourcePackResource> textures = new HashMap<>();
    private final Set<ModelElementBuilder> elementBuilders = new HashSet<>();
    private int[] texture_size = new int[]{16, 16};
    private NamespacedKey parent;

    private ModelBuilder(NamespacedKey namespacedKey) {
        super(namespacedKey);
    }

    public ModelBuilder withParent(NamespacedKey parent){
        this.parent = parent;
        return this;
    }

    public ModelBuilder withTextureSize(int xSize, int ySize) {
        this.texture_size = new int[]{xSize, ySize};
        return this;
    }

    public ModelBuilder withTexture(String textureID, NamespacedKey namespacedKey, Asset<CustomResourcePack> asset) {
        textures.put(textureID, new AssetBasedResourcePackResource(namespacedKey, asset, ResourcePackAssetTypes.TEXTURES, "png"));
        if(!textures.containsKey("particle"))
            textures.put("particle", new AssetBasedResourcePackResource(namespacedKey, asset, ResourcePackAssetTypes.TEXTURES, "png"));
        return this;
    }

    public ModelBuilder withElement(int[] from, int[] to, Consumer<ModelElementBuilder> consumer) {
        ModelElementBuilder modelElementBuilder = new ModelElementBuilder(from, to);
        consumer.accept(modelElementBuilder);
        elementBuilders.add(modelElementBuilder);
        return this;
    }

    public ModelBuilder withFullBlockElement(Consumer<ModelElementBuilder> consumer) {
        return withElement(new int[]{0, 0, 0}, new int[]{16, 16, 16}, consumer);
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        this.textures.forEach((s, assetBasedResourcePackResource) -> customPack.register(assetBasedResourcePackResource));
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {
        JsonObjectBuilder mainJson = JsonObjectBuilder.create();
        createJson(mainJson);
        AssetUtil.createJsonAssetAndInstall(mainJson.build(), customPack, getKey(), ResourcePackAssetTypes.MODELS);
    }

    public ItemTextureData.ModelType asModelType() {
        return new ItemTextureData.ModelType("", (namespacedKey, json) -> {
            JsonObjectBuilder mainJson = JsonObjectBuilder.create(json);
            createJson(mainJson);
        });
    }

    private void createJson(JsonObjectBuilder mainJson) {
        mainJson
            .add("credit", "Made with MCCreativeLab")
            .add("texture_size", JsonArrayBuilder.of(texture_size));
        if(parent != null)
            mainJson.add("parent", parent.asString());

        if (!this.textures.isEmpty()) {
            JsonObjectBuilder textures = JsonObjectBuilder.create();
            this.textures.forEach((s, assetBasedResourcePackResource) -> textures.add(s, assetBasedResourcePackResource.getKey().toString()));
            mainJson.add("textures", textures);
        }

        if (!this.elementBuilders.isEmpty()) {
            JsonArrayBuilder elements = JsonArrayBuilder.create();
            this.elementBuilders.forEach(modelElementBuilder -> elements.add(modelElementBuilder.create()));
            mainJson.add("elements", elements);
        }
    }

    public Map<String, AssetBasedResourcePackResource> getTextures() {
        return textures;
    }

    private record Face(String face, int[] uv, String textureID) {
    }

    public static class ModelElementBuilder {
        private final int[] from;
        private final int[] to;
        private final Map<String, Face> faces = new HashMap<>();

        private ModelElementBuilder(int[] from, int[] to) {
            this.from = from;
            this.to = to;
        }

        public ModelElementBuilder withFace(String face, int[] uv, String textureID) {
            faces.put(face, new Face(face, uv, textureID));
            return this;
        }

        public ModelElementBuilder withFace(String face, String textureID) {
            return withFace(face, new int[]{0, 0, 16, 16}, textureID);
        }

        private JsonObjectBuilder create() {
            JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();
            jsonObjectBuilder.add("from", JsonArrayBuilder.of(from));
            jsonObjectBuilder.add("to", JsonArrayBuilder.of(to));

            if (!this.faces.isEmpty()) {
                JsonObjectBuilder faces = JsonObjectBuilder.create();
                this.faces.forEach((s, face) -> {
                    faces.add(s,
                        JsonObjectBuilder.create()
                            .add("uv", JsonArrayBuilder.of(face.uv))
                            .add("texture", "#" + face.textureID)
                    );
                });
                jsonObjectBuilder.add("faces", faces);
            }
            return jsonObjectBuilder;
        }
    }
}

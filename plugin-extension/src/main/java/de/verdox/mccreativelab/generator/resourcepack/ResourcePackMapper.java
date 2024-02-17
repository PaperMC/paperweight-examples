package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.Resource;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.ModelFile;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ShaderRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.CustomGUIBuilder;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageFile;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.CustomMenu;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import de.verdox.mccreativelab.registry.OpenRegistry;

public class ResourcePackMapper {
    private static final String NAMESPACE = "mccreativelab";
    private final OpenRegistry<CustomGUIBuilder> guiRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<CustomHud> hudsRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<ItemTextureData> itemTexturesRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<ModelFile> modelsRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<ShaderRendered> shaderRenderedRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<SoundData> soundDataRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<CustomMenu> menuRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<LanguageFile> languageRegistry = new OpenRegistry<>(NAMESPACE);
    private final OpenRegistry<Font> fontRegistry = new OpenRegistry<>(NAMESPACE);
    void register(Resource<CustomResourcePack> resource){
        if(resource instanceof CustomGUIBuilder customGUIBuilder)
            guiRegistry.register(customGUIBuilder.key(), customGUIBuilder);
        else if(resource instanceof CustomHud customHud)
            hudsRegistry.register(customHud.key(), customHud);
        else if(resource instanceof ItemTextureData itemTextureData)
            itemTexturesRegistry.register(itemTextureData.key(), itemTextureData);
/*        else if(resource instanceof ModelFile modelFile)
            modelsRegistry.register(modelFile.key(), modelFile);*/
        else if(resource instanceof ShaderRendered shaderRendered)
            shaderRenderedRegistry.register(shaderRendered.key(), shaderRendered);
        else if(resource instanceof CustomMenu customMenu)
            menuRegistry.register(customMenu.key(), customMenu);
        else if(resource instanceof LanguageFile languageFile)
            languageRegistry.register(languageFile.key(), languageFile);
        else if(resource instanceof Font font)
            fontRegistry.register(font.key(), font);
    }

    public OpenRegistry<CustomGUIBuilder> getGuiRegistry() {
        return guiRegistry;
    }

    public OpenRegistry<CustomHud> getHudsRegistry() {
        return hudsRegistry;
    }

    public OpenRegistry<ItemTextureData> getItemTexturesRegistry() {
        return itemTexturesRegistry;
    }

    public OpenRegistry<ModelFile> getModelsRegistry() {
        return modelsRegistry;
    }

    public OpenRegistry<ShaderRendered> getShaderRenderedRegistry() {
        return shaderRenderedRegistry;
    }

    public OpenRegistry<SoundData> getSoundDataRegistry() {
        return soundDataRegistry;
    }

    public OpenRegistry<CustomMenu> getMenuRegistry() {
        return menuRegistry;
    }

    public OpenRegistry<LanguageFile> getLanguageRegistry() {
        return languageRegistry;
    }

    public OpenRegistry<Font> getFontRegistry() {
        return fontRegistry;
    }

    void clear(){
        guiRegistry.clear();
        hudsRegistry.clear();
        itemTexturesRegistry.clear();
        modelsRegistry.clear();
        shaderRenderedRegistry.clear();
        soundDataRegistry.clear();
        menuRegistry.clear();
        languageRegistry.clear();
        fontRegistry.clear();
    }
}

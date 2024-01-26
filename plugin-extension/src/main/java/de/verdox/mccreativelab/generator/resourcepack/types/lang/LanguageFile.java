package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public class LanguageFile extends ResourcePackResource {
    private final LanguageInfo languageInfo;
    private final Asset<CustomResourcePack> customLanguageFile;

    public LanguageFile(NamespacedKey namespacedKey, LanguageInfo languageInfo, Asset<CustomResourcePack> customLanguageFile) {
        super(namespacedKey);
        this.languageInfo = languageInfo;
        this.customLanguageFile = customLanguageFile;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        JsonObject jsonObject = JsonUtil.readJsonInputStream(this.customLanguageFile.assetInputStream().get());
        for (String s : jsonObject.keySet())
            customPack.addTranslation(new Translatable(languageInfo, s, jsonObject.get(s).getAsString()));
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {

    }
}

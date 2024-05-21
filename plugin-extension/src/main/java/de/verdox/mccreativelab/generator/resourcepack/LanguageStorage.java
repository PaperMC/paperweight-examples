package de.verdox.mccreativelab.generator.resourcepack;

import com.destroystokyo.paper.ClientOption;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translation;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import de.verdox.mccreativelab.util.io.AssetUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class LanguageStorage {
    private static final LanguageInfo STANDARD = LanguageInfo.ENGLISH_US;
    private final Set<Translation> customTranslations = new HashSet<>();
    private final Map<String, Map<LanguageInfo, Translation>> translationKeyMapping = new HashMap<>();
    private final CustomResourcePack customResourcePack;

    LanguageStorage(CustomResourcePack customResourcePack) {
        this.customResourcePack = customResourcePack;

        cacheMinecraftTranslations(LanguageInfo.ENGLISH_US);
    }

    public void addTranslation(Translation translation) {
        addTranslation(translation, false);
    }

    public void addTranslation(Translation translation, boolean onlyCache) {
        if (!onlyCache)
            customTranslations.add(translation);
        translationKeyMapping.computeIfAbsent(translation.key(), s -> new HashMap<>()).put(translation.languageInfo(), translation);

        // We add the translation as standard translation if there is no alternative in standard translation.
        if(!translationKeyMapping.get(translation.key()).containsKey(STANDARD) && !translation.languageInfo().equals(STANDARD))
            addTranslation(new Translation(STANDARD, translation.key(), translation.content()), onlyCache);
    }

    public void addTranslation(Translatable translatable, boolean onlyCache) {
        for (Translation translation : translatable.getTranslations()) {
            addTranslation(translation, onlyCache);
        }
    }

    public void addTranslation(Translatable translatable) {
        addTranslation(translatable, false);
    }

    public TextComponent translateToComponent(String key, LanguageInfo languageInfo, String defaultTranslation) {
        return Component.text(translate(key, languageInfo, defaultTranslation));
    }

    public String translate(String key, LanguageInfo languageInfo, String defaultTranslation) {
        if (!translationKeyMapping.containsKey(key))
            return defaultTranslation;
        Map<LanguageInfo, Translation> byLanguageTranslations = translationKeyMapping.get(key);
        if (!byLanguageTranslations.containsKey(languageInfo)) {
            if (STANDARD.equals(languageInfo))
                return defaultTranslation;
            return translate(key, STANDARD, defaultTranslation);
        }

        return byLanguageTranslations.get(languageInfo).content();
    }

    public String translate(String key, Player player) {
        return translate(key, player, key);
    }

    public String translate(String key, Player player, String fallbackText) {
        String localeKey = player.getClientOption(ClientOption.LOCALE);
        LanguageInfo languageInfo = new LanguageInfo(localeKey, "", "", false);
        return translate(key, languageInfo, fallbackText);
    }

    public TextComponent translateToComponent(String key, Player player) {
        return Component.text(translate(key, player));
    }

    Set<Translation> getCustomTranslations() {
        return customTranslations;
    }

    void installLanguages() {
        customTranslations
            .stream()
            .collect(Collectors.groupingBy(Translation::languageInfo))
            .forEach((languageInfo, translations) -> {
                JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();
                Collections.sort(translations);
                for (Translation translation : translations)
                    jsonObjectBuilder.add(translation.key(), translation.content());

                AssetUtil.createJsonAssetAndInstall(jsonObjectBuilder.build(), customResourcePack, new NamespacedKey("minecraft", languageInfo.identifier()), ResourcePackAssetTypes.LANG);
            });
    }

    private void cacheMinecraftTranslations(LanguageInfo languageInfo) {

        try (InputStream defaultTranslationFile = Bukkit.class.getResourceAsStream("/assets/minecraft/lang/" + languageInfo.identifier() + ".json")) {
            JsonObject jsonObject = JsonUtil.readJsonInputStream(defaultTranslationFile);
            for (String translationKey : jsonObject.keySet()) {
                String translation = jsonObject.get(translationKey).getAsString();
                addTranslation(new Translation(LanguageInfo.ENGLISH_US, translationKey, translation), true);
            }
            Bukkit.getLogger()
                .info("Cached " + jsonObject.keySet().size() + " vanilla translations into LanguageStorage");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

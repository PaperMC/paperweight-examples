package de.verdox.mccreativelab.generator.resourcepack;

import com.destroystokyo.paper.ClientOption;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
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
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

public class LanguageStorage {
    private static final LanguageInfo STANDARD = LanguageInfo.ENGLISH_US;
    private final Set<Translatable> customTranslations = new HashSet<>();
    private final Map<String, Map<LanguageInfo, Translatable>> translationKeyMapping = new HashMap<>();
    private final CustomResourcePack customResourcePack;

    LanguageStorage(CustomResourcePack customResourcePack) {
        this.customResourcePack = customResourcePack;

        try (InputStream defaultTranslationFile = Bukkit.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
            JsonObject jsonObject = JsonUtil.readJsonInputStream(defaultTranslationFile);
            for (String translationKey : jsonObject.keySet()) {
                String translation = jsonObject.get(translationKey).getAsString();
                addTranslation(new Translatable(LanguageInfo.ENGLISH_US, translationKey, translation), true);
            }
            Bukkit.getLogger()
                .info("Cached " + jsonObject.keySet().size() + " vanilla translations into LanguageStorage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTranslation(Translatable translatable) {
        addTranslation(translatable, false);
    }

    public void addTranslation(Translatable translatable, boolean onlyCache) {
        if (!onlyCache)
            customTranslations.add(translatable);
        translationKeyMapping.computeIfAbsent(translatable.key(), s -> new HashMap<>())
            .put(translatable.languageInfo(), translatable);
    }

    public TextComponent translateToComponent(String key, LanguageInfo languageInfo, String defaultTranslation) {
        return Component.text(translate(key, languageInfo, defaultTranslation));
    }

    public String translate(String key, LanguageInfo languageInfo, String defaultTranslation) {
        if (!translationKeyMapping.containsKey(key))
            return defaultTranslation;
        Map<LanguageInfo, Translatable> byLanguageTranslations = translationKeyMapping.get(key);
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

    Set<Translatable> getCustomTranslations() {
        return customTranslations;
    }

    void installLanguages() {
        customTranslations
            .stream()
            .collect(Collectors.groupingBy(Translatable::languageInfo))
            .forEach((languageInfo, translations) -> {
                JsonObjectBuilder jsonObjectBuilder = JsonObjectBuilder.create();
                Collections.sort(translations);
                for (Translatable translatable : translations)
                    jsonObjectBuilder.add(translatable.key(), translatable.content());

                AssetUtil.createJsonAssetAndInstall(jsonObjectBuilder.build(), customResourcePack, new NamespacedKey("minecraft", languageInfo.identifier()), ResourcePackAssetTypes.LANG);
            });
    }
}

package de.verdox.mccreativelab.generator.resourcepack;

import com.destroystokyo.paper.ClientOption;
import com.google.gson.JsonObject;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translatable;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translation;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import de.verdox.mccreativelab.util.gson.JsonUtil;
import de.verdox.mccreativelab.util.io.AssetUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LanguageStorage {
    //TODO: TranslationRegistry not UTF-8
    private static final LanguageInfo STANDARD = LanguageInfo.ENGLISH_US;
    private final Set<Translation> customTranslations = new HashSet<>();
    private final Map<String, Map<LanguageInfo, Translation>> translationKeyMapping = new HashMap<>();
    private final CustomResourcePack customResourcePack;
    private final TranslationRegistry translationRegistry = TranslationRegistry.create(Key.key("mccreativelab","language_storage"));

    LanguageStorage(CustomResourcePack customResourcePack) {
        this.customResourcePack = customResourcePack;

        cacheMinecraftTranslations(LanguageInfo.ENGLISH_US);
        //GlobalTranslator.translator().addSource(translationRegistry);
        translationRegistry.defaultLocale(Locale.US);
    }

    public void addTranslation(Translation translation) {
        addTranslation(translation, false);
    }

    public void addTranslation(Translation translation, boolean onlyCache) {
        if(translation.languageInfo().identifier().isEmpty())
            return;
        if (!onlyCache)
            customTranslations.add(translation);

        if(!translationKeyMapping.computeIfAbsent(translation.key(), s -> new HashMap<>()).containsKey(translation.languageInfo()))
            translationRegistry.register(translation.key(), translation.languageInfo().toLocale(), new MessageFormat(translation.content()));

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

    public TextComponent translateToComponent(String key, LanguageInfo languageInfo) {
        return Component.text(translate(key, languageInfo));
    }

    public String translate(String key, LanguageInfo languageInfo) {
        //String standardTranslation = translationRegistry.translate(key, languageInfo.toLocale()).format(null);

        String standardTranslation = key;

        if (!translationKeyMapping.containsKey(key))
            return standardTranslation;
        Map<LanguageInfo, Translation> byLanguageTranslations = translationKeyMapping.get(key);
        if (!byLanguageTranslations.containsKey(languageInfo)) {
            if (STANDARD.equals(languageInfo))
                return standardTranslation;
            return translate(key, STANDARD);
        }

        return byLanguageTranslations.get(languageInfo).content();
    }

    public String translate(String key, Player player) {
        String localeKey = player.getClientOption(ClientOption.LOCALE);
        LanguageInfo languageInfo = new LanguageInfo(localeKey, "", "", false);
        return translate(key, languageInfo);
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

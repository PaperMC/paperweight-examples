package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Translatable implements GameTranslation{
    private final Map<LanguageInfo, Translation> cache = new HashMap<>();
    private String key;

    public Translatable(LanguageInfo languageInfo, String key, String content){
        this.key = key;
        withAdditionalTranslation(languageInfo, content);
    }

    public String getTranslation(LanguageInfo languageInfo){
        if(!cache.containsKey(languageInfo))
            return "";
        return cache.get(languageInfo).content();
    }

    public void changeTranslationKey(String key) {
        this.key = key;
    }

    public Translatable(String key){
        this.key = key;
    }

    public Map<LanguageInfo, Translation> getCache() {
        return Map.copyOf(cache);
    }

    public Translatable withAdditionalTranslation(LanguageInfo languageInfo, String content){
        this.cache.put(languageInfo, new Translation(languageInfo, key, content));
        return this;
    }

    public Set<Translation> getTranslations(){
        return Set.copyOf(cache.values());
    }

    @Override
    public String key(){
        return this.key;
    }

    public static class Builder {
        private final Map<LanguageInfo, String> cache = new HashMap<>();

        public Builder withAdditionalTranslation(LanguageInfo languageInfo, String content){
            this.cache.put(languageInfo, content);
            return this;
        }

        public Translatable build(String key){
            Translatable translatable =  new Translatable(key);
            this.cache.forEach(translatable::withAdditionalTranslation);
            return translatable;
        }
    }
}

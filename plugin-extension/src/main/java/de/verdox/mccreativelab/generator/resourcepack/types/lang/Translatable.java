package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Translatable implements GameTranslation{
    private final Map<LanguageInfo, Translation> cache = new HashMap<>();
    private final String key;

    public Translatable(LanguageInfo languageInfo, String key, String content){
        this.key = key;
        withAdditionalTranslation(languageInfo, content);
    }

    public String getTranslation(LanguageInfo languageInfo){
        if(!cache.containsKey(languageInfo))
            return "";
        return cache.get(languageInfo).content();
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
}

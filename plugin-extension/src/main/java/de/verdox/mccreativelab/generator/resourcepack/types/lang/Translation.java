package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import org.jetbrains.annotations.NotNull;

import java.text.Collator;

public record Translation(LanguageInfo languageInfo, String key, String content) implements Comparable<Translation>, GameTranslation {
    @Override
    public int compareTo(@NotNull Translation o) {
        return Collator.getInstance().compare(o.key, this.key);
    }


}

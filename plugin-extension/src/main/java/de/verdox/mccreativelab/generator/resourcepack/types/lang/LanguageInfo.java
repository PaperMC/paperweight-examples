package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import java.util.Locale;
import java.util.Objects;

public record LanguageInfo(String identifier, String name, String region, boolean bidirectional) {
    public static LanguageInfo GERMAN = new LanguageInfo("de_de", "German", "Germany", false);
    public static LanguageInfo ENGLISH_AU = new LanguageInfo("en_au", "Australian English", "Australia", false);
    public static LanguageInfo ENGLISH_CA = new LanguageInfo("en_ca", "Canadian English", "Canada", false);
    public static LanguageInfo ENGLISH_GB = new LanguageInfo("en_gp", "British English", "Great Britain", false);
    public static LanguageInfo ENGLISH_NZ = new LanguageInfo("en_nu", "New Zealand English", "New Zealand", false);
    public static LanguageInfo ENGLISH_US = new LanguageInfo("en_us", "American English", "United States", false);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageInfo that = (LanguageInfo) o;
        return Objects.equals(identifier.toLowerCase(Locale.ROOT), that.identifier.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier.toLowerCase(Locale.ROOT));
    }
}

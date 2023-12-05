package de.verdox.mccreativelab.generator.resourcepack.types.lang;

public record LanguageInfo(String identifier, String name, String region, boolean bidirectional) {
    public static LanguageInfo GERMAN = new LanguageInfo("de_de", "German", "Germany", false);
    public static LanguageInfo ENGLISH_AU = new LanguageInfo("en_au", "Australian English", "Australia", false);
    public static LanguageInfo ENGLISH_CA = new LanguageInfo("en_ca", "Canadian English", "Canada", false);
    public static LanguageInfo ENGLISH_GB = new LanguageInfo("en_gp", "British English", "Great Britain", false);
    public static LanguageInfo ENGLISH_NZ = new LanguageInfo("en_nu", "New Zealand English", "New Zealand", false);
    public static LanguageInfo ENGLISH_US = new LanguageInfo("en_us", "American English", "United States", false);
}

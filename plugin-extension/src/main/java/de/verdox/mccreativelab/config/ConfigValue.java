package de.verdox.mccreativelab.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class ConfigValue<T> {
    protected final FileConfiguration fileConfiguration;
    protected final java.lang.String key;
    @Nullable
    protected final T defaultValue;

    public ConfigValue(FileConfiguration fileConfiguration, java.lang.String key, @Nullable T defaultValue) {
        this.fileConfiguration = fileConfiguration;
        this.key = key;
        this.defaultValue = defaultValue;
        if (this.defaultValue != null) {
            addDefault(defaultValue);
        }
    }

    @Nullable
    public abstract T read();

    public abstract void set(@Nullable T value);

    protected abstract void addDefault(@NotNull T value);

    protected boolean contains(boolean ignoreDefaults){
        return fileConfiguration.contains(key, ignoreDefaults);
    }

    public static class String extends ConfigValue<java.lang.String> {
        public String(FileConfiguration fileConfiguration, java.lang.String key, @Nullable java.lang.String defaultValue) {
            super(fileConfiguration, key, defaultValue);
        }

        @Override
        public java.lang.String read() {
            return fileConfiguration.getString(key);
        }

        @Override
        public void set(java.lang.String value) {
            fileConfiguration.set(key, value);
        }

        @Override
        protected void addDefault(java.lang.@NotNull String value) {
            fileConfiguration.addDefault(key, value);
        }
    }

    public static class Boolean extends ConfigValue<java.lang.Boolean> {
        public Boolean(FileConfiguration fileConfiguration, java.lang.String key, java.lang.@Nullable Boolean defaultValue) {
            super(fileConfiguration, key, defaultValue);
        }

        @Override
        public java.lang.Boolean read() {
            return fileConfiguration.getBoolean(key);
        }

        @Override
        public void set(java.lang.Boolean value) {
            fileConfiguration.set(key, value);
        }

        @Override
        protected void addDefault(java.lang.@NotNull Boolean value) {
            fileConfiguration.addDefault(key, value);
        }
    }

    public static class Integer extends ConfigValue<java.lang.Integer> {

        public Integer(FileConfiguration fileConfiguration, java.lang.String key, java.lang.@Nullable Integer defaultValue) {
            super(fileConfiguration, key, defaultValue);
        }

        @Override
        public java.lang.Integer read() {
            return fileConfiguration.getInt(key);
        }

        @Override
        public void set(java.lang.Integer value) {
            fileConfiguration.set(key, value);
        }

        @Override
        protected void addDefault(java.lang.@NotNull Integer value) {
            fileConfiguration.addDefault(key, value);
        }
    }

    public static class Enum<E extends java.lang.Enum<E>> extends ConfigValue<E> {
        private final Class<E> type;

        public Enum(FileConfiguration fileConfiguration, Class<E> type, java.lang.String key, @Nullable E defaultValue) {
            super(fileConfiguration, key, defaultValue);
            this.type = type;
        }

        @Override
        public E read() {
            java.lang.String foundValue = fileConfiguration.getString(key);
            if (foundValue == null)
                return null;

            return java.lang.Enum.valueOf(type, foundValue.toUpperCase(Locale.ROOT));
        }

        @Override
        public void set(E value) {
            fileConfiguration.set(key, value.name().toUpperCase());
        }

        @Override
        protected void addDefault(@NotNull E value) {
            fileConfiguration.addDefault(key, value.name().toUpperCase());
        }
    }

}

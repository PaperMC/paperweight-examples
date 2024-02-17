package de.verdox.mccreativelab;

import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Wrappers {
    /**
     * Creates a Wrapped Sound
     *
     * @param soundData Custom sound
     * @return the wrapped sound
     */
    public static Sound of(@Nullable SoundData soundData) {
        if(soundData == null)
            return new Sound(org.bukkit.Sound.INTENTIONALLY_EMPTY);
        return new Sound(soundData);
    }

    /**
     * Creates a Wrapped Sound
     *
     * @param sound Bukkit sound
     * @return the wrapped sound
     */
    public static Sound of(@Nullable org.bukkit.Sound sound) {
        if(sound == null)
            return new Sound(org.bukkit.Sound.INTENTIONALLY_EMPTY);
        return new Sound(sound);
    }

    /**
     * Creates a Wrapped Sound
     *
     * @param sound Paper sound
     * @return the wrapped sound
     */
    public static Sound of(@Nullable net.kyori.adventure.sound.Sound sound) {
        if(sound == null)
            return new Sound(org.bukkit.Sound.INTENTIONALLY_EMPTY);
        return new Sound(sound);
    }

    public static SoundGroup of(org.bukkit.SoundGroup soundGroup){
        return new SoundGroup(soundGroup);
    }

    public static SoundGroup of(@NotNull Wrappers.Sound hitSound, @NotNull Wrappers.Sound stepSound, @NotNull Wrappers.Sound breakSound, @NotNull Wrappers.Sound placeSound, @NotNull Wrappers.Sound fallSound){
        return new SoundGroup(hitSound, stepSound, breakSound, placeSound, fallSound);
    }

    public static class Sound {
        private SoundData soundData;
        private org.bukkit.Sound bukkitSound;
        private net.kyori.adventure.sound.Sound paperSound;

        private Sound(@NotNull org.bukkit.Sound bukkitSound) {
            Objects.requireNonNull(bukkitSound);
            this.bukkitSound = bukkitSound;
        }

        private Sound(@NotNull SoundData soundData) {
            Objects.requireNonNull(soundData);
            this.soundData = soundData;
        }

        private Sound(@NotNull net.kyori.adventure.sound.Sound paperSound) {
            Objects.requireNonNull(paperSound);
            this.paperSound = paperSound;
        }

        public NamespacedKey getKey(){
            if(paperSound != null)
                return new NamespacedKey(paperSound.name().namespace(), paperSound.name().value());
            else if(soundData != null)
                return soundData.getKey();
            else if(bukkitSound != null)
                return bukkitSound.getKey();
            throw new IllegalStateException("No key was found. This is a bug");
        }

        /**
         * This method returns a bukkit sound if this wrapper object was initialized with one
         *
         * @return A bukkit sound
         */
        @Nullable
        public org.bukkit.Sound getBukkitSound() {
            return bukkitSound;
        }

        /**
         * This method returns a paper sound if this wrapper object was initialized with one
         *
         * @return A paper sound
         */
        @Nullable
        public net.kyori.adventure.sound.Sound getPaperSound() {
            return paperSound;
        }

        /**
         * This method returns a custom sound if this wrapper object was initialized with one
         *
         * @return A custom sound
         */
        @Nullable
        public SoundData getSoundData() {
            return soundData;
        }

        /**
         * Returns this sound wrapper was paper api sound
         *
         * @param source The source of the sound
         * @param volume The volume of the sound
         * @param pitch  The pitch of the sound
         * @return A paper sound object
         */
        public net.kyori.adventure.sound.Sound asSound(net.kyori.adventure.sound.Sound.Source source, float volume, float pitch) {
            if (this.bukkitSound != null)
                return net.kyori.adventure.sound.Sound.sound(this.bukkitSound.key(), source, volume, pitch);
            else if (this.paperSound != null)
                return net.kyori.adventure.sound.Sound.sound(paperSound.name(), source, volume, pitch);
            else
                return soundData.asSound(source, volume, pitch);
        }

        /**
         * Returns this sound wrapper was paper api sound
         *
         * @param source The source of the sound
         * @param volume The volume of the sound
         * @return A paper sound object
         */
        public net.kyori.adventure.sound.Sound asSound(net.kyori.adventure.sound.Sound.Source source, float volume) {
            return asSound(source, volume, 1.0f);
        }

        /**
         * Returns this sound wrapper was paper api sound
         *
         * @param source The source of the sound
         * @return A paper sound object
         */
        public net.kyori.adventure.sound.Sound asSound(net.kyori.adventure.sound.Sound.Source source) {
            return asSound(source, 1.0f, 1.0f);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sound sound = (Sound) o;
            return Objects.equals(asSound(net.kyori.adventure.sound.Sound.Source.BLOCK).name(), sound.asSound(net.kyori.adventure.sound.Sound.Source.BLOCK).name());
        }

        @Override
        public int hashCode() {
            return Objects.hash(asSound(net.kyori.adventure.sound.Sound.Source.BLOCK).name());
        }
    }
    public static class SoundGroup {
        private Sound hitSound;
        private Sound stepSound;
        private Sound breakSound;
        private Sound placeSound;
        private Sound fallSound;
        private org.bukkit.SoundGroup soundGroup;

        private SoundGroup(@NotNull org.bukkit.SoundGroup soundGroup) {
            Objects.requireNonNull(soundGroup);
            this.soundGroup = soundGroup;
        }

        private SoundGroup(@NotNull Wrappers.Sound hitSound, @NotNull Wrappers.Sound stepSound, @NotNull Wrappers.Sound breakSound, @NotNull Wrappers.Sound placeSound, @NotNull Wrappers.Sound fallSound) {
            Objects.requireNonNull(hitSound);
            Objects.requireNonNull(stepSound);
            Objects.requireNonNull(breakSound);
            Objects.requireNonNull(placeSound);
            Objects.requireNonNull(fallSound);
            this.hitSound = hitSound;
            this.stepSound = stepSound;
            this.breakSound = breakSound;
            this.placeSound = placeSound;
            this.fallSound = fallSound;
        }

        /**
         * Get the volume these sounds are played at.
         * <p>
         * Note that this volume does not always represent the actual volume
         * received by the client.
         *
         * @return volume
         */
        public float getVolume() {
            if(soundGroup != null)
                return soundGroup.getVolume();
            else
                return 1.0f;
        }

        /**
         * Gets the pitch these sounds are played at.
         * <p>
         * Note that this pitch does not always represent the actual pitch received
         * by the client.
         *
         * @return pitch
         */
        public float getPitch(){
            if(soundGroup != null)
                return soundGroup.getPitch();
            else
                return 1.0f;
        }

        /**
         * Gets the corresponding hit sound for this group.
         *
         * @return the hi sound
         */
        public Sound getHitSound(){
            if(soundGroup != null)
                return of(soundGroup.getHitSound());
            else
                return hitSound;
        }

        /**
         * Gets the corresponding step sound for this group.
         *
         * @return the step sound
         */
        public Sound getStepSound(){
            if(soundGroup != null)
                return of(soundGroup.getStepSound());
            else
                return stepSound;
        }

        /**
         * Gets the corresponding fall sound for this group.
         *
         * @return the fall sound
         */
        public Sound getFallSound(){
            if(soundGroup != null)
                return of(soundGroup.getFallSound());
            else
                return fallSound;
        }

        /**
         * Gets the corresponding breaking sound for this group.
         *
         * @return the break sound
         */
        public Sound getBreakSound(){
            if(soundGroup != null)
                return of(soundGroup.getBreakSound());
            else
                return breakSound;
        }

        /**
         * Gets the corresponding place sound for this group.
         *
         * @return the place sound
         */
        public Sound getPlaceSound(){
            if(soundGroup != null)
                return of(soundGroup.getPlaceSound());
            else
                return placeSound;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SoundGroup that = (SoundGroup) o;
            return Objects.equals(hitSound, that.hitSound) && Objects.equals(stepSound, that.stepSound) && Objects.equals(breakSound, that.breakSound) && Objects.equals(placeSound, that.placeSound) && Objects.equals(fallSound, that.fallSound) && Objects.equals(soundGroup, that.soundGroup);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hitSound, stepSound, breakSound, placeSound, fallSound, soundGroup);
        }
    }
}

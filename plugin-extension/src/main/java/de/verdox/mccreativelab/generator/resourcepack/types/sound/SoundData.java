package de.verdox.mccreativelab.generator.resourcepack.types.sound;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import org.bukkit.NamespacedKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundData extends ResourcePackResource {
    private final Map<NamespacedKey, SoundVariant> soundVariants = new HashMap<>();
    private boolean alreadyInstalled;
    private final boolean replace;
    private final String subtitle;
    public SoundData(NamespacedKey namespacedKey, boolean replace, String subtitle) {
        super(namespacedKey);
        if(!namespacedKey.namespace().equals("minecraft"))
            throw new IllegalArgumentException("Sounds can only be installed in minecraft namespace");
        this.replace = replace;
        this.subtitle = subtitle;
    }
    public SoundData withSoundVariant(NamespacedKey namespacedKey, Asset<CustomResourcePack> soundAsset, float volume, float pitch){
        if(alreadyInstalled)
            throw new IllegalStateException("Cannot add more sounds during runtime.");
        soundVariants.put(namespacedKey, new SoundVariant(namespacedKey, soundAsset, volume, pitch));
        return this;
    }

    @Override
    public void installToDataPack(CustomResourcePack customPack) throws IOException {
        alreadyInstalled = true;
        for (NamespacedKey namespacedKey : soundVariants.keySet()) {
            SoundVariant soundVariant = soundVariants.get(namespacedKey);
            soundVariant.soundAsset().installAsset(customPack, namespacedKey, ResourcePackAssetTypes.SOUNDS, "ogg");
        }
    }
    public Map<NamespacedKey, SoundVariant> getSoundVariants() {
        return soundVariants;
    }
    public String getSubtitle() {
        return subtitle;
    }
    public boolean isReplace() {
        return replace;
    }

    public record SoundVariant(NamespacedKey namespacedKey, Asset<CustomResourcePack> soundAsset, float volume, float pitch){}
}

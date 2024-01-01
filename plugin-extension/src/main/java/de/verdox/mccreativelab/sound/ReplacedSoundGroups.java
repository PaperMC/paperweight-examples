package de.verdox.mccreativelab.sound;

import de.verdox.mccreativelab.Wrappers;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.sound.SoundData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

public class ReplacedSoundGroups {
    private static final Map<Wrappers.SoundGroup, Wrappers.SoundGroup> replacedSoundGroups = new HashMap<>();

    static {
        replaceGlassSoundGroup();
        replaceWoodSoundGroup();
    }

    private static void replaceGlassSoundGroup(){
        SoundData newGlassBreakSound = new SoundData(new NamespacedKey("minecraft", "block.glass.custom.break"), false, "subtitles.block.generic.break")
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/glass/break/glass1"), new Asset<>("/sounds/glass/break/glass1.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/glass/break/glass2"), new Asset<>("/sounds/glass/break/glass2.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/glass/break/glass3"), new Asset<>("/sounds/glass/break/glass3.ogg"), 1, 1)
            ;

        Wrappers.SoundGroup newGlassSoundGroup = Wrappers.of(Wrappers.of(Sound.BLOCK_STONE_HIT), Wrappers.of(Sound.BLOCK_STONE_STEP), Wrappers.of(newGlassBreakSound), Wrappers.of(Sound.BLOCK_STONE_PLACE), Wrappers.of(Sound.BLOCK_STONE_FALL));
        replaceSoundGroup("block.glass", Material.GLASS.createBlockData().getSoundGroup(), newGlassSoundGroup);
    }

    private static void replaceWoodSoundGroup(){
        SoundData newWoodDigSound = new SoundData(new NamespacedKey("minecraft", "block.wood.custom.break"), false, "subtitles.block.generic.break")
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/dig/wood1"), new Asset<>("/sounds/wood/dig/wood1.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/dig/wood2"), new Asset<>("/sounds/wood/dig/wood2.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/dig/wood3"), new Asset<>("/sounds/wood/dig/wood3.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/dig/wood4"), new Asset<>("/sounds/wood/dig/wood4.ogg"), 1, 1)
            ;

        SoundData newWoodStepSound = new SoundData(new NamespacedKey("minecraft", "block.wood.custom.step"), false, "subtitles.block.generic.step")
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/step/wood1"), new Asset<>("/sounds/wood/step/wood1.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/step/wood2"), new Asset<>("/sounds/wood/step/wood2.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/step/wood3"), new Asset<>("/sounds/wood/step/wood3.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/step/wood4"), new Asset<>("/sounds/wood/step/wood4.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/step/wood5"), new Asset<>("/sounds/wood/step/wood5.ogg"), 1, 1)
            .withSoundVariant(new NamespacedKey("minecraft", "block/custom/wood/step/wood6"), new Asset<>("/sounds/wood/step/wood6.ogg"), 1, 1)
            ;


        Wrappers.SoundGroup newWoodSoundGroup = Wrappers.of(
            Wrappers.of(newWoodStepSound),
            Wrappers.of(newWoodStepSound),
            Wrappers.of(newWoodDigSound),
            Wrappers.of(newWoodDigSound),
            Wrappers.of(Sound.BLOCK_STONE_FALL)
        );
        replaceSoundGroup("block.wood", Material.OAK_LOG.createBlockData().getSoundGroup(), newWoodSoundGroup);
    }

    public static void init() {

    }

    public static boolean wasSoundReplaced(SoundGroup soundGroup) {
        return replacedSoundGroups.containsKey(Wrappers.of(soundGroup));
    }

    public static Wrappers.SoundGroup getSoundGroup(BlockData blockData){
        if(!wasSoundReplaced(blockData.getSoundGroup()))
            return Wrappers.of(blockData.getSoundGroup());
        return replacedSoundGroups.get(Wrappers.of(blockData.getSoundGroup()));
    }

    public static void replaceSoundGroup(String groupParentName, SoundGroup bukkitSoundGroup, Wrappers.SoundGroup soundGroup) {
        CustomResourcePack customResourcePack = MCCreativeLabExtension.getInstance().getCustomResourcePack();

        customResourcePack.register(new SoundData(NamespacedKey.minecraft(groupParentName + ".break"), true, "subtitles.block.generic.break"));
        customResourcePack.register(new SoundData(NamespacedKey.minecraft(groupParentName + ".hit"), true, "subtitles.block.generic.hit"));
        customResourcePack.register(new SoundData(NamespacedKey.minecraft(groupParentName + ".place"), true, "subtitles.block.generic.place"));
        customResourcePack.register(new SoundData(NamespacedKey.minecraft(groupParentName + ".step"), true, "subtitles.block.generic.footsteps"));

        if (soundGroup.getBreakSound().getSoundData() != null)
            customResourcePack.register(soundGroup.getBreakSound().getSoundData());
        if (soundGroup.getHitSound().getSoundData() != null)
            customResourcePack.register(soundGroup.getHitSound().getSoundData());
        if (soundGroup.getPlaceSound().getSoundData() != null)
            customResourcePack.register(soundGroup.getPlaceSound().getSoundData());
        if (soundGroup.getStepSound().getSoundData() != null)
            customResourcePack.register(soundGroup.getStepSound().getSoundData());
        if (soundGroup.getFallSound().getSoundData() != null)
            customResourcePack.register(soundGroup.getFallSound().getSoundData());
        replacedSoundGroups.put(Wrappers.of(bukkitSoundGroup), soundGroup);
    }
}

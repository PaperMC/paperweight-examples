package de.verdox.mccreativelab.features.legacy.herobrine;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.features.legacy.LegacyFeature;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public class HerobrineFeature extends LegacyFeature {
    private HerobrineModel herobrineModel;
    @Override
    protected void onEnable() {
        herobrineModel = new HerobrineModel(new NamespacedKey("mccreativelab", "herobrine"), new Asset<>("/herobrine/texture/herobrine.png"));
        MCCreativeLabExtension.getCustomResourcePack().register(herobrineModel);
        addModelFiles();
    }

    public void testSpawnHerobrineModel(Location location){
        herobrineModel.summon(location);
    }

    private void addModelFiles() {
        addModelFile("template/body");
        addModelFile("template/eyes");
        addModelFile("template/head");
        addModelFile("template/left_arm");
        addModelFile("template/left_arm_slim");

        addModelFile("template/right_arm");
        addModelFile("template/right_arm_slim");

        addModelFile("template/left_leg");
        addModelFile("template/right_leg");
    }

    private void addModelFile(String key) {
        Asset<CustomResourcePack> modelAsset = new Asset<>("/herobrine/model/" + key + ".json");
        AssetBasedResourcePackResource assetBasedResourcePackResource = new AssetBasedResourcePackResource(new NamespacedKey("mccreativelab", "herobrine/" + key), modelAsset, ResourcePackAssetTypes.MODELS, "json");
        MCCreativeLabExtension.getCustomResourcePack().register(assetBasedResourcePackResource);
    }
}

package de.verdox.mccreativelab.features.legacy.herobrine;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.*;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.util.gson.JsonObjectBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HerobrineModel extends ResourcePackResource {
    private final Asset<CustomResourcePack> fullSkinTexture;
    private final Map<ModelPart, ItemTextureData> modelParts = new HashMap<>();

    public HerobrineModel(NamespacedKey namespacedKey, Asset<CustomResourcePack> fullSkinTexture) {
        super(namespacedKey);
        this.fullSkinTexture = fullSkinTexture;
    }

    public void summon(Location location) {
        for (ModelPart value : ModelPart.values()) {
            spawnModelType(value, location);
        }
    }

/*    private ItemDisplay spawnModelType(ModelPart modelPart, Location location){
        ItemTextureData itemTextureData = modelParts.get(modelPart);
        ItemStack stack = itemTextureData.createItem();
        //Location spawnLoc = location.clone().add(modelPart.getXOffset(), modelPart.getYOffset(), modelPart.getZOffset());
        ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
        itemDisplay.setItemStack(stack);

        itemDisplay.setPersistent(false);
        return itemDisplay;
    }*/

    private ArmorStand spawnModelType(ModelPart modelPart, Location location) {
        ItemTextureData itemTextureData = modelParts.get(modelPart);
        ItemStack stack = itemTextureData.createItem();
        //Location spawnLoc = location.clone().add(modelPart.getXOffset(), modelPart.getYOffset(), modelPart.getZOffset());
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setDisabledSlots();
        armorStand.setHeadPose(new EulerAngle(modelPart.getXOffset(), modelPart.getYOffset(), modelPart.getZOffset()));
        for (EquipmentSlot value : EquipmentSlot.values()) {
            armorStand.addDisabledSlots(value);
        }
        armorStand.setItem(EquipmentSlot.HEAD, stack);


        armorStand.setPersistent(false);
        return armorStand;
    }

    @Override
    public void beforeResourceInstallation(CustomResourcePack customPack) throws IOException {
        NamespacedKey herobrineSkinKey = new NamespacedKey("mccreativelab", "item/herobrine/default");
        AssetBasedResourcePackResource assetBasedResourcePackResource = new AssetBasedResourcePackResource(herobrineSkinKey, fullSkinTexture, ResourcePackAssetTypes.TEXTURES, "png");

        for (ModelPart value : ModelPart.values()) {
            ItemTextureData model = createItem(value, herobrineSkinKey);
            modelParts.put(value, model);
            MCCreativeLabExtension.getCustomResourcePack().register(model);
        }
        MCCreativeLabExtension.getCustomResourcePack().register(assetBasedResourcePackResource);
    }

    @Override
    public void installResourceToPack(CustomResourcePack customPack) throws IOException {

    }

    private ItemTextureData createItem(ModelPart modelPart, NamespacedKey herobrineSkinTexture) {
        ItemTextureData.ModelType modelType = createModel(modelPart, herobrineSkinTexture);

        return new ItemTextureData(new NamespacedKey("mccreativelab", "herobrine/default/" + modelPart.name()
                                                                                                      .toLowerCase()), Material.DISC_FRAGMENT_5, CustomModelDataProvider.drawCustomModelData(Material.DISC_FRAGMENT_5), null, modelType);
    }

    private ItemTextureData.ModelType createModel(ModelPart modelPart, NamespacedKey herobrineSkinTexture) {
        String parent = "mccreativelab:herobrine/template/" + modelPart.name().toLowerCase(Locale.ROOT);
        return new ItemTextureData.ModelType(null, (namespacedKey, jsonObject) -> {
            JsonObjectBuilder.create(jsonObject)
                             .add("parent", parent)
                             .add("textures", JsonObjectBuilder.create().add("skin", herobrineSkinTexture.asString()));
        });
    }
}

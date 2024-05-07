package de.verdox.mccreativelab.features.gui;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.Locale;

public class GUIButtons {

    private static final String BOPXIX_PACKAGE = "BopxixV3";
    private static final String VERDOX_PACKAGE = "Verdox";

    public static final ItemTextureData ARROW_DOWN = registerButton(BOPXIX_PACKAGE, "arrow_down_button");
    public static final ItemTextureData ARROW_LEFT = registerButton(BOPXIX_PACKAGE, "arrow_left_button");
    public static final ItemTextureData ARROW_RIGHT = registerButton(BOPXIX_PACKAGE, "arrow_right_button");
    public static final ItemTextureData ARROW_UP = registerButton(BOPXIX_PACKAGE, "arrow_up_button");
    public static final ItemTextureData CANCEL = registerButton(BOPXIX_PACKAGE, "cancel_button");
    public static final ItemTextureData CHAT = registerButton(BOPXIX_PACKAGE, "chat_button");
    public static final ItemTextureData CHECK = registerButton(BOPXIX_PACKAGE, "check_button");
    public static final ItemTextureData EXCLAMATION = registerButton(BOPXIX_PACKAGE, "exclamation_button");
    public static final ItemTextureData FRIENDS = registerButton(BOPXIX_PACKAGE, "friends_button");
    public static final ItemTextureData HOME = registerButton(BOPXIX_PACKAGE, "home_button");
    public static final ItemTextureData MENU = registerButton(BOPXIX_PACKAGE, "menu_button");
    public static final ItemTextureData NOTIFICATION = registerButton(BOPXIX_PACKAGE, "notification_button");
    public static final ItemTextureData QUESTION = registerButton(BOPXIX_PACKAGE, "question_button");
    public static final ItemTextureData REDO = registerButton(BOPXIX_PACKAGE, "redo_button");
    public static final ItemTextureData REFRESH = registerButton(BOPXIX_PACKAGE, "refresh_button");
    public static final ItemTextureData TRUE = registerButton(BOPXIX_PACKAGE, "right_button");
    public static final ItemTextureData SEARCH = registerButton(BOPXIX_PACKAGE, "search_button");
    public static final ItemTextureData SETTINGS = registerButton(BOPXIX_PACKAGE, "settings_button");
    public static final ItemTextureData UNDO = registerButton(BOPXIX_PACKAGE, "undo_button");
    public static final ItemTextureData FALSE = registerButton(BOPXIX_PACKAGE, "wrong_button");

    public static final ItemTextureData PLUS = registerButton(VERDOX_PACKAGE, "plus_button");

    public static void init() {


    }

    private static ItemTextureData registerButton(String packageID, String id) {
        ItemTextureData itemTextureData = new ItemTextureData(new NamespacedKey(packageID.toLowerCase(Locale.ROOT), "item/gui/buttons/" + id), Material.STICK, new Asset<>("/gui/buttons/" + packageID + "/" + id + ".png"), ItemTextureData.ModelType.CLICKABLE_ITEM);
        MCCreativeLabExtension.getCustomResourcePack().register(itemTextureData);
        return itemTextureData;
    }
}

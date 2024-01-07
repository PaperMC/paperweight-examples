package de.verdox.mccreativelab.item;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.item.impl.DebugItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class FakeItems {
    public static final FakeItem DEBUG_ITEM = MCCreativeLabExtension
        .getFakeItemRegistry()
        .register(
            new FakeItem.Builder<>(new NamespacedKey("mccreativelab", "debug_item"), Material.STICK, DebugItem.class)
                .withProperties(new FakeItem.FakeItemProperties().stacksTo(4).fireResistant())
                .withItemMeta(meta -> meta.displayName(Component.text("Debug Item")))
        );

    public static void init() {
    }
}

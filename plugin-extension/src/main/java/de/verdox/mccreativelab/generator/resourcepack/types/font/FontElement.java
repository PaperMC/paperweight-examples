package de.verdox.mccreativelab.generator.resourcepack.types.font;

import de.verdox.mccreativelab.util.gson.JsonArrayBuilder;
import org.bukkit.NamespacedKey;

public interface FontElement {
    void buildToProviders(NamespacedKey namespacedKey, JsonArrayBuilder providers);
}

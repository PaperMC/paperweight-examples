package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translation;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class LoreUtil {
    public static List<TextComponent> createLore(@NotNull String text, int linesPerRow, @NotNull Function<TextComponent, TextComponent> afterConversion) {
        List<String> formatted = StringAlign.formatStringToLines(text, linesPerRow, StringAlign.Alignment.LEFT);
        return formatted
            .stream()
            .map(Component::text)
            .map(textComponent -> textComponent.decoration(TextDecoration.ITALIC, false))
            .map(afterConversion)
            .toList();
    }

    public static List<TextComponent> createLore(@NotNull String text, @NotNull Function<TextComponent, TextComponent> afterConversion) {
        return createLore(text, 20, afterConversion);
    }

    public static List<TextComponent> createLore(@NotNull String text, int linesPerRow) {
        return createLore(text, linesPerRow, textComponent -> textComponent);
    }

    public static List<TextComponent> createLore(@NotNull String text) {
        return createLore(text, 20, textComponent -> textComponent);
    }

    public static ItemStack formatItem(Player player, ItemStack stack, Translation title, Translation description) {
        stack.editMeta(itemMeta -> {
            itemMeta.displayName(title.asTranslatableComponent());
            createLore(description.translate(player));
        });
        return stack;
    }
}

package de.verdox.mccreativelab.generator.resourcepack.types.lang;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.util.LoreUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.LinkedList;
import java.util.List;

public record Translatable(LanguageInfo languageInfo, String key, String content) implements Comparable<Translatable> {
    @Override
    public int compareTo(@NotNull Translatable o) {
        return Collator.getInstance().compare(o.key, this.key);
    }

    public String translate(Player player){
        return MCCreativeLabExtension.getCustomResourcePack().getLanguageStorage().translate(key, player);
    }

    public TextComponent translateToComponent(Player player){
        return MCCreativeLabExtension.getCustomResourcePack().getLanguageStorage().translateToComponent(key, player);
    }

    public TranslatableComponent asTranslatableComponent(){
        return Component.translatable(key);
    }

    public static List<Translatable> createTranslatableLines(String key, String content, int amountLines){
        return createTranslatableLines(LanguageInfo.ENGLISH_US, key, content, amountLines);
    }

    public static List<Translatable> createTranslatableLines(String key, String content){
        return createTranslatableLines(LanguageInfo.ENGLISH_US, key, content, 20);
    }

    public static List<Translatable> createTranslatableLines(LanguageInfo languageInfo, String key, String content, int amountLines){
        List<Translatable> list = new LinkedList<>();
        List<TextComponent> loreLines = LoreUtil.createLore(content, amountLines);
        int counter = 0;
        for (TextComponent loreLine : loreLines) {
            list.add(new Translatable(languageInfo, key+".lines."+counter, loreLine.content()));
            counter++;
        }
        return list;
    }

    public static List<TranslatableComponent> toComponent(List<Translatable> list){
        return list.stream().map(Translatable::asTranslatableComponent)
            .map(translatableComponent -> translatableComponent.decoration(TextDecoration.ITALIC, false))
            .map(translatableComponent -> translatableComponent.color(TextColor.color(255, 255, 255)))
            .toList();
    }
}

package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.registry.CustomRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;

public class RegistryLookUpCommand<T> extends Command {
    private final CustomRegistry<T> registry;
    private final BiConsumer<Player, T> consumeEntry;

    public RegistryLookUpCommand(@NotNull String name, CustomRegistry<T> registry, BiConsumer<Player, T> consumeEntry) {
        super(name);
        Objects.requireNonNull(registry);
        Objects.requireNonNull(consumeEntry);
        this.registry = registry;
        this.consumeEntry = consumeEntry;
        setPermission("mccreativelab.command.registry.lookup."+getName().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player) || !player.hasPermission("mccreativelab.command.registry.lookup."+getName().toLowerCase(Locale.ROOT)))
            return false;
        if(args.length == 0) {
            sender.sendMessage("Please provide a valid entry");
            return false;
        }
        if(args[0].equalsIgnoreCase("get")){
            if(args.length == 2){
                String keyAsString = args[1];
                try{
                    NamespacedKey namespacedKey = NamespacedKey.fromString(keyAsString);
                    if(!registry.contains(namespacedKey)){
                        sender.sendMessage("Please provide a valid entry");
                        return false;
                    }
                    T entry = registry.get(namespacedKey);
                    consumeEntry.accept(player, entry);
                }
                catch (Exception e){
                    sender.sendMessage("Please provide a valid entry");
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if(args.length <= 1)
            return List.of("get");
        if(args.length == 2)
            return registry.streamKeys().map(NamespacedKey::asString).toList();
        return List.of();
    }
}

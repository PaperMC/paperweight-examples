package de.verdox.mccreativelab.debug;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.ItemBehaviour;
import de.verdox.mccreativelab.debug.Debug;
import de.verdox.mccreativelab.world.item.FakeItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FakeItemCommand extends Command {
    public FakeItemCommand() {
        super("fakeItem");
        setPermission("mccreativelab.command.fakeitem");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!sender.hasPermission("mccreativelab.command.fakeitem"))
            return false;
        if(!(sender instanceof Player player))
            return false;
        if(args.length == 0) {
            player.sendMessage("");
            return false;
        }
        if(args[0].equalsIgnoreCase("get")){
            if(args.length == 2){
                String keyAsString = args[1];
                try{
                    NamespacedKey namespacedKey = NamespacedKey.fromString(keyAsString);
                    FakeItem fakeItem = MCCreativeLabExtension.getFakeItemRegistry().get(namespacedKey);
                    player.getInventory().addItem(fakeItem.createItemStack());
                }
                catch (Exception e){
                    sender.sendMessage("Please provide a valid custom item");
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
            return MCCreativeLabExtension.getFakeItemRegistry().streamKeys().map(NamespacedKey::asString).filter(s -> s.contains(args[1])).toList();
        return List.of();
    }
}

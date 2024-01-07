package de.verdox.mccreativelab.item;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FakeItemCommand extends Command {
    public FakeItemCommand() {
        super("fakeItem");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player))
            return false;
        if(args.length == 0)
            return false;
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("get")){
                player.getInventory().addItem(FakeItems.DEBUG_ITEM.createItemStack());
            }
        }
        return false;
    }
}

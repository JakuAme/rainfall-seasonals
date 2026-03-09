package com.blockymmc.rainfall;

import com.blockymmc.rainfall.items.SeasonalItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /rainfall <player> <itemId>");
            return true;
        }

        String playerName = args[0];
        String itemId = args[1];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("Player not found: " + playerName);
            return true;
        }

        if (!SeasonalItemRegistry.exists(itemId)) {
            sender.sendMessage("Unknown item: " + itemId);
            return true;
        }

        ItemStack item = SeasonalItemRegistry.build(itemId);
        if (item == null) {
            sender.sendMessage("Failed to build item: " + itemId + " (Nexo item not found?)");
            return true;
        }

        target.getInventory().addItem(item).values()
            .forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));

        return true;
    }
}

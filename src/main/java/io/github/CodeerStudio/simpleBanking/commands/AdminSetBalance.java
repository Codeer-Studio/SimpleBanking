package io.github.CodeerStudio.simpleBanking.commands;

import io.github.CodeerStudio.simpleBanking.handlers.BankManagerHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminSetBalance implements CommandExecutor {

    private final BankManagerHandler bankManagerHandler;

    public AdminSetBalance(BankManagerHandler bankManagerHandler) {
        this.bankManagerHandler = bankManagerHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simplebanking.admin.set")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setbankbalance <player> <amount>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
            return true;
        }

        bankManagerHandler.setBalance(target, amount);
        sender.sendMessage(ChatColor.GREEN + "Successfully set " + target.getName() + "'s balance to " + amount + ".");
        return true;
    }
}

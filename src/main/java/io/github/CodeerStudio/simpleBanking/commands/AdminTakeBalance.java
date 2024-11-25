package io.github.CodeerStudio.simpleBanking.commands;

import io.github.CodeerStudio.simpleBanking.handlers.BankManagerHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminTakeBalance implements CommandExecutor {

    private final BankManagerHandler bankManagerHandler;

    public AdminTakeBalance(BankManagerHandler bankManagerHandler) {
        this.bankManagerHandler = bankManagerHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simplebanking.admin.take")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /takebankbalance <player> <amount>");
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

        bankManagerHandler.handleWithdraw(target, amount);
        sender.sendMessage(ChatColor.GREEN + "Successfully took " + amount + " from " + target.getName() + ".");
        return true;
    }
}

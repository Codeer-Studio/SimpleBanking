package io.github.CodeerStudio.simpleBanking.commands;

import io.github.CodeerStudio.simpleBanking.SimpleBanking;
import io.github.CodeerStudio.simpleBanking.gui.BankMenuGUI;
import io.github.CodeerStudio.simpleBanking.handlers.BankManagerHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

/**
 * This class handles the bank menu GUI and player interactions with it.
 */
public class BankMenu implements Listener, CommandExecutor {

    private final SimpleBanking plugin;

    /**
     * Creates a new instance of BankMenu, registers the event listener, and links the plugin.
     *
     * @param plugin The SimpleBanking plugin instance.
     */
    public BankMenu(SimpleBanking plugin) {
        this.plugin = plugin; // Assign the plugin instance to the field
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the execution of the "/bank" command. Opens the bank inventory for the player.
     *
     * @param sender The entity that executed the command.
     * @param command The command that was executed.
     * @param label The alias used to execute the command.
     * @param args Arguments passed with the command.
     * @return true if the command was handled successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        // Create and populate the inventory
        Inventory inventory = BankMenuGUI.createBankMenu(player);
        player.openInventory(inventory);

        // Open the inventory for the player
        player.openInventory(inventory);

        return true;
    }

    /**
     * Handles clicks in the bank inventory. Performs actions based on the clicked slot.
     *
     * @param event The inventory click event triggered by a player.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Ensure the inventory is the bank menu
        if (!event.getView().getTitle().equals(BankMenuGUI.getInventoryName())) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        event.setCancelled(true);

        // Determine action based on the clicked slot
        switch (slot) {
            case BankMenuGUI.DEPOSIT_SLOT -> {
                player.closeInventory(); // Optional: close inventory if necessary
                player.sendMessage(ChatColor.GREEN + "Enter the amount to deposit in the chat.");
                promptForAmount(player, "deposit");
            }
            case BankMenuGUI.WITHDRAW_SLOT -> {
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Enter the amount to withdraw in the chat.");
                promptForAmount(player, "withdraw");
            }
            case BankMenuGUI.INFORMATION_SLOT -> {
                player.sendMessage(ChatColor.YELLOW + "Fetching your bank information...");
                // Show the player their balance or other details
                BankManagerHandler bankManagerHandler = new BankManagerHandler(plugin);
                bankManagerHandler.showBankInformation(player);
            }
            default -> player.sendMessage(ChatColor.RED + "This slot does not perform any action.");
        }
    }

    /**
     * Prompts the player for an amount and listens for their input via chat.
     *
     * @param player The player to prompt.
     * @param action The action to perform ("deposit" or "withdraw").
     */
    private void promptForAmount(Player player, String action) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent chatEvent) {
                if (!chatEvent.getPlayer().equals(player)) return;

                chatEvent.setCancelled(true);
                String input = chatEvent.getMessage();
                double amount;

                // Validate the input as a proper number
                try {
                    amount = Double.parseDouble(input);

                    // Check if the number is positive
                    if (amount <= 0) {
                        player.sendMessage(ChatColor.RED + "The amount must be greater than zero. Try again.");
                        return;
                    }

                    // Check for up to two decimal places
                    if (!hasTwoDecimalPlacesOrLess(amount)) {
                        player.sendMessage(ChatColor.RED + "The amount can only have up to two decimal places. Try again.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number. Please enter a valid amount.");
                    return;
                }

                // Perform the action
                BankManagerHandler bankManagerHandler = new BankManagerHandler(plugin);
                if (action.equals("deposit")) {
                    bankManagerHandler.depositMoney(player, amount);
                } else if (action.equals("withdraw")) {
                    bankManagerHandler.handleWithdraw(player, amount);
                }

                // Unregister the listener after handling the input
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
            }
        }, plugin);
    }

    /**
     * Validates if a number has at most two decimal places.
     *
     * @param value The number to validate.
     * @return True if the number has up to two decimal places, false otherwise.
     */
    private boolean hasTwoDecimalPlacesOrLess(double value) {
        String[] parts = String.valueOf(value).split("\\.");
        return parts.length <= 1 || parts[1].length() <= 2;
    }
}

package io.github.CodeerStudio.simpleBanking.commands;

import io.github.CodeerStudio.simpleBanking.SimpleBanking;
import io.github.CodeerStudio.simpleBanking.handlers.VaultAPIHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the bank menu GUI and player interactions with it.
 * It allows players to deposit and withdraw money into/from their bank account.
 */
public class BankMenu implements Listener, CommandExecutor {

    private final String inventoryName = ChatColor.translateAlternateColorCodes('&', "&6Bank");
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_SIZE = INVENTORY_ROWS * 9;
    private static final int DEPOSIT_SLOT = 11;
    private static final int INFORMATION_SLOT = 13;
    private static final int WITHDRAW_SLOT = 15;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        // Create and populate the inventory
        Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, inventoryName);
        populateInventory(inventory);

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
        if (!event.getView().getTitle().equals(inventoryName)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        event.setCancelled(true);

        switch (slot) {
            case DEPOSIT_SLOT -> handleDeposit(player);
            case WITHDRAW_SLOT -> handleWithdraw(player);
            case INFORMATION_SLOT -> showBankInformation(player);
            default -> player.sendMessage(ChatColor.YELLOW + "This slot does nothing.");
        }
    }

    /**
     * Populates the bank inventory with items representing actions (deposit, withdraw, information).
     *
     * @param inventory The inventory to populate.
     */
    private void populateInventory(Inventory inventory) {
        inventory.setItem(11, getItem(
                new ItemStack(Material.CHEST),
                "&6Bank Vault",
                "&7Deposit your money."
        ));

        // Add a sign to slot 13 for bank information
        inventory.setItem(13, getItem(
                new ItemStack(Material.OAK_SIGN),
                "&cBank Information",
                "&7Check the latest bank news."
        ));

        // Add a dispenser to slot 15 for withdrawal
        inventory.setItem(15, getItem(
                new ItemStack(Material.DISPENSER),
                "&6Bank Vault",
                "&7Withdraw your money."
        ));
    }

    /**
     * Creates an ItemStack with the specified name and lore, and sets its metadata.
     *
     * @param item The item to modify.
     * @param name The name to assign to the item.
     * @param lore Optional lore to assign to the item.
     * @return The modified ItemStack.
     */
    private ItemStack getItem(ItemStack item, String name, String... lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Set the display name
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // Set the lore if provided
        if (lore.length > 0) {
            List<String> lores = new ArrayList<>();
            for (String s : lore) {
                lores.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setLore(lores);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Handles depositing money into the player's bank account.
     * It withdraws money from the player and updates their balance in the database.
     *
     * @param player The player performing the deposit.
     */
    private void handleDeposit(Player player) {
        double amountToDeposit = 100.0;
        Economy economy = VaultAPIHandler.getEconomy();

        if (economy.getBalance(player) < amountToDeposit) {
            player.sendMessage(ChatColor.RED + "You don't have enough money to deposit!");
            return;
        }

        economy.withdrawPlayer(player, amountToDeposit);

        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement(
                "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance = balance + ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setDouble(2, amountToDeposit);
            stmt.setDouble(3, amountToDeposit);
            stmt.executeUpdate();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while depositing your money.");
            e.printStackTrace();
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Successfully deposited " + amountToDeposit + " into your bank!");
    }

    /**
     * Handles withdrawing money from the player's bank account.
     * It checks the player's balance in the database and updates it when the withdrawal is successful.
     *
     * @param player The player performing the withdrawal.
     */
    private void handleWithdraw(Player player) {
        double amountToWithdraw = 100.0; // Example amount
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement(
                "SELECT balance FROM player_balances WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getDouble("balance") < amountToWithdraw) {
                    player.sendMessage(ChatColor.RED + "You don't have enough money in the bank!");
                    return;
                }
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while withdrawing your money.");
            e.printStackTrace();
            return;
        }

        try (PreparedStatement updateStmt = plugin.getDatabaseConnection().prepareStatement(
                "UPDATE player_balances SET balance = balance - ? WHERE uuid = ?")) {
            updateStmt.setDouble(1, amountToWithdraw);
            updateStmt.setString(2, player.getUniqueId().toString());
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while withdrawing your money.");
            e.printStackTrace();
            return;
        }

        VaultAPIHandler.getEconomy().depositPlayer(player, amountToWithdraw);
        player.sendMessage(ChatColor.GREEN + "Successfully withdrew " + amountToWithdraw + " from your bank!");
    }

    /**
     * Displays the player's bank balance by querying the database.
     *
     * @param player The player requesting their balance.
     */
    private void showBankInformation(Player player) {
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement(
                "SELECT balance FROM player_balances WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    player.sendMessage(ChatColor.GOLD + "Your bank balance is: " + balance);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have a bank account yet.");
                }
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while fetching your balance.");
            e.printStackTrace();
        }
    }
}

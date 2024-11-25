package io.github.CodeerStudio.simpleBanking.handlers;

import io.github.CodeerStudio.simpleBanking.SimpleBanking;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles core banking operations such as deposits and withdrawals.
 * This class serves as the manager for interacting with the player's bank account
 * stored in the database, ensuring safe updates and balance checks.
 */
public class BankManagerHandler {

    private final SimpleBanking plugin;

    /**
     * Constructs a new BankManagerHandler instance.
     *
     * @param plugin The main plugin instance, used for accessing the database connection.
     */
    public BankManagerHandler(SimpleBanking plugin) {
        this.plugin = plugin;
    }

    /**
     * Deposits money from a player's wallet into their bank account.
     *
     * This method checks if the player has sufficient funds in their wallet before
     * transferring the specified amount to their bank account in the database.
     *
     * @param player The player performing the deposit.
     * @param amount The amount of money to deposit.
     */
    public void depositMoney(Player player, double amount) {
        Economy economy = VaultAPIHandler.getEconomy();

        // Check if the player has enough money in their wallet
        if (economy.getBalance(player) < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough money to deposit!");
            return;
        }

        // Deduct money from the player's wallet
        economy.withdrawPlayer(player, amount);

        // Update the player's bank balance in the database
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement(
                "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance = balance + ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setDouble(2, amount);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while depositing your money.");
            e.printStackTrace();
            return;
        }

        // Notify the player of a successful deposit
        player.sendMessage(ChatColor.GREEN + "Successfully deposited " + amount + " into your bank!");
    }

    /**
     * Withdraws money from a player's bank account to their wallet.
     *
     * This method ensures the player has sufficient balance in their bank account
     * before updating the database and transferring the amount to their wallet.
     *
     * @param player The player performing the withdrawal.
     * @param amount The amount of money to withdraw.
     */
    public void handleWithdraw(Player player, double amount) {
        Economy economy = VaultAPIHandler.getEconomy();

        // Check the player's bank balance in the database
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement(
                "SELECT balance FROM player_balances WHERE uuid = ?")) {
            stmt.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getDouble("balance") < amount) {
                    player.sendMessage(ChatColor.RED + "You don't have enough money in the bank!");
                    return;
                }
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while withdrawing your money.");
            e.printStackTrace();
            return;
        }

        // Deduct money from the player's bank account in the database
        try (PreparedStatement updateStmt = plugin.getDatabaseConnection().prepareStatement(
                "UPDATE player_balances SET balance = balance - ? WHERE uuid = ?")) {
            updateStmt.setDouble(1, amount);
            updateStmt.setString(2, player.getUniqueId().toString());
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while withdrawing your money.");
            e.printStackTrace();
            return;
        }

        // Add the money to the player's wallet
        VaultAPIHandler.getEconomy().depositPlayer(player, amount);

        // Notify the player of a successful withdrawal
        player.sendMessage(ChatColor.GREEN + "Successfully withdrew " + amount + " from your bank!");
    }

    /**
     * Sets the player's bank balance to a specified amount.
     *
     * @param player The player whose balance is being set.
     * @param amount The amount to set the player's balance to.
     */
    public void setBalance(Player player, double amount) {
        if (amount < 0) {
            player.sendMessage(ChatColor.RED + "Balance cannot be set to a negative value.");
            return;
        }

        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement(
                "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance = ?")) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setDouble(2, amount);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while setting your balance.");
            e.printStackTrace();
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your bank balance has been set to " + amount + ".");
    }

    /**
     * Displays the player's bank balance by querying the database.
     *
     * @param player The player requesting their balance.
     */
    public void showBankInformation(Player player) {
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

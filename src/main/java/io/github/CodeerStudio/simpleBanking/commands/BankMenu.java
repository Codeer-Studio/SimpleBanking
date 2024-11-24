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

public class BankMenu implements Listener, CommandExecutor {

    private final String inventoryName = ChatColor.translateAlternateColorCodes('&', "&6Bank");
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_SIZE = INVENTORY_ROWS * 9;
    private static final int DEPOSIT_SLOT = 11;
    private static final int INFORMATION_SLOT = 13;
    private static final int WITHDRAW_SLOT = 15;
    private final SimpleBanking plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        Inventory inventory = Bukkit.createInventory(player, 9 * 3, inventoryName);
        populateInventory(inventory);

        player.openInventory(inventory);

        return true;
    }

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

    public BankMenu(SimpleBanking plugin) {
        this.plugin = plugin; // Assign the plugin instance to the field
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    private void populateInventory(Inventory inventory) {
        inventory.setItem(11, getItem(
                new ItemStack(Material.CHEST),
                "&6Bank Vault",
                "&7Deposit your money."
        ));

        // Add a sign to slot 13
        inventory.setItem(13, getItem(
                new ItemStack(Material.OAK_SIGN),
                "&cBank Information",
                "&7Check the latest bank news."
        ));

        // Add a dispenser to slot 15
        inventory.setItem(15, getItem(
                new ItemStack(Material.DISPENSER),
                "&6Bank Vault",
                "&7Withdraw your money."
        ));
    }

    private ItemStack getItem(ItemStack item, String name, String ... lore) {
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

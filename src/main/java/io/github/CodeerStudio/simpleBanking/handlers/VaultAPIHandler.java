package io.github.CodeerStudio.simpleBanking.handlers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A handler class for interacting with the Vault API for economy functionality.
 * It is responsible for setting up the economy system using Vault and providing access to the Economy instance.
 */
public class VaultAPIHandler {

    // The economy instance provided by Vault.
    private static Economy economy = null;

    /**
     * Sets up the economy system using Vault. This method checks if Vault is installed and enabled,
     * and if an economy provider is available.
     *
     * @param plugin The instance of the plugin calling this method (usually the main plugin class).
     * @return {@code true} if the economy system is successfully set up, {@code false} otherwise.
     */
    public static boolean setUpEconomy(JavaPlugin plugin) {
        // Check if Vault is installed and enabled on the server
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault is not installed or enabled!");
            return false; // Return false if Vault is not available
        }

        // Get the economy provider from Vault
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("No economy provider found!");
            return false; // Return false if no economy provider is found
        }

        // Set the economy instance
        economy = rsp.getProvider();
        plugin.getLogger().info("Economy provider found: " + economy.getName());
        return true; // Return true if the economy provider is successfully found
    }

    /**
     * Gets the economy instance provided by Vault.
     *
     * @return The Economy instance if Vault and an economy provider are successfully set up, {@code null} otherwise.
     */
    public static Economy getEconomy() {
        return economy;
    }
}

package io.github.CodeerStudio.simpleBanking.handlers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultAPIHandler {
    private static Economy economy = null;

    public static boolean setUpEconomy(JavaPlugin plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault is not installed or enabled!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("No economy provider found!");
            return false;
        }

        economy = rsp.getProvider();
        plugin.getLogger().info("Economy provider found: " + economy.getName());
        return true;
    }

    public static Economy getEconomy() {
        return economy;
    }
}

package io.github.CodeerStudio.simpleBanking;

import io.github.CodeerStudio.simpleBanking.commands.BankMenu;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.CodeerStudio.simpleBanking.handlers.VaultAPIHandler;

public final class SimpleBanking extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!VaultAPIHandler.setUpEconomy(this)) {
            getLogger().severe("Disabling plugin due to missing Vault economy!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("BankPlugin has been enabled!");

        getCommand("bank").setExecutor(new BankMenu(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("BankPlugin has been disabled!");
    }
}

package io.github.CodeerStudio.simpleBanking;

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
    }

    @Override
    public void onDisable() {
        getLogger().info("BankPlugin has been disabled!");
    }
}

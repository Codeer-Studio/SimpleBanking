package io.github.CodeerStudio.simpleBanking;

import io.github.CodeerStudio.simpleBanking.commands.BankMenu;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.CodeerStudio.simpleBanking.handlers.VaultAPIHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class SimpleBanking extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        if (!VaultAPIHandler.setUpEconomy(this)) {
            getLogger().severe("Disabling plugin due to missing Vault economy!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setUpDatabase();

        getLogger().info("BankPlugin has been enabled!");

        getCommand("bank").setExecutor(new BankMenu(this));
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().warning("Could not close the database connection: " + e.getMessage());
        }

        getLogger().info("BankPlugin has been disabled!");
    }

    public Connection getDatabaseConnection() {
        return connection;
    }

    private void setUpDatabase() {
        try {
            // Create the data folder if it doesn't exist
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // Use a file path inside the plugin's data folder
            File databaseFile = new File(getDataFolder(), "banking.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());

            // Create the table if it doesn't exist
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_balances (" +
                            "uuid TEXT PRIMARY KEY, " +
                            "balance DOUBLE DEFAULT 0)")) {
                stmt.executeUpdate();
            }

            getLogger().info("Database initialized successfully at " + databaseFile.getAbsolutePath());
        } catch (SQLException e) {
            getLogger().severe("Could not initialize the database: " + e.getMessage());
        }
    }
}

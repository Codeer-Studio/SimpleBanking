package io.github.CodeerStudio.simpleBanking;

import io.github.CodeerStudio.simpleBanking.commands.AdminGiveBalance;
import io.github.CodeerStudio.simpleBanking.commands.AdminSetBalance;
import io.github.CodeerStudio.simpleBanking.commands.AdminTakeBalance;
import io.github.CodeerStudio.simpleBanking.commands.BankMenu;
import io.github.CodeerStudio.simpleBanking.handlers.BankManagerHandler;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.CodeerStudio.simpleBanking.handlers.VaultAPIHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Main class for the SimpleBanking plugin. This class handles the plugin's lifecycle,
 * including initialization of Vault economy and SQLite database setup.
 */
public final class SimpleBanking extends JavaPlugin {

    // The database connection used to interact with the SQLite database.
    private Connection connection;

    /**
     * Called when the plugin is enabled. This method sets up the economy (via Vault) and the database,
     * and registers the command executor for the "bank" command.
     */
    @Override
    public void onEnable() {
        // Set up Vault economy provider
        if (!VaultAPIHandler.setUpEconomy(this)) {
            getLogger().severe("Disabling plugin due to missing Vault economy!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Set up the SQLite database
        setUpDatabase();

        getLogger().info("BankPlugin has been enabled!");

        BankManagerHandler bankManagerHandler = new BankManagerHandler(this);


        // Register the commands
        getCommand("bank").setExecutor(new BankMenu(this));
        getCommand("setbankbalance").setExecutor(new AdminSetBalance(bankManagerHandler));
        getCommand("givebankbalance").setExecutor(new AdminGiveBalance(bankManagerHandler));
        getCommand("takebankbalance").setExecutor(new AdminTakeBalance(bankManagerHandler));
    }

    /**
     * Called when the plugin is disabled. This method ensures that the database connection is closed.
     */
    @Override
    public void onDisable() {
        try {
            // Close the database connection if it's open
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().warning("Could not close the database connection: " + e.getMessage());
        }

        getLogger().info("BankPlugin has been disabled!");
    }

    /**
     * Gets the active database connection.
     *
     * @return The current database connection, or {@code null} if not initialized.
     */
    public Connection getDatabaseConnection() {
        return connection;
    }

    /**
     * Sets up the SQLite database by creating the necessary file and initializing the player_balances table
     * if it doesn't exist.
     */
    private void setUpDatabase() {
        try {
            // Create the data folder if it doesn't exist
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // Define the file path for the database in the plugin's data folder
            File databaseFile = new File(getDataFolder(), "banking.db");

            // Establish a connection to the SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());

            // Create the player_balances table if it doesn't exist
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

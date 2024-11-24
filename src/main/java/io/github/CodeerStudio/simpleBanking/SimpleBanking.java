package io.github.CodeerStudio.simpleBanking;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleBanking extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Hello there!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

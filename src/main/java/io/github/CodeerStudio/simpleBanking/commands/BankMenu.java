package io.github.CodeerStudio.simpleBanking.commands;

import io.github.CodeerStudio.simpleBanking.SimpleBanking;
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

import java.util.ArrayList;
import java.util.List;

public class BankMenu implements Listener, CommandExecutor {

    private final String inventoryName = ChatColor.translateAlternateColorCodes('&', "&6Bank");
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_SIZE = INVENTORY_ROWS * 9;
    private static final int TEST_SLOT = 11;

    public BankMenu(SimpleBanking plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(inventoryName)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        event.setCancelled(true);

        if (slot == TEST_SLOT) {
            player.sendMessage(ChatColor.GREEN + "You clicked on the Test item!");
        } else {
            player.sendMessage(ChatColor.RED + "This slot does nothing.");
        }
    }

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

    private void populateInventory(Inventory inventory) {
        inventory.setItem(TEST_SLOT, getItem(new ItemStack(Material.ACACIA_FENCE), "&3Test", "&7Click to interact."));
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
}

package io.github.CodeerStudio.simpleBanking.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the creation and population of the bank menu GUI.
 */
public class BankMenuGUI {

    private static final String INVENTORY_NAME = ChatColor.translateAlternateColorCodes('&', "&6Bank");
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_SIZE = INVENTORY_ROWS * 9;

    // Slot assignments
    public static final int DEPOSIT_SLOT = 11;
    public static final int INFORMATION_SLOT = 13;
    public static final int WITHDRAW_SLOT = 15;

    /**
     * Creates and populates the bank menu inventory.
     *
     * @param player The player for whom the inventory is created.
     * @return A populated Inventory instance.
     */
    public static Inventory createBankMenu(Player player) {
        Inventory inventory = player.getServer().createInventory(player, INVENTORY_SIZE, INVENTORY_NAME);

        // Populate the inventory with specific items
        inventory.setItem(DEPOSIT_SLOT, createMenuItem(
                new ItemStack(Material.CHEST),
                "&6Bank Vault",
                "&7Deposit your money."
        ));

        inventory.setItem(INFORMATION_SLOT, createMenuItem(
                new ItemStack(Material.OAK_SIGN),
                "&cBank Information",
                "&7Check the latest bank news."
        ));

        inventory.setItem(WITHDRAW_SLOT, createMenuItem(
                new ItemStack(Material.DISPENSER),
                "&6Bank Vault",
                "&7Withdraw your money."
        ));

        return inventory;
    }

    /**
     * Utility method to create a menu item with specified metadata.
     *
     * @param item The base ItemStack.
     * @param name The display name of the item.
     * @param lore The lore (description) of the item.
     * @return A modified ItemStack with the specified metadata.
     */
    private static ItemStack createMenuItem(ItemStack item, String name, String... lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Set the display name
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // Set the lore if provided
        if (lore.length > 0) {
            List<String> lores = new ArrayList<>();
            for (String line : lore) {
                lores.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lores);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gets the title of the bank menu inventory.
     *
     * @return The inventory title.
     */
    public static String getInventoryName() {
        return INVENTORY_NAME;
    }
}

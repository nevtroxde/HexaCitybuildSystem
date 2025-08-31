package de.nicerecord.citybuildsystem.bank;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.GUIManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankGUI {
    private final CitybuildSystem plugin;
    private final BankManager bankManager;
    private final MessageManager messageManager;
    private final Economy economy;
    private final GUIManager guiManager;

    private double[] nuggetAmounts;
    private double[] ingotAmounts;
    private double[] blockAmounts;

    public BankGUI(CitybuildSystem plugin, BankManager bankManager, MessageManager messageManager, Economy economy) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.messageManager = messageManager;
        this.economy = economy;
        this.guiManager = plugin.getGUIManager();

        loadAmountsFromConfig();
    }

    private void loadAmountsFromConfig() {
        List<Integer> nuggetList = guiManager.getBankNuggetAmounts();
        List<Integer> ingotList = guiManager.getBankIngotAmounts();
        List<Integer> blockList = guiManager.getBankBlockAmounts();

        if (nuggetList.isEmpty()) {
            nuggetAmounts = new double[]{10, 25, 50, 100, 250};
        } else {
            nuggetAmounts = nuggetList.stream().mapToDouble(Integer::doubleValue).toArray();
        }

        if (ingotList.isEmpty()) {
            ingotAmounts = new double[]{500, 1000, 2500, 5000};
        } else {
            ingotAmounts = ingotList.stream().mapToDouble(Integer::doubleValue).toArray();
        }

        if (blockList.isEmpty()) {
            blockAmounts = new double[]{10000, 25000, 50000, 100000};
        } else {
            blockAmounts = blockList.stream().mapToDouble(Integer::doubleValue).toArray();
        }
    }

    public void openMainGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', guiManager.getBankMainGUITitle());
        Inventory inventory = Bukkit.createInventory(null, guiManager.getBankMainGUISize(), title);

        fillBorder(inventory);

        bankManager.getBalance(player.getUniqueId()).thenAccept(bankBalance -> {
            double vaultBalance = economy.getBalance(player);

            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory.setItem(36, createPlayerHead(player, bankBalance, vaultBalance));

                inventory.setItem(20, createDepositItem());

                inventory.setItem(24, createWithdrawItem());

                inventory.setItem(40, createMainInfoItem());

                inventory.setItem(44, createCloseItem());

                player.openInventory(inventory);
            });
        });
    }

    public void openDepositGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', guiManager.getBankDepositGUITitle());
        Inventory inventory = Bukkit.createInventory(null, guiManager.getBankDepositGUISize(), title);

        fillBorderSmall(inventory);

        bankManager.getBalance(player.getUniqueId()).thenAccept(bankBalance -> {
            double vaultBalance = economy.getBalance(player);

            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory.setItem(4, createPlayerHead(player, bankBalance, vaultBalance));

                addDepositItems(inventory, player);

                inventory.setItem(27, createBackItem());

                inventory.setItem(31, createDepositInfoItem());

                inventory.setItem(35, createCloseItem());

                player.openInventory(inventory);
            });
        });
    }

    public void openWithdrawGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', guiManager.getBankWithdrawGUITitle());
        Inventory inventory = Bukkit.createInventory(null, guiManager.getBankWithdrawGUISize(), title);

        fillBorderSmall(inventory);

        bankManager.getBalance(player.getUniqueId()).thenAccept(bankBalance -> {
            double vaultBalance = economy.getBalance(player);

            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory.setItem(4, createPlayerHead(player, bankBalance, vaultBalance));

                addWithdrawItems(inventory, player);

                inventory.setItem(27, createBackItem());

                inventory.setItem(31, createWithdrawInfoItem());

                inventory.setItem(35, createCloseItem());

                player.openInventory(inventory);
            });
        });
    }

    private void fillBorder(Inventory inventory) {
        ItemStack borderItem = createBorderItem();

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(36 + i, borderItem);
        }

        for (int i = 9; i < 36; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }

    private void fillBorderSmall(Inventory inventory) {
        ItemStack borderItem = createBorderItem();

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(27 + i, borderItem);
        }

        for (int i = 9; i < 27; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }

    private ItemStack createBorderItem() {
        Material borderMaterial = Material.valueOf(guiManager.getBorderMaterial());
        ItemStack item = new ItemStack(borderMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBorderName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBorderLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerHead(Player player, double bankBalance, double vaultBalance) {
        Material headMaterial = Material.valueOf(guiManager.getBankPlayerHeadMaterial());
        ItemStack item = new ItemStack(headMaterial);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankPlayerHeadName()));

        List<String> configLore = guiManager.getBankPlayerHeadLore();
        List<String> lore = new ArrayList<>();

        for (String line : configLore) {
            line = line.replace("%bank_balance%", formatMoney(bankBalance))
                      .replace("%vault_balance%", formatMoney(vaultBalance));
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDepositItem() {
        Material depositMaterial = Material.valueOf(guiManager.getBankDepositMaterial());
        ItemStack item = new ItemStack(depositMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankDepositName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBankDepositLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createWithdrawItem() {
        Material withdrawMaterial = Material.valueOf(guiManager.getBankWithdrawMaterial());
        ItemStack item = new ItemStack(withdrawMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankWithdrawName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBankWithdrawLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMainInfoItem() {
        Material infoMaterial = Material.valueOf(guiManager.getBankInfoMaterial());
        ItemStack item = new ItemStack(infoMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankInfoName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBankInfoLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDepositInfoItem() {
        Material infoMaterial = Material.valueOf(guiManager.getBankInfoMaterial());
        ItemStack item = new ItemStack(infoMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankDepositInfoName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBankDepositInfoLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createWithdrawInfoItem() {
        Material infoMaterial = Material.valueOf(guiManager.getBankInfoMaterial());
        ItemStack item = new ItemStack(infoMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankWithdrawInfoName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBankWithdrawInfoLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseItem() {
        Material closeMaterial = Material.valueOf(guiManager.getCloseMaterial());
        ItemStack item = new ItemStack(closeMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getCloseName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getCloseLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackItem() {
        Material backMaterial = Material.valueOf(guiManager.getBankBackMaterial());
        ItemStack item = new ItemStack(backMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBankBackName()));

        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBankBackLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addDepositItems(Inventory inventory, Player player) {
        for (int i = 0; i < nuggetAmounts.length; i++) {
            inventory.setItem(10 + i, createDepositItem(Material.GOLD_NUGGET, nuggetAmounts[i]));
        }

        inventory.setItem(15, createDepositItem(Material.GOLD_INGOT, ingotAmounts[0]));
        inventory.setItem(16, createDepositItem(Material.GOLD_INGOT, ingotAmounts[1]));
        inventory.setItem(19, createDepositItem(Material.GOLD_INGOT, ingotAmounts[2]));
        inventory.setItem(20, createDepositItem(Material.GOLD_INGOT, ingotAmounts[3]));

        for (int i = 0; i < blockAmounts.length; i++) {
            inventory.setItem(21 + i, createDepositItem(Material.GOLD_BLOCK, blockAmounts[i]));
        }
    }

    private void addWithdrawItems(Inventory inventory, Player player) {
        for (int i = 0; i < nuggetAmounts.length; i++) {
            inventory.setItem(10 + i, createWithdrawItem(Material.GOLD_NUGGET, nuggetAmounts[i]));
        }

        inventory.setItem(15, createWithdrawItem(Material.GOLD_INGOT, ingotAmounts[0]));
        inventory.setItem(16, createWithdrawItem(Material.GOLD_INGOT, ingotAmounts[1]));
        inventory.setItem(19, createWithdrawItem(Material.GOLD_INGOT, ingotAmounts[2]));
        inventory.setItem(20, createWithdrawItem(Material.GOLD_INGOT, ingotAmounts[3]));

        for (int i = 0; i < blockAmounts.length; i++) {
            inventory.setItem(21 + i, createWithdrawItem(Material.GOLD_BLOCK, blockAmounts[i]));
        }
    }

    private ItemStack createDepositItem(Material material, double amount) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&l" + formatMoney(amount)));
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7Klicke um " + formatMoney(amount) + " einzuzahlen")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createWithdrawItem(Material material, double amount) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&l" + formatMoney(amount)));
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7Klicke um " + formatMoney(amount) + " auszuzahlen")));
        item.setItemMeta(meta);
        return item;
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000);
        } else {
            return String.format("%.0f", amount);
        }
    }
}

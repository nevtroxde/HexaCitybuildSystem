package de.nicerecord.citybuildsystem.bank;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.GUIManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BankGUIListener implements Listener {
    private final CitybuildSystem plugin;
    private final BankManager bankManager;
    private final MessageManager messageManager;
    private final Economy economy;
    private final GUIManager guiManager;
    private final BankGUI bankGUI;

    public BankGUIListener(CitybuildSystem plugin, BankManager bankManager, MessageManager messageManager, Economy economy) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.messageManager = messageManager;
        this.economy = economy;
        this.guiManager = plugin.getGUIManager();
        this.bankGUI = new BankGUI(plugin, bankManager, messageManager, economy);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String mainTitle = ChatColor.translateAlternateColorCodes('&', guiManager.getBankMainGUITitle());
        String depositTitle = ChatColor.translateAlternateColorCodes('&', guiManager.getBankDepositGUITitle());
        String withdrawTitle = ChatColor.translateAlternateColorCodes('&', guiManager.getBankWithdrawGUITitle());

        if (!title.equals(mainTitle) && !title.equals(depositTitle) && !title.equals(withdrawTitle)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;

        String displayName = meta.getDisplayName();

        if (title.equals(mainTitle)) {
            handleMainGUI(player, displayName, clickedItem);
        } else if (title.equals(depositTitle)) {
            handleDepositGUI(player, displayName, clickedItem);
        } else if (title.equals(withdrawTitle)) {
            handleWithdrawGUI(player, displayName, clickedItem);
        }
    }

    private void handleMainGUI(Player player, String displayName, ItemStack item) {
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankDepositName()))) {
            bankGUI.openDepositGUI(player);
        } else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankWithdrawName()))) {
            bankGUI.openWithdrawGUI(player);
        } else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getCloseName()))) {
            player.closeInventory();
        }
    }

    private void handleDepositGUI(Player player, String displayName, ItemStack item) {
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankBackName()))) {
            bankGUI.openMainGUI(player);
            return;
        } else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getCloseName()))) {
            player.closeInventory();
            return;
        } else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankDepositInfoName())) ||
                   displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankPlayerHeadName()))) {
            return;
        }

        Material material = item.getType();
        if (material == Material.GOLD_NUGGET || material == Material.GOLD_INGOT || material == Material.GOLD_BLOCK) {
            double amount = parseAmountFromDisplayName(displayName);
            if (amount > 0) {
                performDeposit(player, amount);
            }
        }
    }

    private void handleWithdrawGUI(Player player, String displayName, ItemStack item) {
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankBackName()))) {
            bankGUI.openMainGUI(player);
            return;
        } else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getCloseName()))) {
            player.closeInventory();
            return;
        } else if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankWithdrawInfoName())) ||
                   displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getBankPlayerHeadName()))) {
            return;
        }

        Material material = item.getType();
        if (material == Material.GOLD_NUGGET || material == Material.GOLD_INGOT || material == Material.GOLD_BLOCK) {
            double amount = parseAmountFromDisplayName(displayName);
            if (amount > 0) {
                performWithdraw(player, amount);
            }
        }
    }

    private double parseAmountFromDisplayName(String displayName) {
        String stripped = ChatColor.stripColor(displayName);
        
        try {
            if (stripped.endsWith("K")) {
                return Double.parseDouble(stripped.replace("K", "")) * 1000;
            } else if (stripped.endsWith("M")) {
                return Double.parseDouble(stripped.replace("M", "")) * 1000000;
            } else {
                return Double.parseDouble(stripped);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void performDeposit(Player player, double amount) {
        if (amount > 100000) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getMessage("bank.deposit-limit")));
            return;
        }

        if (economy.getBalance(player) < amount) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getMessage("bank.insufficient-funds")));
            return;
        }

        bankManager.deposit(player, amount).thenAccept(success -> {
            if (success) {
                economy.withdrawPlayer(player, amount);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("bank.deposit-success")
                        .replace("%amount%", formatMoney(amount))));
                
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    bankGUI.openDepositGUI(player);
                }, 1L);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    messageManager.getMessage("bank.deposit-failed")));
            }
        });
    }

    private void performWithdraw(Player player, double amount) {
        if (amount > 1000000) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getMessage("bank.withdraw-limit")));
            return;
        }

        bankManager.getBalance(player.getUniqueId()).thenAccept(bankBalance -> {
            if (bankBalance < amount) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    messageManager.getMessage("bank.insufficient-bank-funds")));
                return;
            }

            bankManager.withdraw(player, amount).thenAccept(success -> {
                if (success) {
                    economy.depositPlayer(player, amount);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("bank.withdraw-success")
                            .replace("%amount%", formatMoney(amount))));
                    
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        bankGUI.openWithdrawGUI(player);
                    }, 1L);
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        messageManager.getMessage("bank.withdraw-failed")));
                }
            });
        });
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

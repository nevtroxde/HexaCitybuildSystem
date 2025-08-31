package de.nicerecord.citybuildsystem.bank;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final BankManager bankManager;
    private final MessageManager messageManager;
    private final Economy economy;
    private final BankGUI bankGUI;

    public BankCommand(CitybuildSystem plugin, BankManager bankManager, MessageManager messageManager, Economy economy) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.messageManager = messageManager;
        this.economy = economy;
        this.bankGUI = new BankGUI(plugin, bankManager, messageManager, economy);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.bank")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (economy == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.economy-not-available")));
            return true;
        }

        if (args.length == 0) {
            bankGUI.openMainGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance":
            case "bal":
                handleBalance(player);
                break;
            case "deposit":
            case "dep":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.bank.deposit-usage")));
                    return true;
                }
                handleDeposit(player, args[1]);
                break;
            case "withdraw":
            case "wd":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.bank.withdraw-usage")));
                    return true;
                }
                handleWithdraw(player, args[1]);
                break;
            default:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.bank.usage")));
                break;
        }

        return true;
    }

    private void handleBalance(Player player) {
        bankManager.getBalance(player.getUniqueId()).thenAccept(bankBalance -> {
            double vaultBalance = economy.getBalance(player);
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("bank.balance")
                    .replace("%bank_balance%", formatMoney(bankBalance))
                    .replace("%vault_balance%", formatMoney(vaultBalance))));
        });
    }

    private void handleDeposit(Player player, String amountStr) {
        double amount;
        try {
            if (amountStr.equalsIgnoreCase("all") || amountStr.equalsIgnoreCase("alles")) {
                amount = economy.getBalance(player);
            } else {
                amount = Double.parseDouble(amountStr);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.invalid-amount")));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.invalid-amount")));
            return;
        }

        if (amount > 100000) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.deposit-limit")));
            return;
        }

        if (economy.getBalance(player) < amount) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.insufficient-funds")));
            return;
        }

        bankManager.deposit(player, amount).thenAccept(success -> {
            if (success) {
                economy.withdrawPlayer(player, amount);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("bank.deposit-success").replace("%amount%", formatMoney(amount))));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.deposit-failed")));
            }
        });
    }

    private void handleWithdraw(Player player, String amountStr) {
        double amount;
        try {
            if (amountStr.equalsIgnoreCase("all") || amountStr.equalsIgnoreCase("alles")) {
                amount = bankManager.getBalance(player.getUniqueId()).join();
            } else {
                amount = Double.parseDouble(amountStr);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.invalid-amount")));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.invalid-amount")));
            return;
        }

        if (amount > 1000000) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.withdraw-limit")));
            return;
        }

        bankManager.getBalance(player.getUniqueId()).thenAccept(bankBalance -> {
            if (bankBalance < amount) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.insufficient-bank-funds")));
                return;
            }

            bankManager.withdraw(player, amount).thenAccept(success -> {
                if (success) {
                    economy.depositPlayer(player, amount);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("bank.withdraw-success").replace("%amount%", formatMoney(amount))));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.withdraw-failed")));
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
            return String.format("%.2f", amount);
        }
    }
}

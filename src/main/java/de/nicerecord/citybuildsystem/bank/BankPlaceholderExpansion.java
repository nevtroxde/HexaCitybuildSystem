package de.nicerecord.citybuildsystem.bank;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BankPlaceholderExpansion extends PlaceholderExpansion {
    private final CitybuildSystem plugin;
    private final BankManager bankManager;

    public BankPlaceholderExpansion(CitybuildSystem plugin, BankManager bankManager) {
        this.plugin = plugin;
        this.bankManager = bankManager;
    }

    @Override
    public String getIdentifier() {
        return "cbsystem";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equals("bank_balance")) {
            if (player == null) return "0";
            
            try {
                double balance = bankManager.getBalance(player.getUniqueId()).join();
                return formatMoney(balance);
            } catch (Exception e) {
                return "0";
            }
        }

        if (params.startsWith("bank_balance_")) {
            String targetPlayerName = params.substring("bank_balance_".length());
            
            try {
                double balance = bankManager.getBalance(targetPlayerName).join();
                return formatMoney(balance);
            } catch (Exception e) {
                return "0";
            }
        }

        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
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

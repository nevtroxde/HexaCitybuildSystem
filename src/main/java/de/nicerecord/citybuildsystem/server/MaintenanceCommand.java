package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class MaintenanceCommand implements CommandExecutor, Listener {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final MaintenanceManager maintenanceManager;

    public MaintenanceCommand(CitybuildSystem plugin, MessageManager messageManager, MaintenanceManager maintenanceManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.maintenanceManager = maintenanceManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.maintenance")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 0) {
            boolean currentStatus = maintenanceManager.isMaintenanceEnabled();
            boolean newStatus = !currentStatus;

            maintenanceManager.setMaintenanceEnabled(newStatus);

            String message = newStatus ? "server.maintenance-enabled" : "server.maintenance-disabled";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage(message)));

            if (newStatus) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("cbsystem.maintenance") &&
                            !maintenanceManager.isWhitelisted(player.getName()) &&
                            !maintenanceManager.isWhitelisted(player.getUniqueId())) {
                        player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                messageManager.getMessageWithoutPrefix("server.maintenance-kick")));
                    }
                }
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVerwendung: /maintenance add <spieler>"));
                    return true;
                }

                String addPlayerName = args[1];
                OfflinePlayer addTarget = Bukkit.getOfflinePlayer(addPlayerName);

                if (maintenanceManager.addToWhitelist(addTarget.getName(), addTarget.getUniqueId(), sender.getName())) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("server.maintenance-add-success").replace("%player%", addTarget.getName())));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("server.maintenance-add-failed").replace("%player%", addPlayerName)));
                }
                break;

            case "remove":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVerwendung: /maintenance remove <spieler>"));
                    return true;
                }

                String removePlayerName = args[1];

                if (maintenanceManager.removeFromWhitelist(removePlayerName)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("server.maintenance-remove-success").replace("%player%", removePlayerName)));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("server.maintenance-remove-failed").replace("%player%", removePlayerName)));
                }
                break;

            case "list":
                List<String> whitelistedPlayers = maintenanceManager.getWhitelistedPlayers();
                if (whitelistedPlayers.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("server.maintenance-list-empty")));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("server.maintenance-list-header")));
                    for (String playerName : whitelistedPlayers) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e" + playerName));
                    }
                }
                break;

            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVerwendung: /maintenance [add|remove|list] [spieler]"));
                break;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!maintenanceManager.isMaintenanceEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("cbsystem.maintenance")) {
            return;
        }

        if (maintenanceManager.isWhitelisted(player.getName()) ||
                maintenanceManager.isWhitelisted(player.getUniqueId())) {
            return;
        }

        event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
        event.setKickMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessageWithoutPrefix("server.maintenance-kick")));
    }

}
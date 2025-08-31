package de.nicerecord.citybuildsystem.info;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final PlaytimeManager playtimeManager;
    private final MessageManager messageManager;

    public PlaytimeCommand(CitybuildSystem plugin, PlaytimeManager playtimeManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("cbsystem.playtime")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            playtimeManager.getPlaytime(player.getUniqueId()).thenAccept(playtimeData -> {
                if (playtimeData != null) {
                    String formattedTime = playtimeManager.formatPlaytime(playtimeData.getTotalTime());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("info.playtime-self").replace("%time%", formattedTime)));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("info.playtime-self").replace("%time%", "0 Sekunden")));
                }
            });
        } else {
            if (!sender.hasPermission("cbsystem.playtime.other")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            String targetName = args[0];

            Player targetPlayer = plugin.getServer().getPlayer(targetName);
            if (targetPlayer != null) {
                playtimeManager.getPlaytime(targetPlayer.getUniqueId()).thenAccept(playtimeData -> {
                    if (playtimeData != null) {
                        String formattedTime = playtimeManager.formatPlaytime(playtimeData.getTotalTime());
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("info.playtime-other")
                                .replace("%player%", targetPlayer.getName())
                                .replace("%time%", formattedTime)));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("info.playtime-other")
                                .replace("%player%", targetPlayer.getName())
                                .replace("%time%", "0 Sekunden")));
                    }
                });
            } else {
                playtimeManager.getPlaytime(targetName).thenAccept(playtimeData -> {
                    if (playtimeData != null) {
                        String formattedTime = playtimeManager.formatPlaytime(playtimeData.getTotalTime());
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("info.playtime-other")
                                .replace("%player%", playtimeData.getPlayerName())
                                .replace("%time%", formattedTime)));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("general.player-not-found").replace("%player%", targetName)));
                    }
                });
            }
        }

        return true;
    }
}

package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
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
            if (!player.hasPermission("cbsystem.vanish")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            toggleVanish(player);
            String message = isVanished(player) ? "admin.vanish-enabled-self" : "admin.vanish-disabled-self";
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage(message)));
        } else {
            if (!sender.hasPermission("cbsystem.vanish.other")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[0])));
                return true;
            }

            toggleVanish(target);
            String senderMessage = isVanished(target) ? "admin.vanish-enabled-other" : "admin.vanish-disabled-other";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage(senderMessage).replace("%player%", target.getName())));
        }

        return true;
    }

    private void toggleVanish(Player player) {
        if (isVanished(player)) {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, player);
            }
        } else {
            vanishedPlayers.add(player.getUniqueId());
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("cbsystem.vanish")) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }
        }
    }

    public static boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public static void handlePlayerJoin(Player player, CitybuildSystem plugin) {
        if (!player.hasPermission("cbsystem.vanish")) {
            for (UUID vanishedUUID : vanishedPlayers) {
                Player vanishedPlayer = plugin.getServer().getPlayer(vanishedUUID);
                if (vanishedPlayer != null) {
                    player.hidePlayer(plugin, vanishedPlayer);
                }
            }
        }
    }
}

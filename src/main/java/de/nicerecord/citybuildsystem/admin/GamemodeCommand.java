package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public GamemodeCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.gamemode.usage")));
            return true;
        }

        GameMode gameMode = parseGameMode(args[0]);
        if (gameMode == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.gamemode.usage")));
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("cbsystem.gamemode")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            player.setGameMode(gameMode);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.gamemode-changed-self").replace("%gamemode%", gameMode.name())));
        } else {
            if (!sender.hasPermission("cbsystem.gamemode.other")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[1])));
                return true;
            }

            target.setGameMode(gameMode);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.gamemode-changed-other")
                    .replace("%player%", target.getName())
                    .replace("%gamemode%", gameMode.name())));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.gamemode-changed-by").replace("%gamemode%", gameMode.name())));
        }

        return true;
    }

    private GameMode parseGameMode(String input) {
        switch (input.toLowerCase()) {
            case "0":
            case "s":
            case "survival":
                return GameMode.SURVIVAL;
            case "1":
            case "c":
            case "creative":
                return GameMode.CREATIVE;
            case "2":
            case "a":
            case "adventure":
                return GameMode.ADVENTURE;
            case "3":
            case "sp":
            case "spectator":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }
}

package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final WarpManager warpManager;
    private final MessageManager messageManager;

    public SetWarpCommand(CitybuildSystem plugin, WarpManager warpManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.setwarp")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.setwarp.usage")));
            return true;
        }

        String warpName = args[0];

        if (!warpManager.isValidWarpName(warpName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.invalid-name")));
            return true;
        }

        warpManager.createWarp(warpName, player.getLocation()).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("teleport.warp-created").replace("%warp%", warpName)));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("teleport.warp-already-exists").replace("%warp%", warpName)));
            }
        });

        return true;
    }
}

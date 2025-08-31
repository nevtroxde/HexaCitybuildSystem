package de.nicerecord.citybuildsystem.utility;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderchestCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public EnderchestCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (!player.hasPermission("cbsystem.ec")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            player.openInventory(player.getEnderChest());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("utility.ec-opened-self")));
        } else {
            if (!player.hasPermission("cbsystem.ec.other")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[0])));
                return true;
            }

            player.openInventory(target.getEnderChest());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("utility.ec-opened-other").replace("%player%", target.getName())));
        }

        return true;
    }
}

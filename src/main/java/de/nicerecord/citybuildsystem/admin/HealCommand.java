package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public HealCommand(CitybuildSystem plugin, MessageManager messageManager) {
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
            if (!player.hasPermission("cbsystem.heal")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            player.setHealth(20.0);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("admin.healed-self")));
        } else {
            if (!sender.hasPermission("cbsystem.heal.other")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[0])));
                return true;
            }

            target.setHealth(20.0);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.healed-other").replace("%player%", target.getName())));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.healed-by").replace("%player%", sender.getName())));
        }

        return true;
    }
}

package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaHereCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final TpaManager tpaManager;
    private final MessageManager messageManager;

    public TpaHereCommand(CitybuildSystem plugin, TpaManager tpaManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.tpahere")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.tpahere.usage")));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("general.player-not-found").replace("%player%", args[0])));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("social.message-self")));
            return true;
        }

        if (!tpaManager.isTpaEnabled(target.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.tpa-player-disabled")));
            return true;
        }

        boolean success = tpaManager.sendTpaRequest(player, target, TpaRequest.TpaType.TPAHERE);
        if (success) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("teleport.tpahere-sent").replace("%player%", target.getName())));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("teleport.tpahere-received").replace("%player%", player.getName())));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.tpa-player-disabled")));
        }

        return true;
    }
}

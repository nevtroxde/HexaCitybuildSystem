package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public FlyCommand(CitybuildSystem plugin, MessageManager messageManager) {
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
            if (!player.hasPermission("cbsystem.fly")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            boolean newFlyState = !player.getAllowFlight();
            player.setAllowFlight(newFlyState);
            player.setFlying(newFlyState);

            String message = newFlyState ? "admin.fly-enabled-self" : "admin.fly-disabled-self";
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage(message)));
        } else {
            if (!sender.hasPermission("cbsystem.fly.other")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[0])));
                return true;
            }

            boolean newFlyState = !target.getAllowFlight();
            target.setAllowFlight(newFlyState);
            target.setFlying(newFlyState);

            String senderMessage = newFlyState ? "admin.fly-enabled-other" : "admin.fly-disabled-other";
            String targetMessage = newFlyState ? "admin.fly-enabled-by" : "admin.fly-disabled-by";

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage(senderMessage).replace("%player%", target.getName())));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage(targetMessage)));
        }

        return true;
    }
}

package de.nicerecord.citybuildsystem.messaging;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageSystem messageSystem;
    private final MessageManager messageManager;

    public MessageCommand(CitybuildSystem plugin, MessageSystem messageSystem, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageSystem = messageSystem;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.message")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.message.usage")));
            return true;
        }

        String targetName = args[0];
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("general.player-not-found").replace("%player%", targetName)));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("social.message-self")));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(args[i]);
        }

        String message = messageBuilder.toString();
        messageSystem.sendPrivateMessage(player, target, message);

        return true;
    }
}

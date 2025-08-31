package de.nicerecord.citybuildsystem.messaging;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespondCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageSystem messageSystem;
    private final MessageManager messageManager;

    public RespondCommand(CitybuildSystem plugin, MessageSystem messageSystem, MessageManager messageManager) {
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

        if (!player.hasPermission("cbsystem.respond")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.respond.usage")));
            return true;
        }

        Player target = messageSystem.getLastConversationPartner(player);
        if (target == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("social.no-reply-target")));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(args[i]);
        }

        String message = messageBuilder.toString();
        messageSystem.sendPrivateMessage(player, target, message);

        return true;
    }
}

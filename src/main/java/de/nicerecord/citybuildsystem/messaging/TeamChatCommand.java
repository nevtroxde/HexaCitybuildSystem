package de.nicerecord.citybuildsystem.messaging;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public TeamChatCommand(CitybuildSystem plugin, MessageManager messageManager) {
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

        if (!player.hasPermission("cbsystem.teamchat")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.teamchat.usage")));
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
        sendTeamChatMessage(player, message);

        return true;
    }

    private void sendTeamChatMessage(Player sender, String message) {
        String teamChatFormat = ChatColor.translateAlternateColorCodes('&',
            messageManager.getMessageWithoutPrefix("social.teamchat-format")
                .replace("%player%", sender.getName())
                .replace("%message%", message));

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("cbsystem.teamchat")) {
                onlinePlayer.sendMessage(teamChatFormat);
            }
        }
    }
}

package de.nicerecord.citybuildsystem.sudo;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class SudoCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public SudoCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.sudo")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.sudo.usage")));
            return true;
        }

        String targetPlayerName = args[0];
        Player target = plugin.getServer().getPlayer(targetPlayerName);

        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("general.player-not-found").replace("%player%", targetPlayerName)));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(args[i]);
        }

        String content = messageBuilder.toString();

        try {
            if (content.startsWith("/")) {
                String commandToExecute = content.substring(1);
                boolean success = Bukkit.dispatchCommand(target, commandToExecute);

                if (success) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("sudo.executed")
                            .replace("%command%", "/" + commandToExecute)
                            .replace("%player%", target.getName())));

                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("sudo.failed")));
                }
            } else {
                sendChatMessageAsPlayer(target, content);

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("sudo.message-sent")
                        .replace("%message%", content)
                        .replace("%player%", target.getName())));

            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("sudo.failed")));
        }

        return true;
    }

    private void sendChatMessageAsPlayer(Player player, String message) {
        AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(false, player, message,
                (Set<Player>) Bukkit.getServer().getOnlinePlayers());

        Bukkit.getPluginManager().callEvent(chatEvent);

        if (!chatEvent.isCancelled()) {
            String formattedMessage = String.format(chatEvent.getFormat(),
                chatEvent.getPlayer().getDisplayName(), chatEvent.getMessage());

            for (Player recipient : chatEvent.getRecipients()) {
                recipient.sendMessage(formattedMessage);
            }

            Bukkit.getConsoleSender().sendMessage(formattedMessage);
        }
    }
}

package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public BroadcastCommand(CitybuildSystem plugin, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.broadcast")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.broadcast.usage")));
            return true;
        }

        String message = String.join(" ", args);
        String broadcastPrefix = configManager.getBroadcastPrefix();
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', broadcastPrefix + message);

        Bukkit.broadcastMessage(formattedMessage);

        plugin.getLogger().info("Broadcast: " + ChatColor.stripColor(formattedMessage));

        return true;
    }
}

package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearchatCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public ClearchatCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.clearchat")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        for (int i = 0; i < 100; i++) {
            Bukkit.broadcastMessage("");
        }

        String clearMessage = ChatColor.translateAlternateColorCodes('&',
            messageManager.getMessage("server.chat-cleared").replace("%player%", sender instanceof Player ? ((Player) sender).getName() : "Console"));
        Bukkit.broadcastMessage(clearMessage);

        return true;
    }
}

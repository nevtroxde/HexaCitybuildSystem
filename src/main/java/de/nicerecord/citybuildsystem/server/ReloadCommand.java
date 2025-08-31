package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public ReloadCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        plugin.reloadConfig();

        messageManager.reloadMessages();

        plugin.getGUIManager().reloadGUIConfig();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("server.reload-success")));
        plugin.getLogger().info("Plugin wurde von " + sender.getName() + " neu geladen!");

        return true;
    }
}

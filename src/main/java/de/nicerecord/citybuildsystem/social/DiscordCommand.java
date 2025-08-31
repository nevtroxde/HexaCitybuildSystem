package de.nicerecord.citybuildsystem.social;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public DiscordCommand(CitybuildSystem plugin, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.discord")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (!configManager.isDiscordEnabled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
            return true;
        }

        String discordUrl = configManager.getDiscordUrl();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            messageManager.getMessage("social.discord").replace("%url%", discordUrl)));

        return true;
    }
}

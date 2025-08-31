package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import de.nicerecord.citybuildsystem.utils.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdateCommand implements CommandExecutor {

    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final UpdateChecker updateChecker;

    public UpdateCommand(CitybuildSystem plugin, MessageManager messageManager, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.updateChecker = updateChecker;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("citybuildsystem.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("no_permission")));
            return true;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("update.checking")));

        updateChecker.checkForUpdates().thenAccept(result -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (result.hasError()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("update.error")
                            .replace("%error%", result.getError())));
                } else if (result.hasUpdate()) {
                    String message = messageManager.getMessage("update.available")
                            .replace("%current_version%", plugin.getDescription().getVersion())
                            .replace("%latest_version%", result.getLatestVersion())
                            .replace("%download_url%", result.getDownloadUrl());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("update.uptodate")
                            .replace("%version%", plugin.getDescription().getVersion())));
                }
            });
        }).exceptionally(throwable -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("update.error")
                        .replace("%error%", throwable.getMessage())));
            });
            return null;
        });

        return true;
    }
}

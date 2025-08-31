package de.nicerecord.citybuildsystem.utils;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotificationListener implements Listener {

    private final CitybuildSystem plugin;
    private final UpdateChecker updateChecker;

    public UpdateNotificationListener(CitybuildSystem plugin, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("citybuildsystem.admin") && !player.isOp()) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            updateChecker.checkForUpdates().thenAccept(result -> {
                if (result.hasUpdate()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        String message = plugin.getMessageManager().getMessage("update.available")
                                .replace("%current_version%", plugin.getDescription().getVersion())
                                .replace("%latest_version%", result.getLatestVersion())
                                .replace("%download_url%", result.getDownloadUrl());

                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    });
                }
            }).exceptionally(throwable -> {
                plugin.getLogger().warning("Fehler beim Update-Check für " + player.getName() + ": " + throwable.getMessage());
                return null;
            });
        }, 60L);
    }
}

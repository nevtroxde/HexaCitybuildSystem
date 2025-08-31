package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlowChatCommand implements CommandExecutor, Listener {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final Map<UUID, Long> lastChatTime = new HashMap<>();

    public SlowChatCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.slowchat")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        boolean currentStatus = plugin.getConfigManager().isSlowchatEnabled();
        boolean newStatus = !currentStatus;

        plugin.getConfig().set("server.slowchat.enabled", newStatus);
        plugin.saveConfig();

        if (newStatus) {
            int delay = plugin.getConfigManager().getSlowchatDelay();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("server.slowchat-enabled").replace("%delay%", String.valueOf(delay))));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("server.slowchat-disabled")));
            lastChatTime.clear();
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigManager().isSlowchatEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("cbsystem.slowchat")) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        int slowchatDelay = plugin.getConfigManager().getSlowchatDelay() * 1000;

        if (lastChatTime.containsKey(playerUUID)) {
            long lastTime = lastChatTime.get(playerUUID);
            long timeDiff = currentTime - lastTime;

            if (timeDiff < slowchatDelay) {
                event.setCancelled(true);
                long remainingTime = (slowchatDelay - timeDiff) / 1000;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("server.slowchat-wait").replace("%time%", String.valueOf(remainingTime))));
                return;
            }
        }

        lastChatTime.put(playerUUID, currentTime);
    }
}

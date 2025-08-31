package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final WarpManager warpManager;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public WarpCommand(CitybuildSystem plugin, WarpManager warpManager, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.warp")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.warp.usage")));
            return true;
        }

        String warpName = args[0];

        warpManager.getWarp(warpName).thenAccept(warp -> {
            if (warp == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("teleport.warp-not-found").replace("%warp%", warpName)));
                return;
            }

            Location warpLocation = warp.getLocation();
            if (warpLocation == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
                return;
            }

            if (teleportTasks.containsKey(player.getUniqueId())) {
                teleportTasks.get(player.getUniqueId()).cancel();
                teleportTasks.remove(player.getUniqueId());
            }

            int delay = configManager.getTeleportDelay();
            if (delay <= 0) {
                player.teleport(warpLocation);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("teleport.warp-teleported").replace("%warp%", warpName)));
                return;
            }

            lastLocations.put(player.getUniqueId(), player.getLocation());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("teleport.warp-teleporting").replace("%warp%", warpName)));

            BukkitRunnable teleportTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (configManager.isCancelOnMove()) {
                        Location currentLoc = player.getLocation();
                        Location lastLoc = lastLocations.get(player.getUniqueId());
                        if (lastLoc != null && (Math.abs(currentLoc.getX() - lastLoc.getX()) > 0.5 ||
                            Math.abs(currentLoc.getZ() - lastLoc.getZ()) > 0.5)) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                messageManager.getMessage("home.teleport-cancelled")));
                            teleportTasks.remove(player.getUniqueId());
                            lastLocations.remove(player.getUniqueId());
                            return;
                        }
                    }

                    player.teleport(warpLocation);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("teleport.warp-teleported").replace("%warp%", warpName)));
                    teleportTasks.remove(player.getUniqueId());
                    lastLocations.remove(player.getUniqueId());
                }
            };

            teleportTask.runTaskLater(plugin, delay * 20L);
            teleportTasks.put(player.getUniqueId(), teleportTask);
        });

        return true;
    }
}

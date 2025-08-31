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

public class TpAcceptCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final TpaManager tpaManager;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public TpAcceptCommand(CitybuildSystem plugin, TpaManager tpaManager, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
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

        if (!player.hasPermission("cbsystem.tpaccept")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        TpaRequest request = tpaManager.getRequestForTarget(player.getUniqueId());
        if (request == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.tpa-no-request")));
            return true;
        }

        if (request.isExpired(configManager.getTpaTimeout())) {
            tpaManager.removeRequest(request.getRequester());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.tpa-timeout")));
            return true;
        }

        Player requester = plugin.getServer().getPlayer(request.getRequester());
        if (requester == null) {
            tpaManager.removeRequest(request.getRequester());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.player-offline")));
            return true;
        }

        tpaManager.removeRequest(request.getRequester());

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.tpa-accepted")));
        requester.sendMessage(ChatColor.translateAlternateColorCodes('&',
            messageManager.getMessage("teleport.tpa-accepted-by").replace("%player%", player.getName())));

        performTeleport(request, requester, player);

        return true;
    }

    private void performTeleport(TpaRequest request, Player requester, Player target) {
        Player playerToTeleport;
        Location destination;

        if (request.getType() == TpaRequest.TpaType.TPA) {
            playerToTeleport = requester;
            destination = target.getLocation();
        } else {
            playerToTeleport = target;
            destination = requester.getLocation();
        }

        if (teleportTasks.containsKey(playerToTeleport.getUniqueId())) {
            teleportTasks.get(playerToTeleport.getUniqueId()).cancel();
            teleportTasks.remove(playerToTeleport.getUniqueId());
        }

        int delay = configManager.getTeleportDelay();
        if (delay <= 0) {
            playerToTeleport.teleport(destination);
            return;
        }

        lastLocations.put(playerToTeleport.getUniqueId(), playerToTeleport.getLocation());
        String targetName = request.getType() == TpaRequest.TpaType.TPA ? target.getName() : requester.getName();
        playerToTeleport.sendMessage(ChatColor.translateAlternateColorCodes('&',
            messageManager.getMessage("home.teleporting").replace("%home%", targetName)));

        BukkitRunnable teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (configManager.isCancelOnMove()) {
                    Location currentLoc = playerToTeleport.getLocation();
                    Location lastLoc = lastLocations.get(playerToTeleport.getUniqueId());
                    if (lastLoc != null && (Math.abs(currentLoc.getX() - lastLoc.getX()) > 0.5 ||
                        Math.abs(currentLoc.getZ() - lastLoc.getZ()) > 0.5)) {
                        playerToTeleport.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("home.teleport-cancelled")));
                        teleportTasks.remove(playerToTeleport.getUniqueId());
                        lastLocations.remove(playerToTeleport.getUniqueId());
                        return;
                    }
                }

                playerToTeleport.teleport(destination);
                teleportTasks.remove(playerToTeleport.getUniqueId());
                lastLocations.remove(playerToTeleport.getUniqueId());
            }
        };

        teleportTask.runTaskLater(plugin, delay * 20L);
        teleportTasks.put(playerToTeleport.getUniqueId(), teleportTask);
    }
}

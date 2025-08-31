package de.nicerecord.citybuildsystem.home;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetHomeCommand implements CommandExecutor, Listener {
    private final CitybuildSystem plugin;
    private final HomeManager homeManager;
    private final MessageManager messageManager;
    private final Map<UUID, Boolean> waitingForName = new HashMap<>();

    public SetHomeCommand(CitybuildSystem plugin, HomeManager homeManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (!hasPermissionToSetHome(player)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.enter-name")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.enter-name-cancel")));
            waitingForName.put(player.getUniqueId(), true);
            return true;
        }

        String homeName = args[0];
        processSetHome(player, homeName);
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!waitingForName.containsKey(playerUUID)) {
            return;
        }

        event.setCancelled(true);
        waitingForName.remove(playerUUID);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.loading")));
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> processSetHome(player, message));
    }

    private void processSetHome(Player player, String homeName) {
        if (!homeManager.isValidHomeName(homeName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.invalid-name")));
            return;
        }

        if (!hasPermissionToSetHome(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return;
        }

        homeManager.getHome(player, homeName).thenAccept(existingHome -> {
            if (existingHome != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("home.already-exists").replace("%home%", homeName)));
                return;
            }

            homeManager.getPlayerHomes(player).thenAccept(homes -> {
                int maxHomes = homeManager.getMaxHomesForPlayer(player);
                if (homes.size() >= maxHomes) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.max-homes-reached")));
                    return;
                }

                homeManager.createHome(player, homeName, player.getLocation()).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("home.set-success").replace("%home%", homeName)));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
                    }
                });
            });
        });
    }

    private boolean hasPermissionToSetHome(Player player) {
        if (player.hasPermission("cbsystem.home.set.*")) {
            return true;
        }

        for (int i = 1; i <= 28; i++) {
            if (player.hasPermission("cbsystem.home.set." + i)) {
                return true;
            }
        }

        return false;
    }
}

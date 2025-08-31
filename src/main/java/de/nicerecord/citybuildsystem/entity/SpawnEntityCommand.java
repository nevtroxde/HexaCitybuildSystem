package de.nicerecord.citybuildsystem.entity;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnEntityCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public SpawnEntityCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.spawnentity")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.spawnentity.usage")));
            return true;
        }

        String entityName = args[0].toUpperCase();
        EntityType entityType;

        try {
            entityType = EntityType.valueOf(entityName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("entity.invalid-entity").replace("%entity%", args[0])));
            return true;
        }

        // Check if entity is spawnable
        if (!entityType.isSpawnable()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("entity.invalid-entity").replace("%entity%", args[0])));
            return true;
        }

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0 || amount > 50) {
                    amount = 1;
                }
            } catch (NumberFormatException e) {
                amount = 1;
            }
        }

        Location spawnLocation = player.getLocation();
        int spawned = 0;

        try {
            for (int i = 0; i < amount; i++) {
                Location loc = spawnLocation.clone().add(
                    (Math.random() - 0.5) * 2,
                    0,
                    (Math.random() - 0.5) * 2
                );
                player.getWorld().spawnEntity(loc, entityType);
                spawned++;
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("entity.spawned")
                    .replace("%amount%", String.valueOf(spawned))
                    .replace("%entity%", entityType.name())));

        } catch (Exception e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("entity.spawn-failed").replace("%entity%", entityType.name())));
        }

        return true;
    }
}

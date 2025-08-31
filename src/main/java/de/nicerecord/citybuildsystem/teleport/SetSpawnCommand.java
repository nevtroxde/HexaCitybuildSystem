package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final SpawnManager spawnManager;
    private final MessageManager messageManager;

    public SetSpawnCommand(CitybuildSystem plugin, SpawnManager spawnManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.setspawn")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        spawnManager.setSpawn(player.getLocation()).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.spawn-set")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
            }
        });

        return true;
    }
}

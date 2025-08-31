package de.nicerecord.citybuildsystem.home;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final HomeManager homeManager;
    private final MessageManager messageManager;

    public DelHomeCommand(CitybuildSystem plugin, HomeManager homeManager, MessageManager messageManager) {
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

        if (!player.hasPermission("cbsystem.home.delete")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.delhome.usage")));
            return true;
        }

        String homeName = args[0];

        homeManager.getHome(player, homeName).thenAccept(home -> {
            if (home == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("home.not-found").replace("%home%", homeName)));
                return;
            }

            homeManager.deleteHome(player, homeName).thenAccept(success -> {
                if (success) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("home.delete-success").replace("%home%", homeName)));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
                }
            });
        });

        return true;
    }
}

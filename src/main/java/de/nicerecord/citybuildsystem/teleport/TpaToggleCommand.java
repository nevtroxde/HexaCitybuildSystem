package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaToggleCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final TpaManager tpaManager;
    private final MessageManager messageManager;

    public TpaToggleCommand(CitybuildSystem plugin, TpaManager tpaManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.tpaManager = tpaManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.tpatoggle")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        tpaManager.toggleTpa(player.getUniqueId());
        boolean enabled = tpaManager.isTpaEnabled(player.getUniqueId());

        String message = enabled ? "teleport.tpa-enabled" : "teleport.tpa-disabled";
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage(message)));

        return true;
    }
}

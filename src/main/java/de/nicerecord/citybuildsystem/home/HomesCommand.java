package de.nicerecord.citybuildsystem.home;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomesCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final HomeGUI homeGUI;
    private final MessageManager messageManager;

    public HomesCommand(CitybuildSystem plugin, HomeGUI homeGUI, MessageManager messageManager) {
        this.plugin = plugin;
        this.homeGUI = homeGUI;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cbsystem.home.use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        homeGUI.openHomesGUI(player);
        return true;
    }
}

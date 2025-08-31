package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpDenyCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final TpaManager tpaManager;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public TpDenyCommand(CitybuildSystem plugin, TpaManager tpaManager, MessageManager messageManager, ConfigManager configManager) {
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

        if (!player.hasPermission("cbsystem.tpdeny")) {
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

        tpaManager.removeRequest(request.getRequester());

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("teleport.tpa-denied")));

        if (requester != null) {
            requester.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("teleport.tpa-denied-by").replace("%player%", player.getName())));
        }

        return true;
    }
}

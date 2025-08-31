package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public SpeedCommand(CitybuildSystem plugin, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.speed.usage")));
            return true;
        }

        float speed;
        try {
            speed = Float.parseFloat(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.speed.usage")));
            return true;
        }

        float minSpeed = (float) configManager.getMinSpeed();
        float maxSpeed = (float) configManager.getMaxSpeed();

        if (speed < minSpeed || speed > maxSpeed) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.invalid-speed")
                    .replace("%min%", String.valueOf(minSpeed))
                    .replace("%max%", String.valueOf(maxSpeed))));
            return true;
        }

        float normalizedSpeed = speed / 10.0f;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("cbsystem.speed")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            if (player.isFlying()) {
                player.setFlySpeed(normalizedSpeed);
            } else {
                player.setWalkSpeed(normalizedSpeed);
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.speed-changed-self").replace("%speed%", String.valueOf(speed))));
        } else {
            if (!sender.hasPermission("cbsystem.speed.other")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[1])));
                return true;
            }

            if (target.isFlying()) {
                target.setFlySpeed(normalizedSpeed);
            } else {
                target.setWalkSpeed(normalizedSpeed);
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.speed-changed-other")
                    .replace("%player%", target.getName())
                    .replace("%speed%", String.valueOf(speed))));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.speed-changed-by").replace("%speed%", String.valueOf(speed))));
        }

        return true;
    }
}

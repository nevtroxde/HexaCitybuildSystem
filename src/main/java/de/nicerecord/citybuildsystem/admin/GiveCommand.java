package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public GiveCommand(CitybuildSystem plugin, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.give")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.give.usage")));
            return true;
        }

        Material material;
        try {
            material = Material.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.invalid-item").replace("%item%", args[0])));
            return true;
        }

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0 || amount > configManager.getGiveMaxAmount()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("admin.invalid-amount").replace("%max%", String.valueOf(configManager.getGiveMaxAmount()))));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("commands.give.usage")));
                return true;
            }
        }

        Player target;
        if (args.length > 2) {
            target = plugin.getServer().getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("general.player-not-found").replace("%player%", args[2])));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.only-players")));
                return true;
            }
            target = (Player) sender;
        }

        ItemStack item = new ItemStack(material, amount);
        target.getInventory().addItem(item);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            messageManager.getMessage("admin.give-success")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", material.name())
                .replace("%player%", target.getName())));

        if (!target.equals(sender)) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("admin.give-received")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%item%", material.name())));
        }

        return true;
    }
}

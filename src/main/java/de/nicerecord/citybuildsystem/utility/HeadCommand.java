package de.nicerecord.citybuildsystem.utility;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;

    public HeadCommand(CitybuildSystem plugin, MessageManager messageManager) {
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

        if (args.length == 0) {
            if (!player.hasPermission("cbsystem.head")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            ItemStack skull = createPlayerHead(player.getName());
            player.getInventory().addItem(skull);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("utility.head-given-self")));
        } else {
            if (!player.hasPermission("cbsystem.head.other")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return true;
            }

            String targetName = args[0];
            ItemStack skull = createPlayerHead(targetName);
            player.getInventory().addItem(skull);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("utility.head-given-other").replace("%player%", targetName)));
        }

        return true;
    }

    private ItemStack createPlayerHead(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(playerName);
        skullMeta.setDisplayName(ChatColor.YELLOW + playerName + "'s Kopf");
        skull.setItemMeta(skullMeta);
        return skull;
    }
}

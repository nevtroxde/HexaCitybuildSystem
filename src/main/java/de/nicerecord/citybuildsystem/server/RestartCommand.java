package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartCommand implements CommandExecutor {
    private final CitybuildSystem plugin;
    private final MessageManager messageManager;
    private static BukkitRunnable restartTask;

    public RestartCommand(CitybuildSystem plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cbsystem.restart")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            if (restartTask != null) {
                restartTask.cancel();
                restartTask = null;
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("server.restart-cancelled")));
                return true;
            }
        }

        int seconds = 30;
        if (args.length > 0) {
            try {
                seconds = Integer.parseInt(args[0]);
                if (seconds <= 0) {
                    seconds = 30;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        startRestartCountdown(seconds);
        return true;
    }

    private void startRestartCountdown(int seconds) {
        if (restartTask != null) {
            restartTask.cancel();
        }

        restartTask = new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    Bukkit.getServer().shutdown();
                    this.cancel();
                    return;
                }

                if (timeLeft == 60 || timeLeft == 30 || timeLeft == 15 ||
                    timeLeft == 10 || timeLeft <= 5) {
                    String message = ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("server.restart-countdown")
                            .replace("%time%", String.valueOf(timeLeft)));
                    Bukkit.broadcastMessage(message);
                }

                timeLeft--;
            }
        };

        restartTask.runTaskTimer(plugin, 0L, 20L);
    }

    public static void cancelRestart() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }
    }
}

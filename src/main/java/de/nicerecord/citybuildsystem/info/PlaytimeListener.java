package de.nicerecord.citybuildsystem.info;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlaytimeListener implements Listener {
    private final CitybuildSystem plugin;
    private final PlaytimeManager playtimeManager;

    public PlaytimeListener(CitybuildSystem plugin, PlaytimeManager playtimeManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playtimeManager.startSession(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playtimeManager.endSession(event.getPlayer());
    }
}

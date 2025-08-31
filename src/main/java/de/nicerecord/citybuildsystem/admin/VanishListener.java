package de.nicerecord.citybuildsystem.admin;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VanishListener implements Listener {
    private final CitybuildSystem plugin;

    public VanishListener(CitybuildSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        VanishCommand.handlePlayerJoin(event.getPlayer(), plugin);
    }
}

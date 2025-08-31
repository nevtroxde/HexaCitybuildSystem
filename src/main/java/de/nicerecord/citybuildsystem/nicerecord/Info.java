package de.nicerecord.citybuildsystem.nicerecord;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Info implements Listener {

    private String infoprefix = "§6§lCBSYSTEM §8» §7";

    @EventHandler
    public void onInfoCommand(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        if (message.equalsIgnoreCase("#info")) {
            event.getPlayer().sendMessage(infoprefix + "§e§lCitybuild System Info§8:");
            event.getPlayer().sendMessage(infoprefix + "§7Version§8: §eThis is a download version of the Citybuild System");
            event.getPlayer().sendMessage(infoprefix + "§7Author§8: §eNiceRecord");
            event.getPlayer().sendMessage(infoprefix + "§7Website§8: §ehttps://modrinth.com/user/nicerecord");
            event.setCancelled(true);
        }
    }
}

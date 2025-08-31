package de.nicerecord.citybuildsystem.utils;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final CitybuildSystem plugin;
    private final FileConfiguration config;

    public ConfigManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getDatabaseFile() {
        return config.getString("database.file", "homes.db");
    }

    public String getPlaytimeFile() {
        return config.getString("database.playtime-file", "playtime.db");
    }

    public String getWarpsFile() {
        return config.getString("database.warps-file", "warps.db");
    }

    public int getMaxHomes() {
        return config.getInt("homes.max-homes", 28);
    }

    public int getDefaultHomes() {
        return config.getInt("homes.default-homes", 3);
    }

    public double getHomePrice() {
        return config.getDouble("homes.home-price", 1000.0);
    }

    public boolean isEconomyEnabled() {
        return config.getBoolean("homes.economy-enabled", true);
    }

    public String getMessagePrefix() {
        return config.getString("messages.prefix", "&6&lCBSYSTEM &8» ");
    }

    public int getTeleportDelay() {
        return config.getInt("teleport.delay", 3);
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("teleport.cancel-on-move", true);
    }

    public int getTpaTimeout() {
        return config.getInt("teleport.tpa-timeout", 30);
    }

    public double getMinSpeed() {
        return config.getDouble("commands.speed.min-speed", 0.0);
    }

    public double getMaxSpeed() {
        return config.getDouble("commands.speed.max-speed", 10.0);
    }

    public int getFeedCooldown() {
        return config.getInt("commands.feed.cooldown", 0);
    }

    public int getHealCooldown() {
        return config.getInt("commands.heal.cooldown", 0);
    }

    public String getBroadcastPrefix() {
        return config.getString("commands.broadcast.prefix", "&4[&cBROADCAST&4]&r ");
    }

    public int getGiveMaxAmount() {
        return config.getInt("commands.give.max-amount", 64);
    }

    public boolean isMaintenanceEnabled() {
        return config.getBoolean("server.maintenance.enabled", false);
    }

    public boolean isSlowchatEnabled() {
        return config.getBoolean("server.slowchat.enabled", false);
    }

    public int getSlowchatDelay() {
        return config.getInt("server.slowchat.delay", 5);
    }

    public boolean isDiscordEnabled() {
        return config.getBoolean("social.discord.enabled", true);
    }

    public String getDiscordUrl() {
        return config.getString("social.discord.url", "https://discord.gg/yourserver");
    }

    public boolean isYoutubeEnabled() {
        return config.getBoolean("social.youtube.enabled", true);
    }

    public String getYoutubeUrl() {
        return config.getString("social.youtube.url", "https://youtube.com/yourchannel");
    }

    public String getDefaultLanguage() {
        return config.getString("language.default", "en");
    }
}

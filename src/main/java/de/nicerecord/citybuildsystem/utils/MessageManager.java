package de.nicerecord.citybuildsystem.utils;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MessageManager {
    private final CitybuildSystem plugin;
    private final ConfigManager configManager;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private String currentLanguage;

    public MessageManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.currentLanguage = configManager.getDefaultLanguage();
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "lang/messages-" + currentLanguage + ".yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("lang/messages-" + currentLanguage + ".yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        this.currentLanguage = configManager.getDefaultLanguage();
        loadMessages();
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path, "&cNachricht nicht gefunden: " + path);
        String prefix = configManager.getMessagePrefix();
        return prefix + message;
    }

    public String getMessageWithoutPrefix(String path) {
        return messagesConfig.getString(path, "&cNachricht nicht gefunden: " + path);
    }

    public List<String> getMessageList(String path) {
        return messagesConfig.getStringList(path);
    }

    public void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der messages-de.yml: " + e.getMessage());
        }
    }
}

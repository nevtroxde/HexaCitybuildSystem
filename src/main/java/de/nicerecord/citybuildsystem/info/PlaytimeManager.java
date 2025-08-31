package de.nicerecord.citybuildsystem.info;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlaytimeManager {
    private final CitybuildSystem plugin;
    private Connection connection;
    private final Map<UUID, Long> sessionStart = new HashMap<>();

    public PlaytimeManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = new File(dataFolder, plugin.getConfigManager().getPlaytimeFile()).getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            String createTable = "CREATE TABLE IF NOT EXISTS playtime (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL UNIQUE," +
                    "player_name TEXT NOT NULL," +
                    "total_time INTEGER NOT NULL DEFAULT 0," +
                    "last_seen INTEGER NOT NULL DEFAULT 0" +
                    ")";

            try (PreparedStatement stmt = connection.prepareStatement(createTable)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Initialisieren der Playtime-Datenbank: " + e.getMessage());
        }
    }

    public void startSession(Player player) {
        sessionStart.put(player.getUniqueId(), System.currentTimeMillis());

        // Create or update player record
        CompletableFuture.runAsync(() -> {
            try {
                String sql = "INSERT OR IGNORE INTO playtime (player_uuid, player_name, total_time, last_seen) VALUES (?, ?, 0, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, player.getName());
                    stmt.setLong(3, System.currentTimeMillis());
                    stmt.executeUpdate();
                }

                // Update player name if changed
                String updateNameSQL = "UPDATE playtime SET player_name = ?, last_seen = ? WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateNameSQL)) {
                    stmt.setString(1, player.getName());
                    stmt.setLong(2, System.currentTimeMillis());
                    stmt.setString(3, player.getUniqueId().toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Starten der Spielzeit-Session: " + e.getMessage());
            }
        });
    }

    public void endSession(Player player) {
        Long startTime = sessionStart.remove(player.getUniqueId());
        if (startTime == null) {
            return;
        }

        long sessionTime = System.currentTimeMillis() - startTime;

        CompletableFuture.runAsync(() -> {
            try {
                String sql = "UPDATE playtime SET total_time = total_time + ?, last_seen = ? WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, sessionTime);
                    stmt.setLong(2, System.currentTimeMillis());
                    stmt.setString(3, player.getUniqueId().toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Beenden der Spielzeit-Session: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<PlaytimeData> getPlaytime(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM playtime WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            long totalTime = rs.getLong("total_time");

                            // Add current session time if player is online
                            Long sessionStartTime = sessionStart.get(playerUUID);
                            if (sessionStartTime != null) {
                                totalTime += System.currentTimeMillis() - sessionStartTime;
                            }

                            return new PlaytimeData(
                                    playerUUID,
                                    rs.getString("player_name"),
                                    totalTime,
                                    rs.getLong("last_seen")
                            );
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen der Spielzeit: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<PlaytimeData> getPlaytime(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM playtime WHERE LOWER(player_name) = LOWER(?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                            long totalTime = rs.getLong("total_time");

                            // Add current session time if player is online
                            Long sessionStartTime = sessionStart.get(playerUUID);
                            if (sessionStartTime != null) {
                                totalTime += System.currentTimeMillis() - sessionStartTime;
                            }

                            return new PlaytimeData(
                                    playerUUID,
                                    rs.getString("player_name"),
                                    totalTime,
                                    rs.getLong("last_seen")
                            );
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen der Spielzeit: " + e.getMessage());
            }
            return null;
        });
    }

    public String formatPlaytime(long milliseconds) {
        if (milliseconds < 0) {
            return "0 Sekunden";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" Tag").append(days == 1 ? "" : "e").append(" ");
        }
        if (hours > 0) {
            result.append(hours).append(" Stunde").append(hours == 1 ? "" : "n").append(" ");
        }
        if (minutes > 0) {
            result.append(minutes).append(" Minute").append(minutes == 1 ? "" : "n").append(" ");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append(" Sekunde").append(seconds == 1 ? "" : "n");
        }

        return result.toString().trim();
    }

    public void saveAllSessions() {
        for (UUID playerUUID : new HashMap<>(sessionStart).keySet()) {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null) {
                endSession(player);
                startSession(player);
            }
        }
    }

    public void close() {
        for (UUID playerUUID : new HashMap<>(sessionStart).keySet()) {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null) {
                endSession(player);
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Schließen der Playtime-Datenbankverbindung: " + e.getMessage());
        }
    }
}

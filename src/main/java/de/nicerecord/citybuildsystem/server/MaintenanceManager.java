package de.nicerecord.citybuildsystem.server;

import de.nicerecord.citybuildsystem.CitybuildSystem;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MaintenanceManager {
    private final CitybuildSystem plugin;
    private Connection connection;
    private boolean maintenanceEnabled = false;

    public MaintenanceManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "maintenance.db");

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Initialisieren der Maintenance-Datenbank: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String createWhitelistTable = "CREATE TABLE IF NOT EXISTS maintenance_whitelist (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_name TEXT NOT NULL UNIQUE," +
                "player_uuid TEXT NOT NULL UNIQUE," +
                "added_by TEXT," +
                "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createWhitelistTable);
        }
    }

    public boolean isMaintenanceEnabled() {
        return maintenanceEnabled;
    }

    public void setMaintenanceEnabled(boolean enabled) {
        this.maintenanceEnabled = enabled;
    }

    public boolean addToWhitelist(String playerName, UUID playerUuid, String addedBy) {
        String sql = "INSERT OR REPLACE INTO maintenance_whitelist (player_name, player_uuid, added_by) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName.toLowerCase());
            pstmt.setString(2, playerUuid.toString());
            pstmt.setString(3, addedBy);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Hinzufügen zur Maintenance-Whitelist: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromWhitelist(String playerName) {
        String sql = "DELETE FROM maintenance_whitelist WHERE player_name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName.toLowerCase());
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Entfernen aus der Maintenance-Whitelist: " + e.getMessage());
            return false;
        }
    }

    public boolean isWhitelisted(String playerName) {
        String sql = "SELECT COUNT(*) FROM maintenance_whitelist WHERE player_name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Überprüfen der Maintenance-Whitelist: " + e.getMessage());
            return false;
        }
    }

    public boolean isWhitelisted(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM maintenance_whitelist WHERE player_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Überprüfen der Maintenance-Whitelist: " + e.getMessage());
            return false;
        }
    }

    public List<String> getWhitelistedPlayers() {
        List<String> players = new ArrayList<>();
        String sql = "SELECT player_name FROM maintenance_whitelist ORDER BY player_name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                players.add(rs.getString("player_name"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Abrufen der Maintenance-Whitelist: " + e.getMessage());
        }

        return players;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Schließen der Maintenance-Datenbank: " + e.getMessage());
            }
        }
    }
}
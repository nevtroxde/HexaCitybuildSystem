package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.Location;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class WarpManager {
    private final CitybuildSystem plugin;
    private Connection connection;

    public WarpManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = new File(dataFolder, plugin.getConfigManager().getWarpsFile()).getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            String createTable = "CREATE TABLE IF NOT EXISTS warps (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "world TEXT NOT NULL," +
                    "x REAL NOT NULL," +
                    "y REAL NOT NULL," +
                    "z REAL NOT NULL," +
                    "yaw REAL NOT NULL," +
                    "pitch REAL NOT NULL" +
                    ")";

            try (PreparedStatement stmt = connection.prepareStatement(createTable)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Initialisieren der Warp-Datenbank: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> createWarp(String name, Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "INSERT INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, location.getWorld().getName());
                    stmt.setDouble(3, location.getX());
                    stmt.setDouble(4, location.getY());
                    stmt.setDouble(5, location.getZ());
                    stmt.setFloat(6, location.getYaw());
                    stmt.setFloat(7, location.getPitch());
                    stmt.executeUpdate();
                    return true;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Erstellen des Warps: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> deleteWarp(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "DELETE FROM warps WHERE name = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    int affected = stmt.executeUpdate();
                    return affected > 0;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Löschen des Warps: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Warp> getWarp(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM warps WHERE name = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return new Warp(
                                    rs.getString("name"),
                                    rs.getString("world"),
                                    rs.getDouble("x"),
                                    rs.getDouble("y"),
                                    rs.getDouble("z"),
                                    rs.getFloat("yaw"),
                                    rs.getFloat("pitch")
                            );
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen des Warps: " + e.getMessage());
            }
            return null;
        });
    }

    public boolean isValidWarpName(String name) {
        return name.matches("^[a-zA-Z0-9]+$") && name.length() <= 16;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Schließen der Warp-Datenbankverbindung: " + e.getMessage());
        }
    }
}

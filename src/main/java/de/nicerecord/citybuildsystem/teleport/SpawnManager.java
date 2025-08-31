package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class SpawnManager {
    private final CitybuildSystem plugin;
    private Connection connection;

    public SpawnManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = new File(dataFolder, "spawn.db").getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            String createTable = "CREATE TABLE IF NOT EXISTS spawn (" +
                    "id INTEGER PRIMARY KEY," +
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
            plugin.getLogger().severe("Fehler beim Initialisieren der Spawn-Datenbank: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> setSpawn(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String deleteSQL = "DELETE FROM spawn";
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL)) {
                    deleteStmt.executeUpdate();
                }

                String insertSQL = "INSERT INTO spawn (id, world, x, y, z, yaw, pitch) VALUES (1, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                    insertStmt.setString(1, location.getWorld().getName());
                    insertStmt.setDouble(2, location.getX());
                    insertStmt.setDouble(3, location.getY());
                    insertStmt.setDouble(4, location.getZ());
                    insertStmt.setFloat(5, location.getYaw());
                    insertStmt.setFloat(6, location.getPitch());
                    insertStmt.executeUpdate();
                }

                FileConfiguration config = plugin.getConfig();
                config.set("server.spawn.world", location.getWorld().getName());
                config.set("server.spawn.x", location.getX());
                config.set("server.spawn.y", location.getY());
                config.set("server.spawn.z", location.getZ());
                config.set("server.spawn.yaw", location.getYaw());
                config.set("server.spawn.pitch", location.getPitch());
                plugin.saveConfig();

                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Setzen des Spawns: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Spawn> getSpawn() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM spawn WHERE id = 1";
                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Spawn(
                                rs.getString("world"),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen des Spawns: " + e.getMessage());
            }

            FileConfiguration config = plugin.getConfig();
            return new Spawn(
                    config.getString("server.spawn.world", "world"),
                    config.getDouble("server.spawn.x", 0.0),
                    config.getDouble("server.spawn.y", 64.0),
                    config.getDouble("server.spawn.z", 0.0),
                    (float) config.getDouble("server.spawn.yaw", 0.0),
                    (float) config.getDouble("server.spawn.pitch", 0.0)
            );
        });
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Schließen der Spawn-Datenbankverbindung: " + e.getMessage());
        }
    }
}

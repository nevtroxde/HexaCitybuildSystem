package de.nicerecord.citybuildsystem.home;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HomeManager {
    private final CitybuildSystem plugin;
    private Connection connection;

    public HomeManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = new File(dataFolder, plugin.getConfig().getString("database.file")).getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            String createTable = "CREATE TABLE IF NOT EXISTS homes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "world TEXT NOT NULL," +
                    "x REAL NOT NULL," +
                    "y REAL NOT NULL," +
                    "z REAL NOT NULL," +
                    "yaw REAL NOT NULL," +
                    "pitch REAL NOT NULL," +
                    "UNIQUE(player_uuid, name)" +
                    ")";

            try (PreparedStatement stmt = connection.prepareStatement(createTable)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Initialisieren der Datenbank: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> createHome(Player player, String name, Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "INSERT INTO homes (player_uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, name);
                    stmt.setString(3, location.getWorld().getName());
                    stmt.setDouble(4, location.getX());
                    stmt.setDouble(5, location.getY());
                    stmt.setDouble(6, location.getZ());
                    stmt.setFloat(7, location.getYaw());
                    stmt.setFloat(8, location.getPitch());
                    stmt.executeUpdate();
                    return true;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Erstellen des Homes: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> deleteHome(Player player, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "DELETE FROM homes WHERE player_uuid = ? AND name = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, name);
                    int affected = stmt.executeUpdate();
                    return affected > 0;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Löschen des Homes: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Home> getHome(Player player, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM homes WHERE player_uuid = ? AND name = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, name);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return new Home(
                                    player.getUniqueId(),
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
                plugin.getLogger().severe("Fehler beim Abrufen des Homes: " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<List<Home>> getPlayerHomes(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            List<Home> homes = new ArrayList<>();
            try {
                String sql = "SELECT * FROM homes WHERE player_uuid = ? ORDER BY name";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            homes.add(new Home(
                                    player.getUniqueId(),
                                    rs.getString("name"),
                                    rs.getString("world"),
                                    rs.getDouble("x"),
                                    rs.getDouble("y"),
                                    rs.getDouble("z"),
                                    rs.getFloat("yaw"),
                                    rs.getFloat("pitch")
                            ));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen der Homes: " + e.getMessage());
            }
            return homes;
        });
    }

    public int getMaxHomesForPlayer(Player player) {
        int maxHomes = 0;
        for (int i = 1; i <= plugin.getConfig().getInt("homes.max-homes"); i++) {
            if (player.hasPermission("cbsystem.home.set." + i) || player.hasPermission("cbsystem.home.set.*")) {
                maxHomes = i;
            }
        }
        return maxHomes;
    }

    public CompletableFuture<Boolean> purchaseHomeSlot(Player player, Economy economy) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Aktuelle maximale Homes des Spielers
                int currentMaxHomes = getMaxHomesForPlayer(player);
                int configMaxHomes = plugin.getConfig().getInt("homes.max-homes");

                // Prüfe ob Spieler bereits das Maximum erreicht hat
                if (currentMaxHomes >= configMaxHomes) {
                    return false;
                }

                // Berechne den Preis für das nächste Home-Slot
                double price = plugin.getConfig().getDouble("homes.home-price");

                // Prüfe ob Spieler genug Geld hat
                if (economy.getBalance(player) < price) {
                    return false;
                }

                // Ziehe das Geld ab
                if (!economy.withdrawPlayer(player, price).transactionSuccess()) {
                    return false;
                }

                // Gebe dem Spieler die Berechtigung für ein zusätzliches Home
                int nextHomeSlot = currentMaxHomes + 1;

                // Verwende LuckPerms oder PermissionsEx API wenn verfügbar
                // Für dieses Beispiel verwenden wir den Bukkit Permission Manager (temporär)
                player.addAttachment(plugin, "cbsystem.home.set." + nextHomeSlot, true);

                // TODO: Für permanente Berechtigungen sollte hier eine Integration
                // mit LuckPerms oder einem anderen Permission-Plugin erfolgen

                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Kauf eines Home-Slots: " + e.getMessage());
                return false;
            }
        });
    }

    public int getNextHomeSlotPrice() {
        return (int) plugin.getConfig().getDouble("homes.home-price");
    }

    public boolean isValidHomeName(String name) {
        return name.matches("^[a-zA-Z0-9]+$") && name.length() <= 16;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
        }
    }
}

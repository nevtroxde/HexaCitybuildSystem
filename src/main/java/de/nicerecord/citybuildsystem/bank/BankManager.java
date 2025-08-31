package de.nicerecord.citybuildsystem.bank;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BankManager {
    private final CitybuildSystem plugin;
    private Connection connection;

    public BankManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = new File(dataFolder, "bank.db").getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            String createTable = "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL UNIQUE," +
                    "player_name TEXT NOT NULL," +
                    "balance REAL NOT NULL DEFAULT 0.0," +
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))," +
                    "updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))" +
                    ")";

            try (PreparedStatement stmt = connection.prepareStatement(createTable)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Initialisieren der Bank-Datenbank: " + e.getMessage());
        }
    }

    public CompletableFuture<Double> getBalance(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT balance FROM bank_accounts WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUUID.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getDouble("balance");
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen des Bank-Guthabens: " + e.getMessage());
            }
            return 0.0;
        });
    }

    public CompletableFuture<Double> getBalance(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT balance FROM bank_accounts WHERE LOWER(player_name) = LOWER(?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getDouble("balance");
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen des Bank-Guthabens: " + e.getMessage());
            }
            return 0.0;
        });
    }

    public CompletableFuture<Boolean> deposit(Player player, double amount) {
        if (amount <= 0) return CompletableFuture.completedFuture(false);

        return CompletableFuture.supplyAsync(() -> {
            try {
                createAccountIfNotExists(player);

                String sql = "UPDATE bank_accounts SET balance = balance + ?, updated_at = strftime('%s', 'now') WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setDouble(1, amount);
                    stmt.setString(2, player.getUniqueId().toString());
                    int affected = stmt.executeUpdate();
                    return affected > 0;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Einzahlen: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> withdraw(Player player, double amount) {
        if (amount <= 0) return CompletableFuture.completedFuture(false);

        return CompletableFuture.supplyAsync(() -> {
            try {
                double currentBalance = getBalance(player.getUniqueId()).join();
                if (currentBalance < amount) {
                    return false;
                }

                String sql = "UPDATE bank_accounts SET balance = balance - ?, updated_at = strftime('%s', 'now') WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setDouble(1, amount);
                    stmt.setString(2, player.getUniqueId().toString());
                    int affected = stmt.executeUpdate();
                    return affected > 0;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Auszahlen: " + e.getMessage());
                return false;
            }
        });
    }

    private void createAccountIfNotExists(Player player) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM bank_accounts WHERE player_uuid = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO bank_accounts (player_uuid, player_name, balance) VALUES (?, ?, 0.0)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, player.getUniqueId().toString());
                        insertStmt.setString(2, player.getName());
                        insertStmt.executeUpdate();
                    }
                } else {
                    String updateSql = "UPDATE bank_accounts SET player_name = ? WHERE player_uuid = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, player.getName());
                        updateStmt.setString(2, player.getUniqueId().toString());
                        updateStmt.executeUpdate();
                    }
                }
            }
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Schließen der Bank-Datenbankverbindung: " + e.getMessage());
        }
    }
}

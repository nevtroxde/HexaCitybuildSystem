package de.nicerecord.citybuildsystem.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private final CitybuildSystem plugin;
    private final String projectSlug;
    private final String currentVersion;
    private final String projectUrl;

    public UpdateChecker(CitybuildSystem plugin) {
        this.plugin = plugin;
        this.projectSlug = "hexacitybuildsystem";
        this.currentVersion = plugin.getDescription().getVersion();
        this.projectUrl = "https://modrinth.com/plugin/hexacitybuildsystem/versions";
    }

    public CompletableFuture<UpdateResult> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + projectSlug + "/version");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "CitybuildSystem/" + currentVersion + " (https://github.com/modrinth/modrinth)");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("UpdateChecker: HTTP " + responseCode + " beim Abrufen der Versionen");
                    return new UpdateResult(false, null, null, "HTTP Error: " + responseCode);
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JsonArray versions = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (versions.size() == 0) {
                    return new UpdateResult(false, null, null, "Keine Versionen gefunden");
                }

                JsonObject latestVersion = versions.get(0).getAsJsonObject();
                String latestVersionNumber = latestVersion.get("version_number").getAsString();

                boolean hasUpdate = isNewerVersion(latestVersionNumber, currentVersion);

                if (hasUpdate) {
                    return new UpdateResult(true, latestVersionNumber, projectUrl, null);
                } else {
                    return new UpdateResult(false, latestVersionNumber, null, null);
                }

            } catch (IOException e) {
                plugin.getLogger().warning("UpdateChecker Fehler: " + e.getMessage());
                return new UpdateResult(false, null, null, e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().warning("UpdateChecker unerwarteter Fehler: " + e.getMessage());
                return new UpdateResult(false, null, null, e.getMessage());
            }
        });
    }

    private boolean isNewerVersion(String latest, String current) {
        String cleanLatest = latest.replaceAll("-SNAPSHOT", "");
        String cleanCurrent = current.replaceAll("-SNAPSHOT", "");

        try {
            return compareVersions(cleanLatest, cleanCurrent) > 0;
        } catch (Exception e) {
            return !latest.equals(current);
        }
    }

    private int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");

        int maxLength = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < maxLength; i++) {
            int v1Part = i < v1Parts.length ? parseVersionPart(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? parseVersionPart(v2Parts[i]) : 0;

            if (v1Part != v2Part) {
                return Integer.compare(v1Part, v2Part);
            }
        }

        return 0;
    }

    private int parseVersionPart(String part) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(part);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    public void notifyAdmins(UpdateResult result) {
        if (!result.hasUpdate()) return;

        String message = plugin.getMessageManager().getMessage("update.available")
                .replace("%current_version%", currentVersion)
                .replace("%latest_version%", result.getLatestVersion())
                .replace("%download_url%", result.getDownloadUrl());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("cbsystem.admin") || player.isOp()) {
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
            }
        }

        plugin.getServer().getConsoleSender().sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
    }

    public static class UpdateResult {
        private final boolean hasUpdate;
        private final String latestVersion;
        private final String downloadUrl;
        private final String error;

        public UpdateResult(boolean hasUpdate, String latestVersion, String downloadUrl, String error) {
            this.hasUpdate = hasUpdate;
            this.latestVersion = latestVersion;
            this.downloadUrl = downloadUrl;
            this.error = error;
        }

        public boolean hasUpdate() {
            return hasUpdate;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getError() {
            return error;
        }

        public boolean hasError() {
            return error != null;
        }
    }
}

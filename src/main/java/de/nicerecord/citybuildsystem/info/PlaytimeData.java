package de.nicerecord.citybuildsystem.info;

import java.util.UUID;

public class PlaytimeData {
    private final UUID playerUUID;
    private final String playerName;
    private final long totalTime;
    private final long lastSeen;

    public PlaytimeData(UUID playerUUID, String playerName, long totalTime, long lastSeen) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.totalTime = totalTime;
        this.lastSeen = lastSeen;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getLastSeen() {
        return lastSeen;
    }
}

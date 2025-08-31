package de.nicerecord.citybuildsystem.teleport;

import org.bukkit.entity.Player;

import java.util.UUID;

public class TpaRequest {
    private final UUID requester;
    private final UUID target;
    private final TpaType type;
    private final long timestamp;

    public TpaRequest(Player requester, Player target, TpaType type) {
        this.requester = requester.getUniqueId();
        this.target = target.getUniqueId();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getRequester() {
        return requester;
    }

    public UUID getTarget() {
        return target;
    }

    public TpaType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isExpired(int timeoutSeconds) {
        return System.currentTimeMillis() - timestamp > (timeoutSeconds * 1000L);
    }

    public enum TpaType {
        TPA,
        TPAHERE
    }
}

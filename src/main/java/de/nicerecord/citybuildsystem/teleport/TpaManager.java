package de.nicerecord.citybuildsystem.teleport;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TpaManager {
    private final CitybuildSystem plugin;
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Set<UUID> tpaDisabledPlayers = ConcurrentHashMap.newKeySet();
    private BukkitRunnable cleanupTask;

    public TpaManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    public boolean sendTpaRequest(Player requester, Player target, TpaRequest.TpaType type) {
        if (tpaDisabledPlayers.contains(target.getUniqueId())) {
            return false;
        }

        pendingRequests.remove(requester.getUniqueId());

        TpaRequest request = new TpaRequest(requester, target, type);
        pendingRequests.put(requester.getUniqueId(), request);

        return true;
    }

    public TpaRequest getPendingRequest(UUID playerUUID) {
        return pendingRequests.get(playerUUID);
    }

    public TpaRequest getRequestForTarget(UUID targetUUID) {
        for (TpaRequest request : pendingRequests.values()) {
            if (request.getTarget().equals(targetUUID)) {
                return request;
            }
        }
        return null;
    }

    public void removeRequest(UUID requesterUUID) {
        pendingRequests.remove(requesterUUID);
    }

    public void removePendingRequestsForTarget(UUID targetUUID) {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().getTarget().equals(targetUUID));
    }

    public boolean isTpaEnabled(UUID playerUUID) {
        return !tpaDisabledPlayers.contains(playerUUID);
    }

    public void toggleTpa(UUID playerUUID) {
        if (tpaDisabledPlayers.contains(playerUUID)) {
            tpaDisabledPlayers.remove(playerUUID);
        } else {
            tpaDisabledPlayers.add(playerUUID);
            removePendingRequestsForTarget(playerUUID);
        }
    }

    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                int timeoutSeconds = plugin.getConfigManager().getTpaTimeout();
                pendingRequests.entrySet().removeIf(entry ->
                    entry.getValue().isExpired(timeoutSeconds));
            }
        };
        cleanupTask.runTaskTimer(plugin, 200L, 200L);
    }

    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
    }

    public Collection<TpaRequest> getAllRequests() {
        return pendingRequests.values();
    }
}

package de.nicerecord.citybuildsystem.messaging;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageSystem {
    private final CitybuildSystem plugin;
    private final Map<UUID, UUID> lastConversations = new HashMap<>();

    public MessageSystem(CitybuildSystem plugin) {
        this.plugin = plugin;
    }

    public void sendPrivateMessage(Player sender, Player receiver, String message) {
        lastConversations.put(sender.getUniqueId(), receiver.getUniqueId());
        lastConversations.put(receiver.getUniqueId(), sender.getUniqueId());

        String senderFormat = plugin.getMessageManager().getMessageWithoutPrefix("social.message-sent")
                .replace("%player%", receiver.getName())
                .replace("%message%", message);

        String receiverFormat = plugin.getMessageManager().getMessageWithoutPrefix("social.message-received")
                .replace("%player%", sender.getName())
                .replace("%message%", message);

        sender.sendMessage(senderFormat);
        receiver.sendMessage(receiverFormat);

        plugin.getLogger().info("[PM] " + sender.getName() + " -> " + receiver.getName() + ": " + message);
    }

    public Player getLastConversationPartner(Player player) {
        UUID partnerUUID = lastConversations.get(player.getUniqueId());
        if (partnerUUID == null) {
            return null;
        }

        return plugin.getServer().getPlayer(partnerUUID);
    }

    public void removeConversation(UUID playerUUID) {
        lastConversations.remove(playerUUID);

        lastConversations.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }

    public boolean hasRecentConversation(Player player) {
        return lastConversations.containsKey(player.getUniqueId());
    }

    public void clearAllConversations() {
        lastConversations.clear();
    }
}

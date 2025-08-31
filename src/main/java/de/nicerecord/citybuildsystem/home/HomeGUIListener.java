package de.nicerecord.citybuildsystem.home;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.GUIManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeGUIListener implements Listener {
    private final CitybuildSystem plugin;
    private final HomeManager homeManager;
    private final ConfigManager configManager;
    private final GUIManager guiManager;
    private final MessageManager messageManager;
    private final Economy economy;
    private final SetHomeCommand setHomeCommand;
    private final Map<UUID, Boolean> waitingForHomeName = new HashMap<>();

    public HomeGUIListener(CitybuildSystem plugin, HomeManager homeManager, ConfigManager configManager,
                          GUIManager guiManager, MessageManager messageManager, Economy economy, SetHomeCommand setHomeCommand) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.configManager = configManager;
        this.guiManager = guiManager;
        this.messageManager = messageManager;
        this.economy = economy;
        this.setHomeCommand = setHomeCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String guiTitle = ChatColor.translateAlternateColorCodes('&', guiManager.getHomeGUITitle());

        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;

        String displayName = meta.getDisplayName();

        // Handle close button
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getCloseName()))) {
            player.closeInventory();
            return;
        }

        // Handle navigation - Previous page
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getPreviousPageName()))) {
            int currentPage = getCurrentPageFromInstance(player);
            if (currentPage > 1) {
                HomeGUI homeGUI = new HomeGUI(plugin, homeManager, configManager, guiManager, messageManager, economy);
                homeGUI.openHomesGUI(player, currentPage - 1);
            }
            return;
        }

        // Handle navigation - Next page
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getNextPageName()))) {
            int currentPage = getCurrentPageFromInstance(player);
            int configMaxHomes = configManager.getMaxHomes(); // Verwende Config-Maximum
            int totalPages = (int) Math.ceil((double) configMaxHomes / 28); // HOMES_PER_PAGE
            if (currentPage < totalPages) {
                HomeGUI homeGUI = new HomeGUI(plugin, homeManager, configManager, guiManager, messageManager, economy);
                homeGUI.openHomesGUI(player, currentPage + 1);
            }
            return;
        }

        // Handle info item (do nothing)
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getInfoName()))) {
            return;
        }

        // Handle empty home slots
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getHomeEmptyName()))) {
            player.closeInventory();

            if (!hasPermissionToSetHome(player)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
                return;
            }

            waitingForHomeName.put(player.getUniqueId(), true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.enter-name")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.enter-name-cancel")));
            return;
        }

        // Handle locked home slots (both old and new buy-next-home items)
        if (displayName.equals(ChatColor.translateAlternateColorCodes('&', guiManager.getHomeLockedName())) ||
            displayName.contains("SLOT") && displayName.contains("KAUFEN")) {
            if (!configManager.isEconomyEnabled() || economy == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("bank.economy-not-available")));
                return;
            }

            int currentMaxHomes = homeManager.getMaxHomesForPlayer(player);
            int configMaxHomes = configManager.getMaxHomes();

            // Prüfe ob bereits das Maximum erreicht ist
            if (currentMaxHomes >= configMaxHomes) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.max-homes-purchased")));
                return;
            }

            double price = configManager.getHomePrice();

            // Prüfe ob genug Geld vorhanden ist
            if (economy.getBalance(player) < price) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("home.insufficient-funds").replace("%price%", String.valueOf((int) price))));
                return;
            }

            // Führe den Kauf durch
            homeManager.purchaseHomeSlot(player, economy).thenAccept(success -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (success) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("home.purchase-success").replace("%price%", String.valueOf((int) price))));

                        // Schließe das GUI und öffne es erneut um die Änderungen zu zeigen
                        player.closeInventory();
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            HomeGUI homeGUI = new HomeGUI(plugin, homeManager, configManager, guiManager, messageManager, economy);
                            int currentPage = homeGUI.getCurrentPage(player);
                            homeGUI.openHomesGUI(player, currentPage);
                        }, 5L);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.purchase-failed")));
                    }
                });
            });
            return;
        }

        // Handle existing home items
        if (displayName.startsWith(ChatColor.translateAlternateColorCodes('&', guiManager.getHomeSetName().replace("%name%", "")))) {
            String homeName = ChatColor.stripColor(displayName).replace("HOME ", "");

            if (event.getClick() == ClickType.LEFT) {
                player.closeInventory();
                plugin.getServer().dispatchCommand(player, "home " + homeName);
            } else if (event.getClick() == ClickType.RIGHT) {
                homeManager.deleteHome(player, homeName).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("home.delete-success").replace("%home%", homeName)));
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            HomeGUI homeGUI = new HomeGUI(plugin, homeManager, configManager, guiManager, messageManager, economy);
                            int currentPage = homeGUI.getCurrentPage(player);
                            homeGUI.openHomesGUI(player, currentPage);
                        }, 1L);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
                    }
                });
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!waitingForHomeName.containsKey(playerUUID)) {
            return;
        }

        event.setCancelled(true);
        waitingForHomeName.remove(playerUUID);

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.loading")));
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> processSetHomeFromGUI(player, message));
    }

    private void processSetHomeFromGUI(Player player, String homeName) {
        if (!homeManager.isValidHomeName(homeName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.invalid-name")));
            return;
        }

        if (!hasPermissionToSetHome(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.no-permission")));
            return;
        }

        homeManager.getHome(player, homeName).thenAccept(existingHome -> {
            if (existingHome != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("home.already-exists").replace("%home%", homeName)));
                return;
            }

            homeManager.getPlayerHomes(player).thenAccept(homes -> {
                int maxHomes = homeManager.getMaxHomesForPlayer(player);
                if (homes.size() >= maxHomes) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("home.max-homes-reached")));
                    return;
                }

                homeManager.createHome(player, homeName, player.getLocation()).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("home.set-success").replace("%home%", homeName)));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageManager.getMessage("general.error")));
                    }
                });
            });
        });
    }

    private boolean hasPermissionToSetHome(Player player) {
        if (player.hasPermission("cbsystem.home.set.*")) {
            return true;
        }

        for (int i = 1; i <= 28; i++) {
            if (player.hasPermission("cbsystem.home.set." + i)) {
                return true;
            }
        }

        return false;
    }

    public boolean isWaitingForHomeName(UUID playerUUID) {
        return waitingForHomeName.containsKey(playerUUID);
    }

    public void removeWaitingPlayer(UUID playerUUID) {
        waitingForHomeName.remove(playerUUID);
    }

    private int getCurrentPageFromInstance(Player player) {
        // Verwende die statische Map aus der HomeGUI-Klasse
        // Da playerPages jetzt static ist, können wir eine temporäre HomeGUI-Instanz erstellen
        HomeGUI tempGUI = new HomeGUI(plugin, homeManager, configManager, guiManager, messageManager, economy);
        return tempGUI.getCurrentPage(player);
    }
}

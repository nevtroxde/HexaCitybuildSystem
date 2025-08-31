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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeGUI {
    private final CitybuildSystem plugin;
    private final HomeManager homeManager;
    private final ConfigManager configManager;
    private final GUIManager guiManager;
    private final MessageManager messageManager;
    private final Economy economy;

    // Store current page for each player - STATIC damit es über Instanzen hinweg funktioniert
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    // Slots available for homes per page (excluding borders, navigation, info items)
    private static final int HOMES_PER_PAGE = 28;

    public HomeGUI(CitybuildSystem plugin, HomeManager homeManager, ConfigManager configManager, GUIManager guiManager, MessageManager messageManager, Economy economy) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.configManager = configManager;
        this.guiManager = guiManager;
        this.messageManager = messageManager;
        this.economy = economy;
    }

    public void openHomesGUI(Player player) {
        openHomesGUI(player, 1);
    }

    public void openHomesGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        String title = ChatColor.translateAlternateColorCodes('&', guiManager.getHomeGUITitle());
        Inventory inventory = Bukkit.createInventory(null, guiManager.getHomeGUISize(), title);

        homeManager.getPlayerHomes(player).thenAccept(homes -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int maxHomes = homeManager.getMaxHomesForPlayer(player);
                int configMaxHomes = configManager.getMaxHomes(); // Maximum aus Config

                // Berechne Paginierung basierend auf dem größeren Wert (entweder verfügbare Homes oder Config-Maximum)
                int homesForPagination = Math.max(maxHomes, configMaxHomes);
                int totalPages = calculateTotalPages(homesForPagination);

                fillBorder(inventory);
                inventory.setItem(53, createCloseItem());

                // Add navigation items if needed (basierend auf Config-Maximum, nicht Spieler-Maximum)
                if (totalPages > 1) {
                    if (page > 1) {
                        inventory.setItem(48, createPreviousPageItem()); // Slot 48 für vorherige Seite
                    }
                    if (page < totalPages) {
                        inventory.setItem(50, createNextPageItem()); // Slot 50 für nächste Seite
                    }
                }

                // Info-Item immer anzeigen (ohne Seiteninformation)
                inventory.setItem(49, createInfoItem(player, homes.size()));

                addHomeItems(inventory, player, homes, page, maxHomes, configMaxHomes);
                player.openInventory(inventory);
            });
        });
    }

    private int calculateTotalPages(int maxHomes) {
        return (int) Math.ceil((double) maxHomes / HOMES_PER_PAGE);
    }

    private void fillBorder(Inventory inventory) {
        ItemStack borderItem = createBorderItem();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(45 + i, borderItem);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }

    private ItemStack createBorderItem() {
        Material material = Material.valueOf(guiManager.getBorderMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getBorderName()));
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getBorderLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseItem() {
        Material material = Material.valueOf(guiManager.getCloseMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getCloseName()));
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getCloseLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(Player player, int usedHomes) {
        Material material = Material.valueOf(guiManager.getInfoMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getInfoName()));
        int maxHomes = homeManager.getMaxHomesForPlayer(player);
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getInfoLore()) {
            line = line.replace("%used%", String.valueOf(usedHomes))
                      .replace("%max%", String.valueOf(maxHomes));
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addHomeItems(Inventory inventory, Player player, List<Home> homes, int page, int maxHomes, int configMaxHomes) {
        int startIndex = (page - 1) * HOMES_PER_PAGE;
        int endIndex = Math.min(startIndex + HOMES_PER_PAGE, maxHomes);

        // Get valid slots for home items (excluding border)
        List<Integer> validSlots = getValidSlots();
        int slotIndex = 0;

        // Zeige alle verfügbaren Home-Slots für diese Seite
        for (int i = startIndex; i < endIndex && slotIndex < validSlots.size(); i++) {
            if (i < homes.size()) {
                // Existierendes Home
                Home home = homes.get(i);
                inventory.setItem(validSlots.get(slotIndex), createHomeItem(home));
            } else {
                // Leerer Home-Slot (kann gesetzt werden)
                inventory.setItem(validSlots.get(slotIndex), createEmptyHomeItem());
            }
            slotIndex++;
        }

        // Zeige "Nächsten Home kaufen" Button wenn Economy aktiviert ist und das Config-Maximum nicht erreicht ist
        if (configManager.isEconomyEnabled() && economy != null && slotIndex < validSlots.size()) {
            int currentMaxHomes = homeManager.getMaxHomesForPlayer(player);

            // Zeige Kauf-Button für den nächsten Home-Slot wenn das Config-Maximum noch nicht erreicht ist
            if (currentMaxHomes < configMaxHomes) {
                // Berechne welcher Home-Slot als nächstes gekauft werden kann
                int nextHomeSlot = currentMaxHomes + 1;

                // Prüfe ob der nächste kaufbare Slot auf dieser Seite angezeigt werden soll
                int nextSlotIndex = nextHomeSlot - 1; // 0-basiert
                if (nextSlotIndex >= startIndex && nextSlotIndex < startIndex + HOMES_PER_PAGE) {
                    // Berechne die richtige Slot-Position für den Kauf-Button
                    int buyButtonSlotIndex = nextSlotIndex - startIndex;
                    if (buyButtonSlotIndex < validSlots.size()) {
                        inventory.setItem(validSlots.get(buyButtonSlotIndex), createBuyNextHomeItem(nextHomeSlot));
                    }
                }
            }
        }
    }

    private List<Integer> getValidSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row < 5; row++) { // Rows 1-4 (skip borders)
            for (int col = 1; col < 8; col++) { // Cols 1-7 (skip borders)
                slots.add(row * 9 + col);
            }
        }
        return slots;
    }

    private ItemStack createHomeItem(Home home) {
        Material material = Material.valueOf(guiManager.getHomeSetMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String name = guiManager.getHomeSetName().replace("%name%", home.getName());
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getHomeSetLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptyHomeItem() {
        Material material = Material.valueOf(guiManager.getHomeEmptyMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getHomeEmptyName()));
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getHomeEmptyLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLockedHomeItem() {
        Material material = Material.valueOf(guiManager.getHomeLockedMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getHomeLockedName()));
        double price = configManager.getHomePrice();
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getHomeLockedLore()) {
            line = line.replace("%price%", String.valueOf((int) price));
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPreviousPageItem() {
        Material material = Material.valueOf(guiManager.getPreviousPageMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getPreviousPageName()));
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getPreviousPageLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextPageItem() {
        Material material = Material.valueOf(guiManager.getNextPageMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', guiManager.getNextPageName()));
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getNextPageLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBuyNextHomeItem(int homeSlotNumber) {
        Material material = Material.valueOf(guiManager.getHomeLockedMaterial());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String name = guiManager.getHomeLockedName().replace("KAUFEN", "SLOT " + homeSlotNumber + " KAUFEN");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        double price = configManager.getHomePrice();
        List<String> lore = new ArrayList<>();
        for (String line : guiManager.getHomeLockedLore()) {
            line = line.replace("%price%", String.valueOf((int) price))
                      .replace("Home", "Home-Slot " + homeSlotNumber);
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }

    public void removePlayerPage(Player player) {
        playerPages.remove(player.getUniqueId());
    }
}

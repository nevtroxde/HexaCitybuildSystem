package de.nicerecord.citybuildsystem.utils;

import de.nicerecord.citybuildsystem.CitybuildSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GUIManager {
    private final CitybuildSystem plugin;
    private final ConfigManager configManager;
    private FileConfiguration guiConfig;
    private File guiFile;
    private String currentLanguage;

    public GUIManager(CitybuildSystem plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.currentLanguage = configManager.getDefaultLanguage();
        loadGUIConfig();
    }

    private void loadGUIConfig() {
        guiFile = new File(plugin.getDataFolder(), "lang/gui-" + currentLanguage + ".yml");
        if (!guiFile.exists()) {
            plugin.saveResource("lang/gui-" + currentLanguage + ".yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void reloadGUIConfig() {
        this.currentLanguage = configManager.getDefaultLanguage();
        loadGUIConfig();
    }

    public String getHomeGUITitle() {
        return guiConfig.getString("home-gui.title", "&6&lCBSYSTEM &8» Deine Homes");
    }

    public int getHomeGUISize() {
        return guiConfig.getInt("home-gui.size", 54);
    }

    public String getBorderMaterial() {
        return guiConfig.getString("home-gui.items.border.material", "BLACK_STAINED_GLASS_PANE");
    }

    public String getBorderName() {
        return guiConfig.getString("home-gui.items.border.name", "&7");
    }

    public List<String> getBorderLore() {
        return guiConfig.getStringList("home-gui.items.border.lore");
    }

    public String getCloseMaterial() {
        return guiConfig.getString("home-gui.items.close.material", "SPRUCE_DOOR");
    }

    public String getCloseName() {
        return guiConfig.getString("home-gui.items.close.name", "&c&lSCHLIEßEN");
    }

    public List<String> getCloseLore() {
        return guiConfig.getStringList("home-gui.items.close.lore");
    }

    public String getInfoMaterial() {
        return guiConfig.getString("home-gui.items.info.material", "PAPER");
    }

    public String getInfoName() {
        return guiConfig.getString("home-gui.items.info.name", "&e&lINFO");
    }

    public List<String> getInfoLore() {
        return guiConfig.getStringList("home-gui.items.info.lore");
    }

    public String getHomeSetMaterial() {
        return guiConfig.getString("home-gui.items.home-set.material", "WHITE_BED");
    }

    public String getHomeSetName() {
        return guiConfig.getString("home-gui.items.home-set.name", "&e&lHOME %name%");
    }

    public List<String> getHomeSetLore() {
        return guiConfig.getStringList("home-gui.items.home-set.lore");
    }

    public String getHomeEmptyMaterial() {
        return guiConfig.getString("home-gui.items.home-empty.material", "RED_BED");
    }

    public String getHomeEmptyName() {
        return guiConfig.getString("home-gui.items.home-empty.name", "&e&lHOME SETZEN");
    }

    public List<String> getHomeEmptyLore() {
        return guiConfig.getStringList("home-gui.items.home-empty.lore");
    }

    public String getHomeLockedMaterial() {
        return guiConfig.getString("home-gui.items.home-locked.material", "GRAY_BED");
    }

    public String getHomeLockedName() {
        return guiConfig.getString("home-gui.items.home-locked.name", "&e&lHOME KAUFEN");
    }

    public List<String> getHomeLockedLore() {
        return guiConfig.getStringList("home-gui.items.home-locked.lore");
    }

    public String getPreviousPageMaterial() {
        return guiConfig.getString("home-gui.items.previous-page.material", "ARROW");
    }

    public String getPreviousPageName() {
        return guiConfig.getString("home-gui.items.previous-page.name", "&c&lVORHERIGE SEITE");
    }

    public List<String> getPreviousPageLore() {
        return guiConfig.getStringList("home-gui.items.previous-page.lore");
    }

    public String getNextPageMaterial() {
        return guiConfig.getString("home-gui.items.next-page.material", "ARROW");
    }

    public String getNextPageName() {
        return guiConfig.getString("home-gui.items.next-page.name", "&a&lNÄCHSTE SEITE");
    }

    public List<String> getNextPageLore() {
        return guiConfig.getStringList("home-gui.items.next-page.lore");
    }

    public String getPageInfoMaterial() {
        return guiConfig.getString("home-gui.items.page-info.material", "BOOK");
    }

    public String getPageInfoName() {
        return guiConfig.getString("home-gui.items.page-info.name", "&e&lSEITE %current%/%total%");
    }

    public List<String> getPageInfoLore() {
        return guiConfig.getStringList("home-gui.items.page-info.lore");
    }

    public String getBankMainGUITitle() {
        return guiConfig.getString("bank-gui.main.title", "&6&lCBSYSTEM &8» Deine Bank");
    }

    public int getBankMainGUISize() {
        return guiConfig.getInt("bank-gui.main.size", 45);
    }

    public String getBankPlayerHeadMaterial() {
        return guiConfig.getString("bank-gui.main.items.player-head.material", "PLAYER_HEAD");
    }

    public String getBankPlayerHeadName() {
        return guiConfig.getString("bank-gui.main.items.player-head.name", "&e&lDEIN ACCOUNT");
    }

    public List<String> getBankPlayerHeadLore() {
        return guiConfig.getStringList("bank-gui.main.items.player-head.lore");
    }

    public String getBankDepositMaterial() {
        return guiConfig.getString("bank-gui.main.items.deposit.material", "LIME_DYE");
    }

    public String getBankDepositName() {
        return guiConfig.getString("bank-gui.main.items.deposit.name", "&e&lGELD EINZAHLEN");
    }

    public List<String> getBankDepositLore() {
        return guiConfig.getStringList("bank-gui.main.items.deposit.lore");
    }

    public String getBankWithdrawMaterial() {
        return guiConfig.getString("bank-gui.main.items.withdraw.material", "RED_DYE");
    }

    public String getBankWithdrawName() {
        return guiConfig.getString("bank-gui.main.items.withdraw.name", "&e&lGELD AUSZAHLEN");
    }

    public List<String> getBankWithdrawLore() {
        return guiConfig.getStringList("bank-gui.main.items.withdraw.lore");
    }

    public String getBankInfoMaterial() {
        return guiConfig.getString("bank-gui.main.items.info.material", "PAPER");
    }

    public String getBankInfoName() {
        return guiConfig.getString("bank-gui.main.items.info.name", "&e&lINFO");
    }

    public List<String> getBankInfoLore() {
        return guiConfig.getStringList("bank-gui.main.items.info.lore");
    }

    public String getBankDepositGUITitle() {
        return guiConfig.getString("bank-gui.deposit.title", "&6&lCBSYSTEM &8» Geld einzahlen");
    }

    public int getBankDepositGUISize() {
        return guiConfig.getInt("bank-gui.deposit.size", 36);
    }

    public List<Integer> getBankNuggetAmounts() {
        return guiConfig.getIntegerList("bank-gui.deposit.items.nugget.amounts");
    }

    public List<Integer> getBankIngotAmounts() {
        return guiConfig.getIntegerList("bank-gui.deposit.items.ingot.amounts");
    }

    public List<Integer> getBankBlockAmounts() {
        return guiConfig.getIntegerList("bank-gui.deposit.items.block.amounts");
    }

    public String getBankNuggetMaterial() {
        return guiConfig.getString("bank-gui.deposit.items.nugget.material", "GOLD_NUGGET");
    }

    public String getBankIngotMaterial() {
        return guiConfig.getString("bank-gui.deposit.items.ingot.material", "GOLD_INGOT");
    }

    public String getBankBlockMaterial() {
        return guiConfig.getString("bank-gui.deposit.items.block.material", "GOLD_BLOCK");
    }

    public String getBankAmountName() {
        return guiConfig.getString("bank-gui.deposit.items.nugget.name", "&e&l%amount%");
    }

    public List<String> getBankDepositAmountLore() {
        return guiConfig.getStringList("bank-gui.deposit.items.nugget.lore");
    }

    public String getBankBackMaterial() {
        return guiConfig.getString("bank-gui.deposit.items.back.material", "ARROW");
    }

    public String getBankBackName() {
        return guiConfig.getString("bank-gui.deposit.items.back.name", "&c&lZURÜCK");
    }

    public List<String> getBankBackLore() {
        return guiConfig.getStringList("bank-gui.deposit.items.back.lore");
    }

    public String getBankDepositInfoName() {
        return guiConfig.getString("bank-gui.deposit.items.info.name", "&e&lINFO");
    }

    public List<String> getBankDepositInfoLore() {
        return guiConfig.getStringList("bank-gui.deposit.items.info.lore");
    }

    public String getBankWithdrawGUITitle() {
        return guiConfig.getString("bank-gui.withdraw.title", "&6&lCBSYSTEM &8» Geld auszahlen");
    }

    public int getBankWithdrawGUISize() {
        return guiConfig.getInt("bank-gui.withdraw.size", 36);
    }

    public List<String> getBankWithdrawAmountLore() {
        return guiConfig.getStringList("bank-gui.withdraw.items.nugget.lore");
    }

    public String getBankWithdrawInfoName() {
        return guiConfig.getString("bank-gui.withdraw.items.info.name", "&e&lINFO");
    }

    public List<String> getBankWithdrawInfoLore() {
        return guiConfig.getStringList("bank-gui.withdraw.items.info.lore");
    }

    public void saveGUIConfig() {
        try {
            guiConfig.save(guiFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der gui-de.yml: " + e.getMessage());
        }
    }
}

package de.nicerecord.citybuildsystem;

import de.nicerecord.citybuildsystem.admin.*;
import de.nicerecord.citybuildsystem.bank.*;
import de.nicerecord.citybuildsystem.entity.*;
import de.nicerecord.citybuildsystem.home.*;
import de.nicerecord.citybuildsystem.info.*;
import de.nicerecord.citybuildsystem.messaging.*;
import de.nicerecord.citybuildsystem.nicerecord.Info;
import de.nicerecord.citybuildsystem.server.*;
import de.nicerecord.citybuildsystem.social.*;
import de.nicerecord.citybuildsystem.sudo.*;
import de.nicerecord.citybuildsystem.teleport.*;
import de.nicerecord.citybuildsystem.utility.*;
import de.nicerecord.citybuildsystem.utils.ConfigManager;
import de.nicerecord.citybuildsystem.utils.GUIManager;
import de.nicerecord.citybuildsystem.utils.MessageManager;
import de.nicerecord.citybuildsystem.utils.UpdateChecker;
import de.nicerecord.citybuildsystem.utils.UpdateNotificationListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class CitybuildSystem extends JavaPlugin {

    private HomeManager homeManager;
    private SpawnManager spawnManager;
    private WarpManager warpManager;
    private TpaManager tpaManager;
    private PlaytimeManager playtimeManager;
    private MessageSystem messageSystem;
    private BankManager bankManager;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private GUIManager guiManager;
    private Economy economy;
    private HomeGUI homeGUI;
    private SetHomeCommand setHomeCommand;
    private MaintenanceManager maintenanceManager;
    private UpdateChecker updateChecker;
    private String startprefix = "§6§lCBSYSTEM §8» §7";

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        getServer().getConsoleSender().sendMessage(startprefix + "§7 ");
        getServer().getConsoleSender().sendMessage(startprefix + "§eCitybuildSystem §7by §eNiceRecord§7 §8(§ehttps://modrinth.com/user/nicerecord§8)§8!");
        getServer().getConsoleSender().sendMessage(startprefix + "§7Starting CitybuildSystem...");
        saveDefaultConfig();

        getServer().getConsoleSender().sendMessage(startprefix + "§7Loading configuration...");
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        guiManager = new GUIManager(this);
        homeManager = new HomeManager(this);
        spawnManager = new SpawnManager(this);
        warpManager = new WarpManager(this);
        tpaManager = new TpaManager(this);
        playtimeManager = new PlaytimeManager(this);
        messageSystem = new MessageSystem(this);
        bankManager = new BankManager(this);
        maintenanceManager = new MaintenanceManager(this);

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            getServer().getConsoleSender().sendMessage(startprefix + "§7Vault Economy successfully set up!");
        } else {
            getServer().getConsoleSender().sendMessage(startprefix + "§cVault not found! Economy features will not work.");
        }

        homeGUI = new HomeGUI(this, homeManager, configManager, guiManager, messageManager, economy);
        setHomeCommand = new SetHomeCommand(this, homeManager, messageManager);
        updateChecker = new UpdateChecker(this);

        getServer().getConsoleSender().sendMessage(startprefix + "§7Loading commands and listeners...");
        registerCommands();
        registerListeners();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BankPlaceholderExpansion(this, bankManager).register();
            getServer().getConsoleSender().sendMessage(startprefix + "§7Placeholder successfully registered!");
        } else {
            getServer().getConsoleSender().sendMessage(startprefix + "§cPlaceholderAPI not found! Some features may not work.");
        }
        long duration = System.currentTimeMillis() - startTime;
        getServer().getConsoleSender().sendMessage(startprefix + "§eCitybuildSystem §7activated successfully §8(§7" + duration + "ms§8)§8!");
        getServer().getConsoleSender().sendMessage(startprefix + "§7 ");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.close();
        }
        if (spawnManager != null) {
            spawnManager.close();
        }
        if (warpManager != null) {
            warpManager.close();
        }
        if (tpaManager != null) {
            tpaManager.shutdown();
        }
        if (playtimeManager != null) {
            playtimeManager.close();
        }
        if (messageSystem != null) {
            messageSystem.clearAllConversations();
        }
        if (bankManager != null) {
            bankManager.close();
        }
        if (maintenanceManager != null) {
            maintenanceManager.close();
        }
    }

    private void registerCommands() {
        getCommand("homes").setExecutor(new HomesCommand(this, homeGUI, messageManager));
        getCommand("home").setExecutor(new HomeCommand(this, homeManager, messageManager, configManager));
        getCommand("sethome").setExecutor(setHomeCommand);
        getCommand("delhome").setExecutor(new DelHomeCommand(this, homeManager, messageManager));

        getCommand("gamemode").setExecutor(new GamemodeCommand(this, messageManager));
        getCommand("fly").setExecutor(new FlyCommand(this, messageManager));
        getCommand("heal").setExecutor(new HealCommand(this, messageManager));
        getCommand("feed").setExecutor(new FeedCommand(this, messageManager));
        getCommand("speed").setExecutor(new SpeedCommand(this, messageManager, configManager));
        getCommand("give").setExecutor(new GiveCommand(this, messageManager, configManager));
        getCommand("clear").setExecutor(new ClearCommand(this, messageManager));
        getCommand("vanish").setExecutor(new VanishCommand(this, messageManager));

        getCommand("trash").setExecutor(new TrashCommand(this, messageManager));
        getCommand("head").setExecutor(new HeadCommand(this, messageManager));
        getCommand("enderchest").setExecutor(new EnderchestCommand(this, messageManager));
        getCommand("invsee").setExecutor(new InvseeCommand(this, messageManager));
        getCommand("sign").setExecutor(new SignCommand(this, messageManager));

        getCommand("discord").setExecutor(new DiscordCommand(this, messageManager, configManager));
        getCommand("youtube").setExecutor(new YoutubeCommand(this, messageManager, configManager));

        getCommand("spawn").setExecutor(new SpawnCommand(this, spawnManager, messageManager, configManager));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this, spawnManager, messageManager));
        getCommand("warp").setExecutor(new WarpCommand(this, warpManager, messageManager, configManager));
        getCommand("setwarp").setExecutor(new SetWarpCommand(this, warpManager, messageManager));
        getCommand("delwarp").setExecutor(new DelWarpCommand(this, warpManager, messageManager));
        getCommand("tpo").setExecutor(new TpoCommand(this, messageManager));
        getCommand("tphere").setExecutor(new TpHereCommand(this, messageManager));

        getCommand("tpa").setExecutor(new TpaCommand(this, tpaManager, messageManager));
        getCommand("tpahere").setExecutor(new TpaHereCommand(this, tpaManager, messageManager));
        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this, tpaManager, messageManager, configManager));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this, tpaManager, messageManager, configManager));
        getCommand("tpatoggle").setExecutor(new TpaToggleCommand(this, tpaManager, messageManager));

        getCommand("broadcast").setExecutor(new BroadcastCommand(this, messageManager, configManager));
        getCommand("slowchat").setExecutor(new SlowChatCommand(this, messageManager));
        getCommand("clearchat").setExecutor(new ClearchatCommand(this, messageManager));
        getCommand("cbreload").setExecutor(new ReloadCommand(this, messageManager));
        getCommand("restart").setExecutor(new RestartCommand(this, messageManager));

        getCommand("playtime").setExecutor(new PlaytimeCommand(this, playtimeManager, messageManager));

        getCommand("message").setExecutor(new MessageCommand(this, messageSystem, messageManager));
        getCommand("respond").setExecutor(new RespondCommand(this, messageSystem, messageManager));
        getCommand("teamchat").setExecutor(new TeamChatCommand(this, messageManager));

        getCommand("sudo").setExecutor(new SudoCommand(this, messageManager));

        getCommand("spawnentity").setExecutor(new SpawnEntityCommand(this, messageManager));

        getCommand("bank").setExecutor(new BankCommand(this, bankManager, messageManager, economy));

        getCommand("maintenance").setExecutor(new MaintenanceCommand(this, messageManager, maintenanceManager));
        getCommand("cbupdate").setExecutor(new UpdateCommand(this, messageManager, updateChecker));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(setHomeCommand, this);
        getServer().getPluginManager().registerEvents(new HomeGUIListener(this, homeManager, configManager, guiManager, messageManager, economy, setHomeCommand), this);

        SlowChatCommand slowChatCommand = new SlowChatCommand(this, messageManager);

        MaintenanceCommand maintenanceCommand = new MaintenanceCommand(this, messageManager, maintenanceManager);
        getServer().getPluginManager().registerEvents(maintenanceCommand, this);

        getServer().getPluginManager().registerEvents(maintenanceCommand, this);
        getServer().getPluginManager().registerEvents(slowChatCommand, this);
        getServer().getPluginManager().registerEvents(new PlaytimeListener(this, playtimeManager), this);
        getServer().getPluginManager().registerEvents(new BankGUIListener(this, bankManager, messageManager, economy), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new Info(), this);
        getServer().getPluginManager().registerEvents(new UpdateNotificationListener(this, updateChecker), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        getLogger().info("Vault Economy erfolgreich eingerichtet!");
        return economy != null;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public MessageSystem getMessageSystem() {
        return messageSystem;
    }
    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }
}

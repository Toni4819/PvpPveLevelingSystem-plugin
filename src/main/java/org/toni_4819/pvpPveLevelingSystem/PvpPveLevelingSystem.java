package org.toni_4819.pvpPveLevelingSystem;

import net.luckperms.api.LuckPerms;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.toni_4819.pvpPveLevelingSystem.listeners.XPListener;
import org.toni_4819.pvpPveLevelingSystem.managers.RewardsManager;
import org.toni_4819.pvpPveLevelingSystem.managers.XPManager;
import org.toni_4819.pvpPveLevelingSystem.placeholders.LevelingExpansion;
import org.toni_4819.pvpPveLevelingSystem.managers.CommandManager;
import org.toni_4819.pvpPveLevelingSystem.managers.LangManager;
import org.toni_4819.pvpPveLevelingSystem.managers.StorageManager;

public class PvpPveLevelingSystem extends JavaPlugin {

    private XPManager xpManager;
    private RewardsManager rewardsManager;
    private LuckPerms luckPerms;
    private LangManager langManager;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int pluginId = 29210;
        Metrics metrics = new Metrics(this, pluginId);

        // Load language system
        langManager = new LangManager(this);

        // Initialize storage
        storageManager = new StorageManager(this);
        storageManager.connect();

        // Detect LuckPerms
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider =
                    getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
                getLogger().info("LuckPerms detected, group multipliers enabled.");
            }
        } else {
            getLogger().info("LuckPerms not found, using default multipliers only.");
        }

        xpManager = new XPManager(this, luckPerms);
        rewardsManager = new RewardsManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new XPListener(this, xpManager, rewardsManager), this);

        // Register commands
        getCommand("leveling").setExecutor(new CommandManager(this, xpManager));

        // Register PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelingExpansion(this, xpManager).register();
        }

        getLogger().info("PvpPveLevelingSystem enabled!");
    }

    @Override
    public void onDisable() {
        // Save all online players before shutdown
        getServer().getOnlinePlayers().forEach(player -> {
            xpManager.savePlayer(player);
        });

        if (storageManager != null) {
            storageManager.close();
        }

        getLogger().info("All player data saved.");
        getLogger().info("PvpPveLevelingSystem disabled!");
    }





    public XPManager getXpManager() {
        return xpManager;
    }

    public RewardsManager getRewardsManager() {
        return rewardsManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}

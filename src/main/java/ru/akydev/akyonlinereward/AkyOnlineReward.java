package ru.akydev.akyonlinereward;

import org.bukkit.plugin.java.JavaPlugin;
import ru.akydev.akyonlinereward.managers.ConfigManager;
import ru.akydev.akyonlinereward.managers.RewardManager;
import ru.akydev.akyonlinereward.commands.AkyCommandExecutor;
import ru.akydev.akyonlinereward.listeners.PlayerTracker;

public final class AkyOnlineReward extends JavaPlugin {
    
    private ConfigManager configManager;
    private RewardManager rewardManager;
    
    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.rewardManager = new RewardManager(this);
        
        getServer().getPluginManager().registerEvents(new PlayerTracker(this), this);
        getCommand("akyonline").setExecutor(new AkyCommandExecutor(this));
        
        if (configManager.isCheckOnStartup()) {
            rewardManager.checkCurrentOnline();
        }
    }
    
    @Override
    public void onDisable() {
        if (rewardManager != null) {
            rewardManager.cleanup();
        }
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public RewardManager getRewardManager() {
        return rewardManager;
    }
}

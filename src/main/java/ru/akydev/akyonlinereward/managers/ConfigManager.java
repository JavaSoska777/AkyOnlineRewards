package ru.akydev.akyonlinereward.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.akydev.akyonlinereward.AkyOnlineReward;
import ru.akydev.akyonlinereward.models.MilestoneReward;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    
    private final AkyOnlineReward plugin;
    private FileConfiguration config;
    private Set<String> allowedCommands;
    private Map<Integer, MilestoneReward> milestones;
    private boolean checkOnStartup;
    private int joinDelay;
    private int autoCheckInterval;
    
    public ConfigManager(AkyOnlineReward plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadSettings();
        loadAllowedCommands();
        loadMilestones();
    }
    
    private void loadConfig() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        loadSettings();
        loadAllowedCommands();
        loadMilestones();
    }
    
    private void loadSettings() {
        this.checkOnStartup = config.getBoolean("settings.check-on-startup", true);
        this.joinDelay = config.getInt("settings.join-delay", 20);
        this.autoCheckInterval = config.getInt("settings.auto-check-interval", 60);
    }
    
    private void loadAllowedCommands() {
        List<String> commands = config.getStringList("allowed-commands");
        this.allowedCommands = commands.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
    
    private void loadMilestones() {
        this.milestones = new HashMap<>();
        
        if (!config.contains("milestones")) {
            return;
        }
        
        for (String key : config.getConfigurationSection("milestones").getKeys(false)) {
            try {
                int milestone = Integer.parseInt(key);
                MilestoneReward reward = loadMilestoneReward("milestones." + key);
                if (reward != null) {
                    milestones.put(milestone, reward);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }
    
    private MilestoneReward loadMilestoneReward(String path) {
        List<String> consoleCommands = config.getStringList(path + ".console-commands");
        List<String> playerCommands = config.getStringList(path + ".player-commands");
        List<String> messages = config.getStringList(path + ".messages");
        String actionBar = config.getString(path + ".actionbar", "");
        
        TitleConfig titleConfig = null;
        if (config.getBoolean(path + ".title.enabled", false)) {
            titleConfig = new TitleConfig(
                config.getString(path + ".title.title", ""),
                config.getString(path + ".title.subtitle", ""),
                config.getInt(path + ".title.fade-in", 10),
                config.getInt(path + ".title.stay", 40),
                config.getInt(path + ".title.fade-out", 10)
            );
        }
        
        BossBarConfig bossBarConfig = null;
        if (config.getBoolean(path + ".bossbar.enabled", false)) {
            bossBarConfig = new BossBarConfig(
                config.getString(path + ".bossbar.message", ""),
                config.getString(path + ".bossbar.color", "GREEN"),
                config.getInt(path + ".bossbar.duration", 5)
            );
        }
        
        SoundConfig soundConfig = null;
        if (config.getBoolean(path + ".sound.enabled", false)) {
            soundConfig = new SoundConfig(
                config.getString(path + ".sound.sound", "ENTITY_PLAYER_LEVELUP"),
                config.getDouble(path + ".sound.volume", 1.0),
                config.getDouble(path + ".sound.pitch", 1.0)
            );
        }
        
        return new MilestoneReward(consoleCommands, playerCommands, messages, actionBar, titleConfig, bossBarConfig, soundConfig);
    }
    
    public boolean isCommandAllowed(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        
        String commandName = command.toLowerCase().split(" ")[0];
        return allowedCommands.contains(commandName);
    }
    
    public Map<Integer, MilestoneReward> getMilestones() {
        return new HashMap<>(milestones);
    }
    
    public boolean isCheckOnStartup() {
        return checkOnStartup;
    }
    
    public int getJoinDelay() {
        return joinDelay;
    }
    
    public int getAutoCheckInterval() {
        return autoCheckInterval;
    }
    
    public static class TitleConfig {
        public final String title;
        public final String subtitle;
        public final int fadeIn;
        public final int stay;
        public final int fadeOut;
        
        public TitleConfig(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this.title = title;
            this.subtitle = subtitle;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }
    }
    
    public static class BossBarConfig {
        public final String message;
        public final String color;
        public final int duration;
        
        public BossBarConfig(String message, String color, int duration) {
            this.message = message;
            this.color = color;
            this.duration = duration;
        }
    }
    
    public static class SoundConfig {
        public final String sound;
        public final double volume;
        public final double pitch;
        
        public SoundConfig(String sound, double volume, double pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}

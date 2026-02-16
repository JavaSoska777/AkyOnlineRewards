package ru.akydev.akyonlinereward.models;

import ru.akydev.akyonlinereward.managers.ConfigManager;

import java.util.List;

public class MilestoneReward {
    
    public final List<String> consoleCommands;
    public final List<String> playerCommands;
    public final List<String> messages;
    public final String actionBar;
    public final ConfigManager.TitleConfig titleConfig;
    public final ConfigManager.BossBarConfig bossBarConfig;
    public final ConfigManager.SoundConfig soundConfig;
    
    public MilestoneReward(List<String> consoleCommands, List<String> playerCommands, List<String> messages,
                          String actionBar, ConfigManager.TitleConfig titleConfig,
                          ConfigManager.BossBarConfig bossBarConfig, ConfigManager.SoundConfig soundConfig) {
        this.consoleCommands = consoleCommands;
        this.playerCommands = playerCommands;
        this.messages = messages;
        this.actionBar = actionBar;
        this.titleConfig = titleConfig;
        this.bossBarConfig = bossBarConfig;
        this.soundConfig = soundConfig;
    }
}

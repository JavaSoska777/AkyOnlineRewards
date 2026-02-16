package ru.akydev.akyonlinereward.utils;

import ru.akydev.akyonlinereward.managers.ConfigManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandBlocker {
    
    private static final Set<String> DANGEROUS_COMMANDS = new HashSet<>(Arrays.asList(
        "op", "deop", "stop", "reload", "restart", "plugman", "ban", "kick", "mute", "tempban",
        "tempmute", "unban", "unmute", "clear", "kill", "gamemode", "give", "tp", "tphere",
        "teleport", "summon", "setblock", "fill", "clone", "execute", "data", "function",
        "schedule", "debug", "perf", "save-all", "save-off", "save-on", "whitelist", "ban-ip",
        "unban-ip", "pardon", "pardon-ip", "defaultgamemode", "difficulty", "seed", "time",
        "weather", "worldborder", "title", "tellraw", "team", "scoreboard", "advancement",
        "recipe", "attribute", "bossbar", "effect", "enchant", "xp", "experience", "particle"
    ));
    
    public static boolean isCommandSafe(String command, ConfigManager configManager) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        String commandName = command.toLowerCase().trim().split(" ")[0];
        commandName = commandName.startsWith("/") ? commandName.substring(1) : commandName;
        
        if (DANGEROUS_COMMANDS.contains(commandName)) {
            return false;
        }
        
        if (!configManager.isCommandAllowed(commandName)) {
            return false;
        }
        
        if (commandName.contains("akyonline") || commandName.contains("aon")) {
            return false;
        }
        
        return true;
    }
    
    public static Set<String> getDangerousCommands() {
        return new HashSet<>(DANGEROUS_COMMANDS);
    }
}

package ru.akydev.akyonlinereward.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.NamespacedKey;
import ru.akydev.akyonlinereward.AkyOnlineReward;
import ru.akydev.akyonlinereward.models.MilestoneReward;
import ru.akydev.akyonlinereward.utils.MessageDispatcher;
import ru.akydev.akyonlinereward.utils.CommandBlocker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RewardManager {
    
    private final AkyOnlineReward plugin;
    private final Set<Integer> awardedMilestones = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<Integer, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private BukkitTask autoCheckTask;
    
    public RewardManager(AkyOnlineReward plugin) {
        this.plugin = plugin;
        startAutoCheck();
    }
    
    public void checkCurrentOnline() {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        Map<Integer, MilestoneReward> milestones = plugin.getConfigManager().getMilestones();
        
        for (Map.Entry<Integer, MilestoneReward> entry : milestones.entrySet()) {
            int milestone = entry.getKey();
            MilestoneReward reward = entry.getValue();
            
            if (onlineCount >= milestone && !awardedMilestones.contains(milestone)) {
                executeReward(milestone, reward, onlineCount);
                awardedMilestones.add(milestone);
            }
        }
        
        awardedMilestones.removeIf(milestone -> milestone > onlineCount);
    }
    
    public void testMilestone(int milestone, boolean silent) {
        Map<Integer, MilestoneReward> milestones = plugin.getConfigManager().getMilestones();
        MilestoneReward reward = milestones.get(milestone);
        
        if (reward == null) {
            return;
        }
        
        if (!silent) {
            Player sender = Bukkit.getPlayer("CONSOLE");
            if (sender == null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("akyonline.admin")) {
                        player.sendMessage("§7[§6AkyOnlineReward§7] §eТест награды для " + milestone + " игроков:");
                        showRewardPreview(player, reward, milestone);
                        break;
                    }
                }
            }
        }
    }
    
    private void showRewardPreview(Player player, MilestoneReward reward, int online) {
        if (!reward.consoleCommands.isEmpty()) {
            player.sendMessage("§aКонсольные команды:");
            for (String cmd : reward.consoleCommands) {
                player.sendMessage("§7  " + cmd.replace("%player%", "<игрок>").replace("%online%", String.valueOf(online)));
            }
        }
        
        if (!reward.playerCommands.isEmpty()) {
            player.sendMessage("§aКоманды игроков:");
            for (String cmd : reward.playerCommands) {
                player.sendMessage("§7  " + cmd.replace("%player%", "<игрок>").replace("%online%", String.valueOf(online)));
            }
        }
        
        if (!reward.messages.isEmpty()) {
            player.sendMessage("§aСообщения в чат:");
            for (String msg : reward.messages) {
                player.sendMessage("§7  " + MessageDispatcher.colorize(msg.replace("%online%", String.valueOf(online))));
            }
        }
        
        if (!reward.actionBar.isEmpty()) {
            player.sendMessage("§aActionBar: §7" + MessageDispatcher.colorize(reward.actionBar.replace("%online%", String.valueOf(online))));
        }
        
        if (reward.titleConfig != null) {
            player.sendMessage("§aTitle: §7" + MessageDispatcher.colorize(reward.titleConfig.title.replace("%online%", String.valueOf(online))));
            player.sendMessage("§aSubtitle: §7" + MessageDispatcher.colorize(reward.titleConfig.subtitle.replace("%online%", String.valueOf(online))));
        }
        
        if (reward.bossBarConfig != null) {
            player.sendMessage("§aBossBar: §7" + MessageDispatcher.colorize(reward.bossBarConfig.message.replace("%online%", String.valueOf(online))));
        }
        
        if (reward.soundConfig != null) {
            player.sendMessage("§aЗвук: §7" + reward.soundConfig.sound);
        }
    }
    
    private void executeReward(int milestone, MilestoneReward reward, int onlineCount) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        
        for (String command : reward.consoleCommands) {
            if (CommandBlocker.isCommandSafe(command, plugin.getConfigManager())) {
                String processedCommand = command.replace("%online%", String.valueOf(onlineCount));
                
                if (command.contains("%player%")) {
                    for (Player player : onlinePlayers) {
                        String playerCommand = processedCommand.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand);
                    }
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                }
            }
        }
        
        for (String command : reward.playerCommands) {
            if (CommandBlocker.isCommandSafe(command, plugin.getConfigManager())) {
                String processedCommand = command.replace("%online%", String.valueOf(onlineCount));
                
                for (Player player : onlinePlayers) {
                    if (player.hasPermission("akyonline.receive")) {
                        String playerCommand = processedCommand.replace("%player%", player.getName());
                        player.performCommand(playerCommand);
                    }
                }
            }
        }
        
        for (String message : reward.messages) {
            String processedMessage = MessageDispatcher.colorize(message.replace("%online%", String.valueOf(onlineCount)));
            Bukkit.broadcastMessage(processedMessage);
        }
        
        if (!reward.actionBar.isEmpty()) {
            String actionBarMessage = MessageDispatcher.colorize(reward.actionBar.replace("%online%", String.valueOf(onlineCount)));
            for (Player player : onlinePlayers) {
                MessageDispatcher.sendActionBar(player, actionBarMessage);
            }
        }
        
        if (reward.titleConfig != null) {
            for (Player player : onlinePlayers) {
                MessageDispatcher.sendTitle(player, reward.titleConfig, onlineCount);
            }
        }
        
        if (reward.bossBarConfig != null) {
            sendBossBar(reward.bossBarConfig, onlineCount, onlinePlayers);
        }
        
        if (reward.soundConfig != null) {
            for (Player player : onlinePlayers) {
                MessageDispatcher.playSound(player, reward.soundConfig);
            }
        }
    }
    
    private void sendBossBar(ConfigManager.BossBarConfig bossBarConfig, int onlineCount, Collection<? extends Player> players) {
        BossBar bossBar = activeBossBars.computeIfAbsent(onlineCount, k -> {
            String message = MessageDispatcher.colorize(bossBarConfig.message.replace("%online%", String.valueOf(onlineCount)));
            NamespacedKey key = new NamespacedKey(plugin, "akyonline_" + onlineCount);
            BossBar bar = Bukkit.createBossBar(key, message, MessageDispatcher.getBarColor(bossBarConfig.color), BarStyle.SOLID);
            return bar;
        });
        
        bossBar.removeAll();
        for (Player player : players) {
            bossBar.addPlayer(player);
        }
        bossBar.setVisible(true);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.setVisible(false);
            activeBossBars.remove(onlineCount);
        }, bossBarConfig.duration * 20L);
    }
    
    private void startAutoCheck() {
        int interval = plugin.getConfigManager().getAutoCheckInterval();
        if (interval <= 0) {
            return;
        }
        
        autoCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkCurrentOnline, interval * 20L, interval * 20L);
    }
    
    public void reload() {
        awardedMilestones.clear();
        clearBossBars();
        
        if (autoCheckTask != null) {
            autoCheckTask.cancel();
        }
        
        startAutoCheck();
        checkCurrentOnline();
    }
    
    public void cleanup() {
        if (autoCheckTask != null) {
            autoCheckTask.cancel();
        }
        clearBossBars();
        awardedMilestones.clear();
    }
    
    private void clearBossBars() {
        for (BossBar bossBar : activeBossBars.values()) {
            bossBar.removeAll();
        }
        activeBossBars.clear();
    }
    
    public Set<Integer> getAwardedMilestones() {
        return new HashSet<>(awardedMilestones);
    }
}

package ru.akydev.akyonlinereward.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.akydev.akyonlinereward.AkyOnlineReward;

public class PlayerTracker implements Listener {
    
    private final AkyOnlineReward plugin;
    
    public PlayerTracker(AkyOnlineReward plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int joinDelay = plugin.getConfigManager().getJoinDelay();
        
        if (joinDelay > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event.getPlayer().isOnline()) {
                    plugin.getRewardManager().checkCurrentOnline();
                }
            }, joinDelay);
        } else {
            plugin.getRewardManager().checkCurrentOnline();
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getRewardManager().checkCurrentOnline();
        }, 1L);
    }
}

package ru.akydev.akyonlinereward.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.akydev.akyonlinereward.AkyOnlineReward;
import ru.akydev.akyonlinereward.utils.MessageDispatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AkyCommandExecutor implements CommandExecutor, TabCompleter {
    
    private final AkyOnlineReward plugin;
    
    public AkyCommandExecutor(AkyOnlineReward plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akyonline.admin")) {
            sender.sendMessage("§cУ вас нет прав для выполнения этой команды.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
                
            case "test":
                handleTest(sender, args);
                break;
                
            case "info":
                handleInfo(sender);
                break;
                
            case "list":
                handleList(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadConfig();
        plugin.getRewardManager().reload();
        sender.sendMessage("§7[§6AkyOnlineReward§7] §aКонфигурация успешно перезагружена!");
    }
    
    private void handleTest(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7[§6AkyOnlineReward§7] §cИспользование: /" + args[0] + " test <число> [--silent]");
            return;
        }
        
        boolean silent = args.length > 2 && args[2].equalsIgnoreCase("--silent");
        
        try {
            int milestone = Integer.parseInt(args[1]);
            if (milestone <= 0) {
                sender.sendMessage("§7[§6AkyOnlineReward§7] §cЧисло должно быть положительным!");
                return;
            }
            
            plugin.getRewardManager().testMilestone(milestone, silent);
            
            if (!silent) {
                sender.sendMessage("§7[§6AkyOnlineReward§7] §aТест награды для " + milestone + " игроков выполнен.");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§7[§6AkyOnlineReward§7] §cНекорректное число!");
        }
    }
    
    private void handleInfo(CommandSender sender) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        Set<Integer> awardedMilestones = plugin.getRewardManager().getAwardedMilestones();
        
        sender.sendMessage("§7[§6AkyOnlineReward§7] §eИнформация о плагине:");
        sender.sendMessage("§fТекущий онлайн: §a" + onlineCount);
        sender.sendMessage("§fВыданные награды: §a" + awardedMilestones.size());
        
        if (!awardedMilestones.isEmpty()) {
            sender.sendMessage("§fСписок выданных:");
            for (int milestone : awardedMilestones) {
                sender.sendMessage("§7  - " + milestone + " игроков");
            }
        }
    }
    
    private void handleList(CommandSender sender) {
        sender.sendMessage("§7[§6AkyOnlineReward§7] §eДоступные награды:");
        
        plugin.getConfigManager().getMilestones().keySet().stream()
                .sorted()
                .forEach(milestone -> {
                    boolean awarded = plugin.getRewardManager().getAwardedMilestones().contains(milestone);
                    String status = awarded ? "§a(выдана)" : "§c(не выдана)";
                    sender.sendMessage("§f  " + milestone + " игроков " + status);
                });
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§7[§6AkyOnlineReward§7] §eКоманды:");
        sender.sendMessage("§f/akyonline reload §7- перезагрузить конфиг");
        sender.sendMessage("§f/akyonline test <число> [--silent] §7- тестировать награду");
        sender.sendMessage("§f/akyonline info §7- информация о плагине");
        sender.sendMessage("§f/akyonline list §7- список наград");
        sender.sendMessage("§f/aon <команда> §7- короткий вариант команды");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("akyonline.admin")) {
            return Arrays.asList();
        }
        
        if (args.length == 1) {
            return Arrays.asList("reload", "test", "info", "list").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
            return plugin.getConfigManager().getMilestones().keySet().stream()
                    .map(String::valueOf)
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("test")) {
            return Arrays.asList("--silent").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return Arrays.asList();
    }
}

package ru.akydev.akyonlinereward.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import ru.akydev.akyonlinereward.managers.ConfigManager;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageDispatcher {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINIHEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        
        String colored = message.replace("&", "§");
        
        try {
            Class<?> clazz = Class.forName("net.md_5.bungee.api.ChatColor");
            final Method of = clazz.getMethod("of", String.class);
            
            Matcher hexMatcher = HEX_PATTERN.matcher(colored);
            StringBuffer hexBuffer = new StringBuffer();
            while (hexMatcher.find()) {
                String hex = hexMatcher.group(1);
                try {
                    String replacement = (String) of.invoke(null, "#" + hex);
                    hexMatcher.appendReplacement(hexBuffer, replacement);
                } catch (Exception e) {
                    hexMatcher.appendReplacement(hexBuffer, hexMatcher.group());
                }
            }
            hexMatcher.appendTail(hexBuffer);
            colored = hexBuffer.toString();
            
            Matcher miniHexMatcher = MINIHEX_PATTERN.matcher(colored);
            StringBuffer miniHexBuffer = new StringBuffer();
            while (miniHexMatcher.find()) {
                String hex = miniHexMatcher.group(1);
                try {
                    String replacement = (String) of.invoke(null, "#" + hex);
                    miniHexMatcher.appendReplacement(miniHexBuffer, replacement);
                } catch (Exception e) {
                    miniHexMatcher.appendReplacement(miniHexBuffer, miniHexMatcher.group());
                }
            }
            miniHexMatcher.appendTail(miniHexBuffer);
            colored = miniHexBuffer.toString();
            
        } catch (Exception ignored) {
        }
        
        return colored;
    }
    
    public static void sendActionBar(Player player, String message) {
        String coloredMessage = colorize(message);
        
        try {
            Class<?> craftPlayerClass = player.getClass();
            Method getHandle = craftPlayerClass.getMethod("getHandle");
            Object craftPlayer = getHandle.invoke(player);
            
            Class<?> entityPlayerClass = craftPlayer.getClass();
            Class<?> packetClass = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutChat");
            Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + getServerVersion() + ".ChatMessageType");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent");
            
            Object chatMessageType = chatMessageTypeClass.getField("GAME_INFO").get(null);
            Method a = iChatBaseComponentClass.getMethod("a", String.class);
            Object chatComponent = a.invoke(null, "{\"text\":\"" + coloredMessage + "\"}");
            
            Object packet = packetClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass).newInstance(chatComponent, chatMessageType);
            
            Class<?> playerConnectionClass = entityPlayerClass.getField("playerConnection").get(craftPlayer).getClass();
            Object playerConnection = entityPlayerClass.getField("playerConnection").get(craftPlayer);
            Method sendPacket = playerConnectionClass.getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"));
            sendPacket.invoke(playerConnection, packet);
            
        } catch (Exception e) {
            try {
                player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(coloredMessage)
                );
            } catch (Exception ignored) {
            }
        }
    }
    
    public static void sendTitle(Player player, ConfigManager.TitleConfig titleConfig, int online) {
        String title = colorize(titleConfig.title.replace("%online%", String.valueOf(online)));
        String subtitle = colorize(titleConfig.subtitle.replace("%online%", String.valueOf(online)));
        
        try {
            player.sendTitle(title, subtitle, titleConfig.fadeIn, titleConfig.stay, titleConfig.fadeOut);
        } catch (Exception e) {
            try {
                Class<?> craftPlayerClass = player.getClass();
                Method getHandle = craftPlayerClass.getMethod("getHandle");
                Object craftPlayer = getHandle.invoke(player);
                
                Class<?> entityPlayerClass = craftPlayer.getClass();
                Class<?> packetClass = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutTitle");
                Class<?> enumTitleActionClass = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutTitle$EnumTitleAction");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + getServerVersion() + ".IChatBaseComponent");
                
                Method a = iChatBaseComponentClass.getMethod("a", String.class);
                Object titleComponent = a.invoke(null, "{\"text\":\"" + title + "\"}");
                Object subtitleComponent = a.invoke(null, "{\"text\":\"" + subtitle + "\"}");
                
                Object timesPacket = packetClass.getConstructor(int.class, int.class, int.class).newInstance(titleConfig.fadeIn, titleConfig.stay, titleConfig.fadeOut);
                Object titlePacket = packetClass.getConstructor(enumTitleActionClass, iChatBaseComponentClass).newInstance(enumTitleActionClass.getField("TITLE").get(null), titleComponent);
                Object subtitlePacket = packetClass.getConstructor(enumTitleActionClass, iChatBaseComponentClass).newInstance(enumTitleActionClass.getField("SUBTITLE").get(null), subtitleComponent);
                
                Class<?> playerConnectionClass = entityPlayerClass.getField("playerConnection").get(craftPlayer).getClass();
                Object playerConnection = entityPlayerClass.getField("playerConnection").get(craftPlayer);
                Method sendPacket = playerConnectionClass.getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"));
                
                sendPacket.invoke(playerConnection, timesPacket);
                sendPacket.invoke(playerConnection, titlePacket);
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("AkyOnlineReward"), () -> {
                    try {
                        sendPacket.invoke(playerConnection, subtitlePacket);
                    } catch (Exception ignored) {
                    }
                }, 5L);
                
            } catch (Exception ignored) {
            }
        }
    }
    
    public static void playSound(Player player, ConfigManager.SoundConfig soundConfig) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundConfig.sound);
            player.playSound(player.getLocation(), sound, (float) soundConfig.volume, (float) soundConfig.pitch);
        } catch (Exception e) {
            try {
                player.playSound(player.getLocation(), soundConfig.sound, (float) soundConfig.volume, (float) soundConfig.pitch);
            } catch (Exception ignored) {
            }
        }
    }
    
    public static BarColor getBarColor(String colorName) {
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (Exception e) {
            return BarColor.GREEN;
        }
    }
    
    private static String getServerVersion() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        return version.substring(version.lastIndexOf('.') + 1);
    }
}

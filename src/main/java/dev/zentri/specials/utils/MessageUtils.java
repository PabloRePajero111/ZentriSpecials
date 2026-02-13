package dev.zentri.specials.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtils {
    
    private static FileConfiguration config;
    
    public static void setConfig(FileConfiguration cfg) {
        config = cfg;
    }
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static String getMessage(String path) {
        if (config == null) return colorize("&cConfiguration not loaded");
        String message = config.getString("messages." + path, "");
        return colorize(message);
    }
    
    public static String getPrefix() {
        return getMessage("prefix");
    }
    
    public static String formatMessage(String path, Object... replacements) {
        String message = getMessage(path);
        if (message.isEmpty()) return "";
        
        // Handle replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String value = String.valueOf(replacements[i + 1]);
                message = message.replace(placeholder, value);
            }
        }
        
        return getPrefix() + message;
    }
    
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return secs > 0 ? minutes + "m " + secs + "s" : minutes + "m";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        }
    }
}

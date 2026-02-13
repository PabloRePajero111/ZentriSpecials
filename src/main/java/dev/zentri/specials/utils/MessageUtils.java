package dev.zentri.specials.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling plugin messages with color codes and placeholders.
 */
public class MessageUtils {

    /**
     * Colorize a string by replacing '&' color codes with proper Minecraft color codes.
     *
     * @param message The message to colorize
     * @return Colorized message
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Send a message to a player with placeholders replaced.
     *
     * @param player       The player to send the message to
     * @param message      The message to send
     * @param placeholders Placeholders to replace in the message
     */
    public static void sendMessage(Player player, String message, Map<String, String> placeholders) {
        String finalMessage = replacePlaceholders(message, placeholders);
        player.sendMessage(colorize(finalMessage));
    }

    /**
     * Send a message to a player.
     *
     * @param player  The player to send the message to
     * @param message The message to send
     */
    public static void sendMessage(Player player, String message) {
        sendMessage(player, message, new HashMap<>());
    }

    /**
     * Replace placeholders in a message.
     *
     * @param message      The message
     * @param placeholders Placeholders to replace
     * @return Message with placeholders replaced
     */
    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) {
            return "";
        }
        
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * Get a message from config with prefix applied.
     *
     * @param config The configuration
     * @param path   The path to the message
     * @return The message with prefix applied
     */
    public static String getMessage(FileConfiguration config, String path) {
        String prefix = config.getString("prefix", "&8[&bZentri&8] &7");
        String message = config.getString(path, "");
        return message.replace("{prefix}", prefix);
    }

    /**
     * Format a time in seconds to a human-readable string.
     *
     * @param seconds The time in seconds
     * @return Formatted time string
     */
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + "h " + remainingMinutes + "m";
        }
    }
}

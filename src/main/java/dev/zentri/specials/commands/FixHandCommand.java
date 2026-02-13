package dev.zentri.specials.commands;

import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command for repairing the item in player's hand.
 */
public class FixHandCommand implements CommandExecutor {
    
    private final FileConfiguration config;
    private final Map<UUID, Long> cooldowns;
    
    public FixHandCommand(FileConfiguration config) {
        this.config = config;
        this.cooldowns = new ConcurrentHashMap<>();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!config.getBoolean("fix.enabled", true)) {
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        // Check permission
        if (!player.hasPermission(config.getString("fix.permission", "zentrispecials.fix"))) {
            String message = MessageUtils.getMessage(config, "fix.messages.no-permission");
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Check cooldown
        long cooldownSeconds = getCooldownForPlayer(player);
        if (cooldownSeconds > 0) {
            UUID playerId = player.getUniqueId();
            long lastUsed = cooldowns.getOrDefault(playerId, 0L);
            long currentTime = System.currentTimeMillis() / 1000;
            long timePassed = currentTime - lastUsed;
            
            if (timePassed < cooldownSeconds) {
                long timeRemaining = cooldownSeconds - timePassed;
                String message = MessageUtils.getMessage(config, "fix.messages.cooldown");
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(timeRemaining));
                MessageUtils.sendMessage(player, message, placeholders);
                return true;
            }
        }
        
        // Get item in hand
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            String message = MessageUtils.getMessage(config, "fix.messages.no-item");
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Check if item is damageable
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            String message = MessageUtils.getMessage(config, "fix.messages.no-item");
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        // Repair the item
        Damageable damageable = (Damageable) meta;
        if (damageable.getDamage() == 0) {
            String message = MessageUtils.getMessage(config, "fix.messages.no-item");
            MessageUtils.sendMessage(player, message);
            return true;
        }
        
        damageable.setDamage(0);
        item.setItemMeta(meta);
        
        // Set cooldown
        if (cooldownSeconds > 0) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
        }
        
        // Send success message
        String message = MessageUtils.getMessage(config, "fix.messages.repaired-hand");
        MessageUtils.sendMessage(player, message);
        
        return true;
    }
    
    /**
     * Get the cooldown for a player based on their permissions.
     */
    private long getCooldownForPlayer(Player player) {
        // Check permissions from highest to lowest
        String[] permissionOrder = {
            "zentrispecials.fix.staff",
            "zentrispecials.fix.mvp",
            "zentrispecials.fix.vip",
            "zentrispecials.fix.default"
        };
        
        for (String permission : permissionOrder) {
            if (player.hasPermission(permission)) {
                return config.getLong("fix.cooldowns." + permission, 120);
            }
        }
        
        return config.getLong("fix.cooldowns.zentrispecials.fix.default", 120);
    }
}

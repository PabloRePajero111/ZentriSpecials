package dev.zentri.specials.listeners;

import dev.zentri.specials.managers.CooldownManager;
import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Listener for applying item cooldowns by region.
 */
public class ItemCooldownListener implements Listener {
    
    private final CooldownManager cooldownManager;
    private final FileConfiguration config;
    
    public ItemCooldownListener(CooldownManager cooldownManager, FileConfiguration config) {
        this.cooldownManager = cooldownManager;
        this.config = config;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!config.getBoolean("item-cooldowns.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        Material material = item.getType();
        Set<String> regions = RegionUtils.getRegionsAtLocation(player);
        
        if (regions.isEmpty()) {
            return;
        }
        
        // Check each region for cooldown configuration
        for (String region : regions) {
            int cooldownTicks = cooldownManager.getCooldown(region, material);
            
            if (cooldownTicks > 0) {
                // Apply cooldown
                cooldownManager.applyCooldown(player, material, cooldownTicks);
                
                // Send message if configured
                String message = config.getString("item-cooldowns.messages.cooldown-applied");
                if (message != null && !message.isEmpty()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(cooldownTicks / 20));
                    placeholders.put("item", material.name());
                    placeholders.put("region", region);
                    MessageUtils.sendMessage(player, MessageUtils.getMessage(config, "item-cooldowns.messages.cooldown-applied"), placeholders);
                }
                
                // Only apply cooldown from first matching region
                break;
            }
        }
    }
}

package dev.zentri.specials.listeners;

import dev.zentri.specials.managers.MaxUsesManager;
import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Listener for max uses of items per region.
 */
public class ItemMaxUsesListener implements Listener {
    
    private final MaxUsesManager maxUsesManager;
    private final FileConfiguration config;
    private final Map<UUID, Set<String>> playerRegions;
    
    public ItemMaxUsesListener(MaxUsesManager maxUsesManager, FileConfiguration config) {
        this.maxUsesManager = maxUsesManager;
        this.config = config;
        this.playerRegions = new ConcurrentHashMap<>();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!config.getBoolean("item-max-uses.enabled", true)) {
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
        
        // Check each region for max uses configuration
        for (String region : regions) {
            MaxUsesManager.MaxUseConfig maxUseConfig = maxUsesManager.getMaxUseConfig(region, material);
            
            if (maxUseConfig != null) {
                int currentUsage = maxUsesManager.getUsage(player, region, material);
                
                if (currentUsage >= maxUseConfig.maxUses) {
                    // Max uses reached, cancel event
                    event.setCancelled(true);
                    
                    // Send message
                    String message = MessageUtils.getMessage(config, "item-max-uses.messages.max-uses-reached");
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("item", material.name());
                    placeholders.put("region", region);
                    placeholders.put("max", String.valueOf(maxUseConfig.maxUses));
                    MessageUtils.sendMessage(player, message, placeholders);
                    return;
                }
                
                // Increment usage
                int newUsage = maxUsesManager.incrementUsage(player, region, material);
                
                // Send remaining uses message if configured
                String usesMsg = config.getString("item-max-uses.messages.uses-remaining");
                if (usesMsg != null && !usesMsg.isEmpty()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("item", material.name());
                    placeholders.put("region", region);
                    placeholders.put("uses", String.valueOf(newUsage));
                    placeholders.put("max", String.valueOf(maxUseConfig.maxUses));
                    MessageUtils.sendMessage(player, MessageUtils.getMessage(config, "item-max-uses.messages.uses-remaining"), placeholders);
                }
                
                // Only apply from first matching region
                break;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!config.getBoolean("item-max-uses.enabled", true)) {
            return;
        }
        
        // Optimize: only check when block changes
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Set<String> currentRegions = RegionUtils.getRegionsAtLocation(player);
        Set<String> previousRegions = playerRegions.getOrDefault(playerId, Set.of());
        
        // Find regions the player left
        for (String region : previousRegions) {
            if (!currentRegions.contains(region)) {
                // Player left this region
                if (maxUsesManager.hasResetOnLeave(region)) {
                    maxUsesManager.resetRegionUsage(player, region);
                }
            }
        }
        
        // Update player's current regions
        playerRegions.put(playerId, currentRegions);
    }
}

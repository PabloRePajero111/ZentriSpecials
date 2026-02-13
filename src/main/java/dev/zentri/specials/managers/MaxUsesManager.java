package dev.zentri.specials.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for tracking max uses of items per region.
 */
public class MaxUsesManager {
    
    private final Plugin plugin;
    private final Map<String, Map<Material, MaxUseConfig>> regionMaxUses;
    private final Map<UUID, Map<String, Map<Material, Integer>>> playerUsages;
    private final Map<UUID, Map<String, Map<Material, Long>>> playerResetTimes;
    
    public MaxUsesManager(Plugin plugin) {
        this.plugin = plugin;
        this.regionMaxUses = new HashMap<>();
        this.playerUsages = new ConcurrentHashMap<>();
        this.playerResetTimes = new ConcurrentHashMap<>();
    }
    
    /**
     * Configuration for max uses.
     */
    public static class MaxUseConfig {
        public final int maxUses;
        public final boolean resetOnLeave;
        public final int resetTime; // seconds
        
        public MaxUseConfig(int maxUses, boolean resetOnLeave, int resetTime) {
            this.maxUses = maxUses;
            this.resetOnLeave = resetOnLeave;
            this.resetTime = resetTime;
        }
    }
    
    /**
     * Load max uses configuration.
     *
     * @param regionMaterialMap Map of region -> material -> config
     */
    public void loadMaxUses(Map<String, Map<Material, MaxUseConfig>> regionMaterialMap) {
        regionMaxUses.clear();
        regionMaxUses.putAll(regionMaterialMap);
    }
    
    /**
     * Get max use config for a material in a region.
     *
     * @param region   The region name
     * @param material The material
     * @return Max use config or null
     */
    public MaxUseConfig getMaxUseConfig(String region, Material material) {
        Map<Material, MaxUseConfig> materialConfigs = regionMaxUses.get(region);
        if (materialConfigs == null) {
            return null;
        }
        return materialConfigs.get(material);
    }
    
    /**
     * Increment usage count for a player.
     *
     * @param player   The player
     * @param region   The region
     * @param material The material
     * @return New usage count
     */
    public int incrementUsage(Player player, String region, Material material) {
        UUID playerId = player.getUniqueId();
        playerUsages.putIfAbsent(playerId, new ConcurrentHashMap<>());
        playerUsages.get(playerId).putIfAbsent(region, new ConcurrentHashMap<>());
        
        Map<Material, Integer> regionUsages = playerUsages.get(playerId).get(region);
        int currentUsage = regionUsages.getOrDefault(material, 0);
        int newUsage = currentUsage + 1;
        regionUsages.put(material, newUsage);
        
        // Schedule reset if configured
        MaxUseConfig config = getMaxUseConfig(region, material);
        if (config != null && config.resetTime > 0) {
            scheduleReset(player, region, material, config.resetTime);
        }
        
        return newUsage;
    }
    
    /**
     * Get current usage count.
     *
     * @param player   The player
     * @param region   The region
     * @param material The material
     * @return Usage count
     */
    public int getUsage(Player player, String region, Material material) {
        UUID playerId = player.getUniqueId();
        if (!playerUsages.containsKey(playerId)) {
            return 0;
        }
        Map<String, Map<Material, Integer>> regions = playerUsages.get(playerId);
        if (!regions.containsKey(region)) {
            return 0;
        }
        return regions.get(region).getOrDefault(material, 0);
    }
    
    /**
     * Reset usage for a player in a region.
     *
     * @param player The player
     * @param region The region
     */
    public void resetRegionUsage(Player player, String region) {
        UUID playerId = player.getUniqueId();
        if (playerUsages.containsKey(playerId)) {
            playerUsages.get(playerId).remove(region);
        }
        if (playerResetTimes.containsKey(playerId)) {
            playerResetTimes.get(playerId).remove(region);
        }
    }
    
    /**
     * Schedule a reset for a specific material usage.
     *
     * @param player     The player
     * @param region     The region
     * @param material   The material
     * @param resetTime  Reset time in seconds
     */
    private void scheduleReset(Player player, String region, Material material, int resetTime) {
        UUID playerId = player.getUniqueId();
        long resetTimestamp = System.currentTimeMillis() + (resetTime * 1000L);
        
        playerResetTimes.putIfAbsent(playerId, new ConcurrentHashMap<>());
        playerResetTimes.get(playerId).putIfAbsent(region, new ConcurrentHashMap<>());
        playerResetTimes.get(playerId).get(region).put(material, resetTimestamp);
        
        // Use async scheduler for time-based resets (Folia-compatible)
        Bukkit.getAsyncScheduler().runDelayed(plugin, task -> {
            if (playerUsages.containsKey(playerId) && 
                playerUsages.get(playerId).containsKey(region)) {
                playerUsages.get(playerId).get(region).remove(material);
            }
            if (playerResetTimes.containsKey(playerId) && 
                playerResetTimes.get(playerId).containsKey(region)) {
                playerResetTimes.get(playerId).get(region).remove(material);
            }
        }, resetTime, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    /**
     * Check if a region has max uses configured.
     *
     * @param region The region name
     * @return True if region has max uses configured
     */
    public boolean hasRegionMaxUses(String region) {
        return regionMaxUses.containsKey(region);
    }
    
    /**
     * Get all materials configured for a region.
     *
     * @param region The region name
     * @return Map of materials to configs, or null if region not found
     */
    public Map<Material, MaxUseConfig> getRegionMaterials(String region) {
        return regionMaxUses.get(region);
    }
    
    /**
     * Check if any material in a region has reset-on-leave enabled.
     *
     * @param region The region name
     * @return True if any material has reset-on-leave
     */
    public boolean hasResetOnLeave(String region) {
        Map<Material, MaxUseConfig> materials = regionMaxUses.get(region);
        if (materials == null) {
            return false;
        }
        return materials.values().stream().anyMatch(config -> config.resetOnLeave);
    }
    
    /**
     * Clear all data.
     */
    public void clear() {
        regionMaxUses.clear();
        playerUsages.clear();
        playerResetTimes.clear();
    }
}

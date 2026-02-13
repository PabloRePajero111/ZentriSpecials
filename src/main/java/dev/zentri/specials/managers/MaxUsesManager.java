package dev.zentri.specials.managers;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaxUsesManager {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private boolean resetOnRegionLeave;
    private int resetInterval;
    private final Map<String, Map<Material, Integer>> regionMaxUses = new HashMap<>();
    private final Map<UUID, Map<String, Map<Material, Integer>>> playerUses = new HashMap<>();
    private final Map<UUID, String> lastRegion = new HashMap<>();
    
    public MaxUsesManager(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
        startResetTask();
    }
    
    public void loadConfig() {
        regionMaxUses.clear();
        this.enabled = plugin.getConfig().getBoolean("max-uses.enabled", true);
        this.resetOnRegionLeave = plugin.getConfig().getBoolean("max-uses.reset-on-region-leave", true);
        this.resetInterval = plugin.getConfig().getInt("max-uses.reset-interval", 3600);
        
        ConfigurationSection regions = plugin.getConfig().getConfigurationSection("max-uses.regions");
        if (regions != null) {
            for (String regionName : regions.getKeys(false)) {
                ConfigurationSection items = regions.getConfigurationSection(regionName);
                if (items != null) {
                    Map<Material, Integer> maxUses = new HashMap<>();
                    for (String materialName : items.getKeys(false)) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            int uses = items.getInt(materialName);
                            maxUses.put(material, uses);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material in max-uses config: " + materialName);
                        }
                    }
                    regionMaxUses.put(regionName.toLowerCase(), maxUses);
                }
            }
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getMaxUses(Location location, Material material) {
        for (Map.Entry<String, Map<Material, Integer>> entry : regionMaxUses.entrySet()) {
            if (RegionUtils.isInRegion(location, entry.getKey())) {
                Integer max = entry.getValue().get(material);
                if (max != null) {
                    return max;
                }
            }
        }
        return -1; // No limit
    }
    
    public boolean canUse(Player player, Location location, Material material) {
        int maxUses = getMaxUses(location, material);
        if (maxUses < 0) {
            return true; // No limit
        }
        
        String regionKey = getRegionKey(location);
        if (regionKey == null) {
            return true;
        }
        
        int currentUses = getCurrentUses(player, regionKey, material);
        return currentUses < maxUses;
    }
    
    public void incrementUse(Player player, Location location, Material material) {
        String regionKey = getRegionKey(location);
        if (regionKey == null) {
            return;
        }
        
        playerUses.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .computeIfAbsent(regionKey, k -> new HashMap<>())
                .merge(material, 1, Integer::sum);
        
        lastRegion.put(player.getUniqueId(), regionKey);
    }
    
    public int getCurrentUses(Player player, String regionKey, Material material) {
        Map<String, Map<Material, Integer>> regions = playerUses.get(player.getUniqueId());
        if (regions == null) {
            return 0;
        }
        
        Map<Material, Integer> uses = regions.get(regionKey);
        if (uses == null) {
            return 0;
        }
        
        return uses.getOrDefault(material, 0);
    }
    
    private String getRegionKey(Location location) {
        for (String regionName : regionMaxUses.keySet()) {
            if (RegionUtils.isInRegion(location, regionName)) {
                return regionName;
            }
        }
        return null;
    }
    
    public void checkRegionChange(Player player) {
        if (!resetOnRegionLeave) {
            return;
        }
        
        String currentRegion = getRegionKey(player.getLocation());
        String previous = lastRegion.get(player.getUniqueId());
        
        if (previous != null && !previous.equals(currentRegion)) {
            // Player left region, reset uses
            Map<String, Map<Material, Integer>> regions = playerUses.get(player.getUniqueId());
            if (regions != null) {
                regions.remove(previous);
            }
            lastRegion.remove(player.getUniqueId());
        }
    }
    
    private void startResetTask() {
        if (resetInterval <= 0) {
            return;
        }
        
        // Use global region scheduler for time-based resets (Folia-compatible)
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            playerUses.clear();
        }, resetInterval * 20L, resetInterval * 20L); // Convert seconds to ticks
    }
}

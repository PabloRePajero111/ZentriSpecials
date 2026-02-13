package dev.zentri.specials.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager for item cooldowns by region.
 */
public class CooldownManager {
    
    private final Map<String, Map<Material, Integer>> regionCooldowns;
    
    public CooldownManager() {
        this.regionCooldowns = new HashMap<>();
    }
    
    /**
     * Load cooldowns from configuration.
     *
     * @param regionMaterialMap Map of region -> material -> cooldown ticks
     */
    public void loadCooldowns(Map<String, Map<Material, Integer>> regionMaterialMap) {
        regionCooldowns.clear();
        regionCooldowns.putAll(regionMaterialMap);
    }
    
    /**
     * Get the cooldown for a material in a region.
     *
     * @param region   The region name
     * @param material The material
     * @return Cooldown in ticks, or 0 if not configured
     */
    public int getCooldown(String region, Material material) {
        Map<Material, Integer> materialCooldowns = regionCooldowns.get(region);
        if (materialCooldowns == null) {
            return 0;
        }
        return materialCooldowns.getOrDefault(material, 0);
    }
    
    /**
     * Apply cooldown to a player for a material.
     *
     * @param player   The player
     * @param material The material
     * @param ticks    Cooldown in ticks
     */
    public void applyCooldown(Player player, Material material, int ticks) {
        player.setCooldown(material, ticks);
    }
    
    /**
     * Check if cooldowns are configured for a region.
     *
     * @param region The region name
     * @return True if region has cooldowns configured
     */
    public boolean hasRegionCooldowns(String region) {
        return regionCooldowns.containsKey(region);
    }
    
    /**
     * Clear all cooldown configurations.
     */
    public void clear() {
        regionCooldowns.clear();
    }
}

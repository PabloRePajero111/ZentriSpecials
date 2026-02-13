package dev.zentri.specials.managers;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private final Map<String, Map<Material, Integer>> regionCooldowns = new HashMap<>();
    private final Map<UUID, Map<Material, Long>> playerCooldowns = new HashMap<>();
    
    public CooldownManager(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        regionCooldowns.clear();
        this.enabled = plugin.getConfig().getBoolean("item-cooldown.enabled", true);
        
        ConfigurationSection regions = plugin.getConfig().getConfigurationSection("item-cooldown.regions");
        if (regions != null) {
            for (String regionName : regions.getKeys(false)) {
                ConfigurationSection items = regions.getConfigurationSection(regionName);
                if (items != null) {
                    Map<Material, Integer> cooldowns = new HashMap<>();
                    for (String materialName : items.getKeys(false)) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            int ticks = items.getInt(materialName);
                            cooldowns.put(material, ticks);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material in cooldown config: " + materialName);
                        }
                    }
                    regionCooldowns.put(regionName.toLowerCase(), cooldowns);
                }
            }
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getCooldownTicks(Location location, Material material) {
        for (Map.Entry<String, Map<Material, Integer>> entry : regionCooldowns.entrySet()) {
            if (RegionUtils.isInRegion(location, entry.getKey())) {
                Integer cooldown = entry.getValue().get(material);
                if (cooldown != null) {
                    return cooldown;
                }
            }
        }
        return 0;
    }
    
    public void applyCooldown(Player player, Material material, int ticks) {
        playerCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(material, System.currentTimeMillis() + (ticks * 50L)); // Convert ticks to milliseconds
    }
    
    public boolean isOnCooldown(Player player, Material material) {
        Map<Material, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return false;
        }
        
        Long endTime = cooldowns.get(material);
        if (endTime == null) {
            return false;
        }
        
        return System.currentTimeMillis() < endTime;
    }
    
    public long getRemainingCooldown(Player player, Material material) {
        Map<Material, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null) {
            return 0;
        }
        
        Long endTime = cooldowns.get(material);
        if (endTime == null) {
            return 0;
        }
        
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 50 : 0; // Convert to ticks
    }
}

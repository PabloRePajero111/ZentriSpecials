package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class BarrierEntityKillListener implements Listener {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private int checkInterval;
    private List<String> regions;
    private List<EntityType> entityTypes;
    
    public BarrierEntityKillListener(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
        startTask();
    }
    
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("barrier-kill.enabled", true);
        this.checkInterval = config.getInt("barrier-kill.check-interval", 100);
        this.regions = config.getStringList("barrier-kill.regions");
        
        List<String> typeNames = config.getStringList("barrier-kill.entity-types");
        if (typeNames == null || typeNames.isEmpty()) {
            this.entityTypes = null; // Kill all non-players
        } else {
            this.entityTypes = typeNames.stream()
                    .map(name -> {
                        try {
                            return EntityType.valueOf(name.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid entity type in barrier-kill config: " + name);
                            return null;
                        }
                    })
                    .filter(type -> type != null)
                    .toList();
        }
    }
    
    private void startTask() {
        if (!enabled) {
            return;
        }
        
        // Use global region scheduler for periodic checks (Folia-compatible)
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            if (!enabled) {
                return;
            }
            
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Player) {
                        continue; // Never kill players
                    }
                    
                    if (!(entity instanceof LivingEntity)) {
                        continue;
                    }
                    
                    // Check entity type filter
                    if (entityTypes != null && !entityTypes.isEmpty()) {
                        if (!entityTypes.contains(entity.getType())) {
                            continue;
                        }
                    }
                    
                    // Check if in configured regions
                    if (regions != null && !regions.isEmpty()) {
                        if (!RegionUtils.isInAnyRegion(entity.getLocation(), regions)) {
                            continue;
                        }
                    }
                    
                    // Check if standing on barrier
                    Block blockBelow = entity.getLocation().subtract(0, 1, 0).getBlock();
                    if (blockBelow.getType() == Material.BARRIER) {
                        // Kill entity using entity scheduler (Folia-compatible)
                        entity.getScheduler().run(plugin, scheduledTask -> {
                            if (entity.isValid()) {
                                entity.remove();
                            }
                        }, null);
                    }
                }
            }
        }, 1, checkInterval);
    }
}

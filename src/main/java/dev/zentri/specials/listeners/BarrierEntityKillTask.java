package dev.zentri.specials.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Task for killing entities standing on barrier blocks in configured regions.
 */
public class BarrierEntityKillTask {
    
    private final Plugin plugin;
    private final FileConfiguration config;
    private final Set<EntityType> excludedEntities;
    
    public BarrierEntityKillTask(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        this.excludedEntities = new HashSet<>();
        loadExcludedEntities();
    }
    
    /**
     * Load excluded entities from config.
     */
    private void loadExcludedEntities() {
        excludedEntities.clear();
        
        List<String> excluded = config.getStringList("barrier-entity-kill.excluded-entities");
        for (String entityName : excluded) {
            try {
                EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                excludedEntities.add(entityType);
            } catch (IllegalArgumentException e) {
                // Invalid entity type, skip
            }
        }
    }
    
    /**
     * Start the periodic task.
     */
    public void start() {
        if (!config.getBoolean("barrier-entity-kill.enabled", true)) {
            return;
        }
        
        int checkInterval = config.getInt("barrier-entity-kill.check-interval", 100);
        List<String> regions = config.getStringList("barrier-entity-kill.regions");
        
        if (regions.isEmpty()) {
            return;
        }
        
        // Schedule task for each world using Folia region scheduler
        for (World world : Bukkit.getWorlds()) {
            scheduleTaskForWorld(world, regions, checkInterval);
        }
    }
    
    /**
     * Schedule the task for a specific world.
     */
    private void scheduleTaskForWorld(World world, List<String> regionNames, int checkInterval) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        
        if (regionManager == null) {
            return;
        }
        
        // Get all configured regions in this world
        for (String regionName : regionNames) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region == null) {
                continue;
            }
            
            // Get a location within the region for the scheduler
            com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint();
            Location regionLocation = new Location(world, min.getX(), min.getY(), min.getZ());
            
            // Use region scheduler for the task (Folia-compatible)
            Bukkit.getRegionScheduler().runAtFixedRate(plugin, regionLocation, task -> {
                checkAndKillEntities(world, region);
            }, 1, checkInterval);
        }
    }
    
    /**
     * Check and kill entities on barriers in a region.
     */
    private void checkAndKillEntities(World world, ProtectedRegion region) {
        com.sk89q.worldedit.math.BlockVector3 min = region.getMinimumPoint();
        com.sk89q.worldedit.math.BlockVector3 max = region.getMaximumPoint();
        
        // Calculate center and size for getNearbyEntities
        double centerX = (min.getX() + max.getX()) / 2.0;
        double centerY = (min.getY() + max.getY()) / 2.0;
        double centerZ = (min.getZ() + max.getZ()) / 2.0;
        Location center = new Location(world, centerX, centerY, centerZ);
        
        double halfSizeX = (max.getX() - min.getX()) / 2.0 + 1;
        double halfSizeY = (max.getY() - min.getY()) / 2.0 + 1;
        double halfSizeZ = (max.getZ() - min.getZ()) / 2.0 + 1;
        
        // Get nearby entities within the region's bounding box
        for (Entity entity : world.getNearbyEntities(center, halfSizeX, halfSizeY, halfSizeZ)) {
            // Skip players
            if (entity instanceof Player) {
                continue;
            }
            
            // Skip excluded entities
            if (excludedEntities.contains(entity.getType())) {
                continue;
            }
            
            Location entityLoc = entity.getLocation();
            
            // Double-check if entity is within region bounds (getNearbyEntities uses spherical bounds)
            if (entityLoc.getBlockX() < min.getX() || entityLoc.getBlockX() > max.getX() ||
                entityLoc.getBlockY() < min.getY() || entityLoc.getBlockY() > max.getY() ||
                entityLoc.getBlockZ() < min.getZ() || entityLoc.getBlockZ() > max.getZ()) {
                continue;
            }
            
            // Check if entity is standing on a barrier block
            Location blockBelow = entityLoc.clone().subtract(0, 1, 0);
            if (blockBelow.getBlock().getType() == Material.BARRIER) {
                // Kill the entity using its own scheduler (Folia-compatible)
                entity.getScheduler().run(plugin, scheduledTask -> {
                    entity.remove();
                }, null);
            }
        }
    }
    
    /**
     * Reload the task configuration.
     */
    public void reload() {
        loadExcludedEntities();
        // Note: In a production environment, you would want to cancel existing tasks
        // and restart them. For simplicity, we just reload the excluded entities.
    }
}

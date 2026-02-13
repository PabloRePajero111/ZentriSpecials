package dev.zentri.specials.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for WorldGuard region operations.
 */
public class RegionUtils {

    /**
     * Get all region names that contain the given player's location.
     *
     * @param player The player
     * @return Set of region names
     */
    public static Set<String> getRegionsAtLocation(Player player) {
        Set<String> regions = new HashSet<>();
        
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));
            
            if (regionManager == null) {
                return regions;
            }
            
            Location location = BukkitAdapter.adapt(player.getLocation());
            ApplicableRegionSet set = regionManager.getApplicableRegions(location.toVector().toBlockPoint());
            
            for (ProtectedRegion region : set) {
                regions.add(region.getId());
            }
        } catch (Exception e) {
            // Silently fail if WorldGuard is not available
        }
        
        return regions;
    }

    /**
     * Check if a player is in a specific region.
     *
     * @param player     The player
     * @param regionName The region name
     * @return True if player is in the region
     */
    public static boolean isInRegion(Player player, String regionName) {
        return getRegionsAtLocation(player).contains(regionName);
    }

    /**
     * Get a specific region by name.
     *
     * @param player     The player (for world context)
     * @param regionName The region name
     * @return The protected region, or null if not found
     */
    public static ProtectedRegion getRegion(Player player, String regionName) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));
            
            if (regionManager == null) {
                return null;
            }
            
            return regionManager.getRegion(regionName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get a region by name from a specific world.
     *
     * @param worldName  The world name
     * @param regionName The region name
     * @return The protected region, or null if not found
     */
    public static ProtectedRegion getRegion(String worldName, String regionName) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(org.bukkit.Bukkit.getWorld(worldName)));
            
            if (regionManager == null) {
                return null;
            }
            
            return regionManager.getRegion(regionName);
        } catch (Exception e) {
            return null;
        }
    }
}

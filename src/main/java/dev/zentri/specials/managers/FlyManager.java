package dev.zentri.specials.managers;

import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.PSUtils;
import dev.zentri.specials.utils.RegionUtils;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for fly permissions in spawn and ProtectionStones regions.
 */
public class FlyManager {
    
    private final Plugin plugin;
    private final Map<UUID, Boolean> flyingPlayers;
    private final String spawnRegion;
    private final String permission;
    
    public FlyManager(Plugin plugin, String spawnRegion, String permission) {
        this.plugin = plugin;
        this.flyingPlayers = new ConcurrentHashMap<>();
        this.spawnRegion = spawnRegion;
        this.permission = permission;
    }
    
    /**
     * Check if a player should be allowed to fly at their current location.
     *
     * @param player The player
     * @return True if player can fly
     */
    public boolean canFlyAtLocation(Player player) {
        if (!player.hasPermission(permission)) {
            return false;
        }
        
        // Check if in spawn region
        if (spawnRegion != null && !spawnRegion.isEmpty()) {
            if (RegionUtils.isInRegion(player, spawnRegion)) {
                return true;
            }
        }
        
        // Check if in owned/member ProtectionStones region
        Set<String> regions = RegionUtils.getRegionsAtLocation(player);
        for (String regionName : regions) {
            ProtectedRegion region = RegionUtils.getRegion(player, regionName);
            if (region != null && PSUtils.isProtectionStonesRegion(region)) {
                if (PSUtils.isOwnerOrMember(player, region)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Enable flight for a player.
     *
     * @param player The player
     */
    public void enableFlight(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return; // Already can fly
        }
        
        player.setAllowFlight(true);
        player.setFlying(true);
        flyingPlayers.put(player.getUniqueId(), true);
    }
    
    /**
     * Disable flight for a player safely.
     *
     * @param player The player
     */
    public void disableFlight(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return; // Don't disable for creative/spectator
        }
        
        if (!flyingPlayers.getOrDefault(player.getUniqueId(), false)) {
            return; // Not managed by us
        }
        
        player.setAllowFlight(false);
        flyingPlayers.remove(player.getUniqueId());
        
        // Safe landing: apply slow falling if player is in the air
        if (!player.isOnGround() && player.getLocation().getY() > player.getWorld().getMinHeight()) {
            // Apply slow falling for 10 seconds
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0, false, false));
            
            // Alternatively, find safe ground below
            Location safeLoc = findSafeGroundBelow(player.getLocation());
            if (safeLoc != null) {
                // Use entity scheduler for teleportation (Folia-compatible)
                player.getScheduler().run(plugin, task -> {
                    player.teleport(safeLoc);
                }, null);
            }
        }
    }
    
    /**
     * Find safe ground below a location.
     *
     * @param location The starting location
     * @return Safe location or null
     */
    private Location findSafeGroundBelow(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }
        
        int startY = location.getBlockY();
        int minY = world.getMinHeight();
        
        for (int y = startY; y >= minY; y--) {
            Location checkLoc = new Location(world, location.getX(), y, location.getZ());
            Material blockType = checkLoc.getBlock().getType();
            
            if (blockType.isSolid() && !blockType.isAir()) {
                // Found solid ground, return location above it
                return new Location(world, location.getX(), y + 1, location.getZ());
            }
        }
        
        return null;
    }
    
    /**
     * Check if a player is currently flying (managed by this plugin).
     *
     * @param player The player
     * @return True if flying
     */
    public boolean isFlying(Player player) {
        return flyingPlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Clean up player data.
     *
     * @param player The player
     */
    public void cleanup(Player player) {
        flyingPlayers.remove(player.getUniqueId());
    }
    
    /**
     * Get spawn region name.
     *
     * @return Spawn region name
     */
    public String getSpawnRegion() {
        return spawnRegion;
    }
}

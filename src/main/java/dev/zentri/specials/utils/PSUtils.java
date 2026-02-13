package dev.zentri.specials.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;

/**
 * Utility class for ProtectionStones integration.
 */
public class PSUtils {

    /**
     * Check if a region is a ProtectionStones region.
     *
     * @param region The WorldGuard region
     * @return True if it's a PS region
     */
    public static boolean isProtectionStonesRegion(ProtectedRegion region) {
        try {
            return PSRegion.fromWGRegion(region.getParent() == null ? region : region.getParent()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a player is an owner or member of a ProtectionStones region.
     *
     * @param player The player
     * @param region The WorldGuard region
     * @return True if player is owner or member
     */
    public static boolean isOwnerOrMember(Player player, ProtectedRegion region) {
        try {
            PSRegion psRegion = PSRegion.fromWGRegion(region.getParent() == null ? region : region.getParent());
            if (psRegion == null) {
                return false;
            }
            
            return psRegion.isOwner(player.getUniqueId()) || psRegion.isMember(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the PSRegion from a WorldGuard region.
     *
     * @param region The WorldGuard region
     * @return PSRegion or null
     */
    public static PSRegion getPSRegion(ProtectedRegion region) {
        try {
            return PSRegion.fromWGRegion(region.getParent() == null ? region : region.getParent());
        } catch (Exception e) {
            return null;
        }
    }
}

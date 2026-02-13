package dev.zentri.specials.utils;

import dev.espi.protectionstones.PSRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PSUtils {
    
    public static boolean isInOwnRegion(Player player, Location location) {
        try {
            PSRegion region = PSRegion.fromLocation(location);
            if (region == null) return false;
            
            // Check if player is owner or member
            return region.isOwner(player.getUniqueId()) || region.isMember(player.getUniqueId());
        } catch (Exception e) {
            // ProtectionStones not available or error
            return false;
        }
    }
    
    public static PSRegion getRegionAt(Location location) {
        try {
            return PSRegion.fromLocation(location);
        } catch (Exception e) {
            return null;
        }
    }
}

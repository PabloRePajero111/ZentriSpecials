package dev.zentri.specials.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Collectors;

public class RegionUtils {
    
    private static RegionContainer container;
    
    public static void init() {
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }
    
    public static boolean isInRegion(Location location, String regionName) {
        if (container == null) return false;
        
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isInAnyRegion(Location location, List<String> regionNames) {
        if (container == null || regionNames == null || regionNames.isEmpty()) {
            return false;
        }
        
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        
        List<String> lowerCaseNames = regionNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        
        for (ProtectedRegion region : set) {
            if (lowerCaseNames.contains(region.getId().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    public static List<String> getRegionsAt(Location location) {
        if (container == null) return List.of();
        
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        
        return set.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toList());
    }
}

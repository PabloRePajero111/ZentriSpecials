package dev.zentri.specials.managers;

import dev.zentri.specials.ZentriSpecials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ExperienceOrb;

import java.util.List;

public class ClumpsManager {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private double mergeRadius;
    private int maxXpPerOrb;
    private List<String> worlds;
    
    public ClumpsManager(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("clumps.enabled", true);
        this.mergeRadius = config.getDouble("clumps.merge-radius", 5.0);
        this.maxXpPerOrb = config.getInt("clumps.max-xp-per-orb", 10000);
        this.worlds = config.getStringList("clumps.worlds");
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public double getMergeRadius() {
        return mergeRadius;
    }
    
    public int getMaxXpPerOrb() {
        return maxXpPerOrb;
    }
    
    public boolean isWorldEnabled(World world) {
        if (worlds == null || worlds.isEmpty()) {
            return true; // All worlds if list is empty
        }
        return worlds.contains(world.getName());
    }
    
    public void mergeNearbyOrbs(ExperienceOrb orb) {
        if (!enabled || orb == null || !orb.isValid()) {
            return;
        }
        
        World world = orb.getWorld();
        if (!isWorldEnabled(world)) {
            return;
        }
        
        Location loc = orb.getLocation();
        int totalXp = orb.getExperience();
        
        // Find nearby orbs
        List<ExperienceOrb> nearbyOrbs = world.getNearbyEntities(loc, mergeRadius, mergeRadius, mergeRadius)
                .stream()
                .filter(entity -> entity instanceof ExperienceOrb)
                .map(entity -> (ExperienceOrb) entity)
                .filter(other -> other != orb && other.isValid())
                .toList();
        
        // Merge nearby orbs into this one
        for (ExperienceOrb other : nearbyOrbs) {
            if (totalXp >= maxXpPerOrb) {
                break;
            }
            
            int additionalXp = Math.min(other.getExperience(), maxXpPerOrb - totalXp);
            totalXp += additionalXp;
            
            // Remove the other orb
            other.remove();
        }
        
        // Update the main orb's experience
        if (totalXp != orb.getExperience()) {
            orb.setExperience(Math.min(totalXp, maxXpPerOrb));
        }
    }
}

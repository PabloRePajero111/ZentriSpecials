package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.managers.ClumpsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class ClumpsListener implements Listener {
    
    private final ZentriSpecials plugin;
    private final ClumpsManager manager;
    
    public ClumpsListener(ZentriSpecials plugin, ClumpsManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOrbSpawn(EntitySpawnEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        
        if (event.getEntityType() != EntityType.EXPERIENCE_ORB) {
            return;
        }
        
        ExperienceOrb orb = (ExperienceOrb) event.getEntity();
        
        if (!manager.isWorldEnabled(orb.getWorld())) {
            return;
        }
        
        // Schedule merge check on the entity's region (Folia-compatible)
        // Use a small delay to allow other orbs to spawn first
        orb.getScheduler().runDelayed(plugin, task -> {
            if (orb.isValid()) {
                manager.mergeNearbyOrbs(orb);
            }
        }, null, 5L); // 5 ticks delay
    }
}

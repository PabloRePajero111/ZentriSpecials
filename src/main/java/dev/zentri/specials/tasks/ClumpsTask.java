package dev.zentri.specials.tasks;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.managers.ClumpsManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;

import java.util.concurrent.TimeUnit;

public class ClumpsTask {
    
    private final ZentriSpecials plugin;
    private final ClumpsManager manager;
    private ScheduledTask task;
    
    public ClumpsTask(ZentriSpecials plugin, ClumpsManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    
    public void start() {
        if (task != null) {
            stop();
        }
        
        int interval = plugin.getConfig().getInt("clumps.check-interval", 40);
        
        // Use global region scheduler for periodic checks across all regions (Folia-compatible)
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!manager.isEnabled()) {
                return;
            }
            
            // Process all loaded worlds
            for (World world : Bukkit.getWorlds()) {
                if (!manager.isWorldEnabled(world)) {
                    continue;
                }
                
                // Get all experience orbs in the world
                world.getEntitiesByClass(ExperienceOrb.class).forEach(orb -> {
                    if (orb.isValid()) {
                        // Schedule merge on the orb's entity scheduler (region-specific)
                        orb.getScheduler().run(plugin, task2 -> {
                            if (orb.isValid()) {
                                manager.mergeNearbyOrbs(orb);
                            }
                        }, null);
                    }
                });
            }
        }, 1, interval); // Start after 1 tick, run every interval ticks
    }
    
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

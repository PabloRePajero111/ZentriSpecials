package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CompactDeathDropsListener implements Listener {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private double spreadRadius;
    private int pickupDelay;
    private double yVelocity;
    private List<String> worlds;
    
    public CompactDeathDropsListener(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("compact-death-drops.enabled", true);
        this.spreadRadius = plugin.getConfig().getDouble("compact-death-drops.spread-radius", 0.3);
        this.pickupDelay = plugin.getConfig().getInt("compact-death-drops.pickup-delay", 20);
        this.yVelocity = plugin.getConfig().getDouble("compact-death-drops.y-velocity", 0.1);
        this.worlds = plugin.getConfig().getStringList("compact-death-drops.worlds");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!enabled) {
            return;
        }
        
        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        
        // Check if world is enabled
        if (worlds != null && !worlds.isEmpty() && !worlds.contains(deathLoc.getWorld().getName())) {
            return;
        }
        
        // If keepInventory is active, don't do anything
        if (event.getKeepInventory()) {
            return;
        }
        
        // Copy the drops
        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        
        // Spawn items with minimal velocity (Folia-compatible)
        Bukkit.getRegionScheduler().run(plugin, deathLoc, task -> {
            for (ItemStack item : drops) {
                if (item == null || item.getType().isAir()) {
                    continue;
                }
                
                // Calculate random offset within spread radius
                double offsetX = (Math.random() - 0.5) * 2 * spreadRadius;
                double offsetZ = (Math.random() - 0.5) * 2 * spreadRadius;
                Location dropLoc = deathLoc.clone().add(offsetX, 0, offsetZ);
                
                // Drop the item
                Item droppedItem = deathLoc.getWorld().dropItem(dropLoc, item);
                
                // Set minimal velocity
                droppedItem.setVelocity(new Vector(0, yVelocity, 0));
                
                // Set pickup delay
                droppedItem.setPickupDelay(pickupDelay);
            }
        });
    }
}

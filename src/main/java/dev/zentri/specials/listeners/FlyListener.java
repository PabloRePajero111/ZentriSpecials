package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.managers.FlyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class FlyListener implements Listener {
    
    private final ZentriSpecials plugin;
    private final FlyManager manager;
    
    public FlyListener(ZentriSpecials plugin, FlyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        
        // Only check if player moved to a different block
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            Player player = event.getPlayer();
            // Use player scheduler for Folia compatibility
            player.getScheduler().run(plugin, task -> {
                manager.checkFly(player);
            }, null);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        // Delayed check to ensure player is fully loaded
        player.getScheduler().runDelayed(plugin, task -> {
            manager.checkFly(player);
        }, null, 20L); // 1 second delay
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        
        if (manager.hasFallProtection(player)) {
            event.setCancelled(true);
            manager.removeFallProtection(player);
        }
    }
}

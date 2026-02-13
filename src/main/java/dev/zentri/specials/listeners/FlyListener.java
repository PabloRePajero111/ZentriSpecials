package dev.zentri.specials.listeners;

import dev.zentri.specials.managers.FlyManager;
import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for fly permissions in spawn and ProtectionStones regions.
 */
public class FlyListener implements Listener {
    
    private final FlyManager flyManager;
    private final FileConfiguration config;
    
    public FlyListener(FlyManager flyManager, FileConfiguration config) {
        this.flyManager = flyManager;
        this.config = config;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!config.getBoolean("fly.enabled", true)) {
            return;
        }
        
        // Optimize: only check when block position changes
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        boolean canFly = flyManager.canFlyAtLocation(player);
        boolean isFlying = flyManager.isFlying(player);
        
        if (canFly && !isFlying) {
            // Enable flight
            flyManager.enableFlight(player);
            
            String message = MessageUtils.getMessage(config, "fly.messages.fly-enabled");
            if (message != null && !message.isEmpty()) {
                MessageUtils.sendMessage(player, message);
            }
        } else if (!canFly && isFlying) {
            // Disable flight
            flyManager.disableFlight(player);
            
            String message = MessageUtils.getMessage(config, "fly.messages.fly-disabled");
            if (message != null && !message.isEmpty()) {
                MessageUtils.sendMessage(player, message);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        flyManager.cleanup(event.getPlayer());
    }
}

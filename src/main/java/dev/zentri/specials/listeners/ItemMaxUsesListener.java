package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.managers.MaxUsesManager;
import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class ItemMaxUsesListener implements Listener {
    
    private final ZentriSpecials plugin;
    private final MaxUsesManager manager;
    
    public ItemMaxUsesListener(ZentriSpecials plugin, MaxUsesManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType().isAir()) {
            return;
        }
        
        Material material = item.getType();
        
        if (!manager.canUse(player, player.getLocation(), material)) {
            event.setCancelled(true);
            int maxUses = manager.getMaxUses(player.getLocation(), material);
            player.sendMessage(MessageUtils.formatMessage("max-uses.limit-reached", "max", maxUses));
            return;
        }
        
        // Increment use if there's a limit
        if (manager.getMaxUses(player.getLocation(), material) > 0) {
            manager.incrementUse(player, player.getLocation(), material);
        }
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
            
            // Check for region change (Folia-compatible - run on player's scheduler)
            Player player = event.getPlayer();
            player.getScheduler().run(plugin, task -> {
                manager.checkRegionChange(player);
            }, null);
        }
    }
}

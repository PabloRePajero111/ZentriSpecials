package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.managers.CooldownManager;
import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemCooldownListener implements Listener {
    
    private final ZentriSpecials plugin;
    private final CooldownManager manager;
    
    public ItemCooldownListener(ZentriSpecials plugin, CooldownManager manager) {
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
        int cooldownTicks = manager.getCooldownTicks(player.getLocation(), material);
        
        if (cooldownTicks > 0) {
            // Check if player is already on cooldown
            if (manager.isOnCooldown(player, material)) {
                event.setCancelled(true);
                long remainingTicks = manager.getRemainingCooldown(player, material);
                String time = MessageUtils.formatTime((remainingTicks / 20) + 1); // Convert ticks to seconds
                player.sendMessage(MessageUtils.formatMessage("cooldown.active", "time", time));
                return;
            }
            
            // Apply cooldown after use
            manager.applyCooldown(player, material, cooldownTicks);
        }
    }
}

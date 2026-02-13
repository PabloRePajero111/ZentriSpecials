package dev.zentri.specials.listeners;

import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Listener for blocking disabled items in regions.
 */
public class DisabledItemsListener implements Listener {
    
    private final FileConfiguration config;
    private final Map<String, Set<Material>> disabledItemsByRegion;
    
    public DisabledItemsListener(FileConfiguration config) {
        this.config = config;
        this.disabledItemsByRegion = new HashMap<>();
        loadDisabledItems();
    }
    
    /**
     * Load disabled items configuration.
     */
    public void loadDisabledItems() {
        disabledItemsByRegion.clear();
        
        ConfigurationSection regionsSection = config.getConfigurationSection("disabled-items.regions");
        if (regionsSection == null) {
            return;
        }
        
        for (String region : regionsSection.getKeys(false)) {
            List<String> items = config.getStringList("disabled-items.regions." + region);
            Set<Material> materials = new HashSet<>();
            
            for (String itemName : items) {
                try {
                    Material material = Material.valueOf(itemName.toUpperCase());
                    materials.add(material);
                } catch (IllegalArgumentException e) {
                    // Invalid material name, skip
                }
            }
            
            if (!materials.isEmpty()) {
                disabledItemsByRegion.put(region, materials);
            }
        }
    }
    
    /**
     * Check if item is disabled in player's current regions.
     */
    private boolean isItemDisabled(Player player, Material material) {
        if (!config.getBoolean("disabled-items.enabled", true)) {
            return false;
        }
        
        Set<String> regions = RegionUtils.getRegionsAtLocation(player);
        if (regions.isEmpty()) {
            return false;
        }
        
        for (String region : regions) {
            Set<Material> disabledItems = disabledItemsByRegion.get(region);
            if (disabledItems != null && disabledItems.contains(material)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Send disabled message to player.
     */
    private void sendDisabledMessage(Player player, Material material) {
        String message = MessageUtils.getMessage(config, "disabled-items.messages.item-disabled");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", material.name());
        MessageUtils.sendMessage(player, message, placeholders);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        if (isItemDisabled(player, item.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, item.getType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.AIR && isItemDisabled(player, item.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, item.getType());
            return;
        }
        
        // Check offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() != Material.AIR && isItemDisabled(player, offhand.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, offhand.getType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.AIR && isItemDisabled(player, item.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, item.getType());
            return;
        }
        
        // Check offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() != Material.AIR && isItemDisabled(player, offhand.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, offhand.getType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        if (item.getType() != Material.AIR && isItemDisabled(player, item.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, item.getType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (isItemDisabled(player, item.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, item.getType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Material bucket = event.getBucket();
        
        if (isItemDisabled(player, bucket)) {
            event.setCancelled(true);
            sendDisabledMessage(player, bucket);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Material bucket = event.getBucket();
        
        if (isItemDisabled(player, bucket)) {
            event.setCancelled(true);
            sendDisabledMessage(player, bucket);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        ItemStack bow = event.getBow();
        if (bow != null && isItemDisabled(player, bow.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, bow.getType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        
        // Check main hand and offhand for projectile items
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (mainHand.getType() != Material.AIR && isItemDisabled(player, mainHand.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, mainHand.getType());
            return;
        }
        
        if (offHand.getType() != Material.AIR && isItemDisabled(player, offHand.getType())) {
            event.setCancelled(true);
            sendDisabledMessage(player, offHand.getType());
        }
    }
}

package dev.zentri.specials.listeners;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisabledItemsListener implements Listener {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private final Map<String, List<Material>> regionDisabledItems = new HashMap<>();
    private final Map<String, String> regionMessages = new HashMap<>();
    
    public DisabledItemsListener(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        regionDisabledItems.clear();
        regionMessages.clear();
        this.enabled = plugin.getConfig().getBoolean("disabled-items.enabled", true);
        
        ConfigurationSection regions = plugin.getConfig().getConfigurationSection("disabled-items.regions");
        if (regions != null) {
            for (String regionName : regions.getKeys(false)) {
                ConfigurationSection regionSection = regions.getConfigurationSection(regionName);
                if (regionSection != null) {
                    List<String> itemNames = regionSection.getStringList("items");
                    List<Material> materials = itemNames.stream()
                            .map(name -> {
                                try {
                                    return Material.valueOf(name.toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().warning("Invalid material in disabled-items config: " + name);
                                    return null;
                                }
                            })
                            .filter(m -> m != null)
                            .toList();
                    
                    regionDisabledItems.put(regionName.toLowerCase(), materials);
                    regionMessages.put(regionName.toLowerCase(), regionSection.getString("message", ""));
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!enabled) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType().isAir()) {
            return;
        }
        
        for (Map.Entry<String, List<Material>> entry : regionDisabledItems.entrySet()) {
            if (RegionUtils.isInRegion(player.getLocation(), entry.getKey())) {
                if (entry.getValue().contains(item.getType())) {
                    event.setCancelled(true);
                    String message = regionMessages.get(entry.getKey());
                    if (message != null && !message.isEmpty()) {
                        player.sendMessage(MessageUtils.getPrefix() + MessageUtils.colorize(message));
                    }
                    return;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!enabled) {
            return;
        }
        
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        
        for (Map.Entry<String, List<Material>> entry : regionDisabledItems.entrySet()) {
            if (RegionUtils.isInRegion(player.getLocation(), entry.getKey())) {
                if (entry.getValue().contains(material)) {
                    event.setCancelled(true);
                    String message = regionMessages.get(entry.getKey());
                    if (message != null && !message.isEmpty()) {
                        player.sendMessage(MessageUtils.getPrefix() + MessageUtils.colorize(message));
                    }
                    return;
                }
            }
        }
    }
}

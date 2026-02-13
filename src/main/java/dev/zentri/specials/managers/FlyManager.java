package dev.zentri.specials.managers;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.PSUtils;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyManager {
    
    private final ZentriSpecials plugin;
    private boolean enabled;
    private String spawnRegion;
    private boolean enableInSpawn;
    private boolean enableInProtectionStones;
    private boolean antiFallDamage;
    private int antiFallTicks;
    private final Map<UUID, Long> fallProtection = new HashMap<>();
    
    public FlyManager(ZentriSpecials plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("fly.enabled", true);
        this.spawnRegion = plugin.getConfig().getString("fly.spawn-region", "spawn");
        this.enableInSpawn = plugin.getConfig().getBoolean("fly.enable-in-spawn", true);
        this.enableInProtectionStones = plugin.getConfig().getBoolean("fly.enable-in-protectionstones", true);
        this.antiFallDamage = plugin.getConfig().getBoolean("fly.anti-fall-damage", true);
        this.antiFallTicks = plugin.getConfig().getInt("fly.anti-fall-ticks", 60);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void checkFly(Player player) {
        if (!enabled) {
            return;
        }
        
        // Don't manage fly for creative/spectator players
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        boolean shouldFly = false;
        
        // Check spawn region
        if (enableInSpawn && player.hasPermission("zentrispecials.fly.spawn")) {
            if (RegionUtils.isInRegion(player.getLocation(), spawnRegion)) {
                shouldFly = true;
            }
        }
        
        // Check ProtectionStones
        if (!shouldFly && enableInProtectionStones && player.hasPermission("zentrispecials.fly.protectionstones")) {
            if (PSUtils.isInOwnRegion(player, player.getLocation())) {
                shouldFly = true;
            }
        }
        
        // Enable or disable fly
        if (shouldFly) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(true);
                String message = MessageUtils.getMessage("fly.enabled");
                if (!message.isEmpty()) {
                    player.sendMessage(MessageUtils.getPrefix() + message);
                }
            }
        } else {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                
                // Add fall protection
                if (antiFallDamage) {
                    fallProtection.put(player.getUniqueId(), System.currentTimeMillis() + (antiFallTicks * 50L));
                }
                
                String message = MessageUtils.getMessage("fly.disabled");
                if (!message.isEmpty()) {
                    player.sendMessage(MessageUtils.getPrefix() + message);
                }
            }
        }
    }
    
    public boolean hasFallProtection(Player player) {
        if (!antiFallDamage) {
            return false;
        }
        
        Long endTime = fallProtection.get(player.getUniqueId());
        if (endTime == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > endTime) {
            fallProtection.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }
    
    public void removeFallProtection(Player player) {
        fallProtection.remove(player.getUniqueId());
    }
}

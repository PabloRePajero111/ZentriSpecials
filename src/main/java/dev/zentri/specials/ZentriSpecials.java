package dev.zentri.specials;

import dev.zentri.specials.commands.FixAllCommand;
import dev.zentri.specials.commands.FixHandCommand;
import dev.zentri.specials.commands.ZentriSpecialsCommand;
import dev.zentri.specials.listeners.*;
import dev.zentri.specials.managers.CooldownManager;
import dev.zentri.specials.managers.FlyManager;
import dev.zentri.specials.managers.MaxUsesManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Main plugin class for ZentriSpecials.
 */
public class ZentriSpecials extends JavaPlugin {
    
    private CooldownManager cooldownManager;
    private MaxUsesManager maxUsesManager;
    private FlyManager flyManager;
    private BarrierEntityKillTask barrierEntityKillTask;
    private DisabledItemsListener disabledItemsListener;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        cooldownManager = new CooldownManager();
        maxUsesManager = new MaxUsesManager(this);
        
        String spawnRegion = getConfig().getString("fly.spawn-region", "spawn");
        String flyPermission = getConfig().getString("fly.permission", "zentrispecials.fly");
        flyManager = new FlyManager(this, spawnRegion, flyPermission);
        
        // Load configurations
        loadCooldowns();
        loadMaxUses();
        
        // Initialize barrier entity kill task
        barrierEntityKillTask = new BarrierEntityKillTask(this, getConfig());
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        // Start barrier entity kill task
        barrierEntityKillTask.start();
        
        getLogger().info("ZentriSpecials has been enabled!");
        getLogger().info("Folia-compatible schedulers are being used.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ZentriSpecials has been disabled!");
    }
    
    /**
     * Register all event listeners.
     */
    private void registerListeners() {
        // Item cooldown listener
        if (getConfig().getBoolean("item-cooldowns.enabled", true)) {
            getServer().getPluginManager().registerEvents(
                new ItemCooldownListener(cooldownManager, getConfig()), this);
        }
        
        // Item max uses listener
        if (getConfig().getBoolean("item-max-uses.enabled", true)) {
            getServer().getPluginManager().registerEvents(
                new ItemMaxUsesListener(maxUsesManager, getConfig()), this);
        }
        
        // Disabled items listener
        if (getConfig().getBoolean("disabled-items.enabled", true)) {
            disabledItemsListener = new DisabledItemsListener(getConfig());
            getServer().getPluginManager().registerEvents(disabledItemsListener, this);
        }
        
        // Fly listener
        if (getConfig().getBoolean("fly.enabled", true)) {
            getServer().getPluginManager().registerEvents(
                new FlyListener(flyManager, getConfig()), this);
        }
    }
    
    /**
     * Register all commands.
     */
    private void registerCommands() {
        if (getConfig().getBoolean("fix.enabled", true)) {
            getCommand("fixhand").setExecutor(new FixHandCommand(getConfig()));
            getCommand("fixall").setExecutor(new FixAllCommand(getConfig()));
        }
        
        getCommand("zentrispecials").setExecutor(new ZentriSpecialsCommand(this));
    }
    
    /**
     * Load cooldown configurations.
     */
    private void loadCooldowns() {
        Map<String, Map<Material, Integer>> regionCooldowns = new HashMap<>();
        
        ConfigurationSection regionsSection = getConfig().getConfigurationSection("item-cooldowns.regions");
        if (regionsSection != null) {
            for (String region : regionsSection.getKeys(false)) {
                Map<Material, Integer> materialCooldowns = new HashMap<>();
                
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(region);
                if (regionSection != null) {
                    for (String materialName : regionSection.getKeys(false)) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            int cooldownTicks = regionSection.getInt(materialName, 0);
                            materialCooldowns.put(material, cooldownTicks);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("Invalid material name in cooldowns config: " + materialName);
                        }
                    }
                }
                
                if (!materialCooldowns.isEmpty()) {
                    regionCooldowns.put(region, materialCooldowns);
                }
            }
        }
        
        cooldownManager.loadCooldowns(regionCooldowns);
    }
    
    /**
     * Load max uses configurations.
     */
    private void loadMaxUses() {
        Map<String, Map<Material, MaxUsesManager.MaxUseConfig>> regionMaxUses = new HashMap<>();
        
        ConfigurationSection regionsSection = getConfig().getConfigurationSection("item-max-uses.regions");
        if (regionsSection != null) {
            for (String region : regionsSection.getKeys(false)) {
                Map<Material, MaxUsesManager.MaxUseConfig> materialConfigs = new HashMap<>();
                
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(region);
                if (regionSection != null) {
                    for (String materialName : regionSection.getKeys(false)) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            ConfigurationSection materialSection = regionSection.getConfigurationSection(materialName);
                            
                            if (materialSection != null) {
                                int maxUses = materialSection.getInt("max-uses", 1);
                                boolean resetOnLeave = materialSection.getBoolean("reset-on-leave", false);
                                int resetTime = materialSection.getInt("reset-time", 0);
                                
                                MaxUsesManager.MaxUseConfig config = 
                                    new MaxUsesManager.MaxUseConfig(maxUses, resetOnLeave, resetTime);
                                materialConfigs.put(material, config);
                            }
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("Invalid material name in max-uses config: " + materialName);
                        }
                    }
                }
                
                if (!materialConfigs.isEmpty()) {
                    regionMaxUses.put(region, materialConfigs);
                }
            }
        }
        
        maxUsesManager.loadMaxUses(regionMaxUses);
    }
    
    /**
     * Reload the plugin configuration.
     */
    public void reloadConfiguration() {
        reloadConfig();
        
        // Clear and reload managers
        cooldownManager.clear();
        maxUsesManager.clear();
        
        loadCooldowns();
        loadMaxUses();
        
        // Reload disabled items
        if (disabledItemsListener != null) {
            disabledItemsListener.loadDisabledItems();
        }
        
        // Reload barrier entity kill task
        if (barrierEntityKillTask != null) {
            barrierEntityKillTask.reload();
        }
        
        getLogger().info("Configuration reloaded successfully!");
    }
}

package dev.zentri.specials;

import dev.zentri.specials.commands.FixAllCommand;
import dev.zentri.specials.commands.FixHandCommand;
import dev.zentri.specials.commands.ReloadCommand;
import dev.zentri.specials.listeners.*;
import dev.zentri.specials.managers.*;
import dev.zentri.specials.tasks.ClumpsTask;
import dev.zentri.specials.utils.MessageUtils;
import dev.zentri.specials.utils.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ZentriSpecials extends JavaPlugin {
    
    // Managers
    private ClumpsManager clumpsManager;
    private CooldownManager cooldownManager;
    private MaxUsesManager maxUsesManager;
    private FlyManager flyManager;
    
    // Tasks
    private ClumpsTask clumpsTask;
    
    // Listeners
    private CompactDeathDropsListener compactDeathDropsListener;
    private DisabledItemsListener disabledItemsListener;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize utilities
        MessageUtils.setConfig(getConfig());
        RegionUtils.init();
        
        // Initialize managers
        clumpsManager = new ClumpsManager(this);
        cooldownManager = new CooldownManager(this);
        maxUsesManager = new MaxUsesManager(this);
        flyManager = new FlyManager(this);
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        // Start tasks
        startTasks();
        
        getLogger().info("ZentriSpecials has been enabled!");
        getLogger().info("Folia-compatible mode: ACTIVE");
    }
    
    @Override
    public void onDisable() {
        // Stop tasks
        if (clumpsTask != null) {
            clumpsTask.stop();
        }
        
        getLogger().info("ZentriSpecials has been disabled!");
    }
    
    private void registerListeners() {
        // NEW Feature 1: Clumps
        getServer().getPluginManager().registerEvents(
                new ClumpsListener(this, clumpsManager), this);
        
        // NEW Feature 2: Compact Death Drops
        compactDeathDropsListener = new CompactDeathDropsListener(this);
        getServer().getPluginManager().registerEvents(compactDeathDropsListener, this);
        
        // Existing Features
        getServer().getPluginManager().registerEvents(
                new ItemCooldownListener(this, cooldownManager), this);
        getServer().getPluginManager().registerEvents(
                new ItemMaxUsesListener(this, maxUsesManager), this);
        
        disabledItemsListener = new DisabledItemsListener(this);
        getServer().getPluginManager().registerEvents(disabledItemsListener, this);
        
        getServer().getPluginManager().registerEvents(
                new BarrierEntityKillListener(this), this);
        getServer().getPluginManager().registerEvents(
                new FlyListener(this, flyManager), this);
    }
    
    private void registerCommands() {
        getCommand("fixhand").setExecutor(new FixHandCommand(this));
        getCommand("fixall").setExecutor(new FixAllCommand(this));
        getCommand("zentrispecials").setExecutor(new ReloadCommand(this));
    }
    
    private void startTasks() {
        // Clumps periodic merge task
        clumpsTask = new ClumpsTask(this, clumpsManager);
        clumpsTask.start();
    }
    
    public void reloadAllComponents() {
        // Reload config
        MessageUtils.setConfig(getConfig());
        
        // Reload managers
        clumpsManager.loadConfig();
        cooldownManager.loadConfig();
        maxUsesManager.loadConfig();
        flyManager.loadConfig();
        
        // Reload listeners
        compactDeathDropsListener.loadConfig();
        disabledItemsListener.loadConfig();
        
        // Restart tasks
        if (clumpsTask != null) {
            clumpsTask.stop();
            clumpsTask.start();
        }
    }
}

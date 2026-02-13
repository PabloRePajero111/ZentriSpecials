package dev.zentri.specials.commands;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FixAllCommand implements CommandExecutor {
    
    private final ZentriSpecials plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public FixAllCommand(ZentriSpecials plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (!plugin.getConfig().getBoolean("fix-commands.fix-all.enabled", true)) {
            player.sendMessage(MessageUtils.formatMessage("no-permission"));
            return true;
        }
        
        // Get cooldown based on permissions
        long cooldownSeconds = getCooldown(player);
        
        // Check cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) + (cooldownSeconds * 1000) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage(MessageUtils.formatMessage("fix-all.cooldown", 
                    "time", MessageUtils.formatTime(timeLeft)));
                return true;
            }
        }
        
        int repairedCount = 0;
        
        // Repair all items in inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            
            if (!(item.getItemMeta() instanceof Damageable)) {
                continue;
            }
            
            Damageable meta = (Damageable) item.getItemMeta();
            if (meta.getDamage() > 0) {
                meta.setDamage(0);
                item.setItemMeta(meta);
                repairedCount++;
            }
        }
        
        // Also check armor
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            
            if (!(item.getItemMeta() instanceof Damageable)) {
                continue;
            }
            
            Damageable meta = (Damageable) item.getItemMeta();
            if (meta.getDamage() > 0) {
                meta.setDamage(0);
                item.setItemMeta(meta);
                repairedCount++;
            }
        }
        
        if (repairedCount == 0) {
            player.sendMessage(MessageUtils.formatMessage("fix-all.no-items"));
            return true;
        }
        
        // Set cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        player.sendMessage(MessageUtils.formatMessage("fix-all.success", "count", repairedCount));
        return true;
    }
    
    private long getCooldown(Player player) {
        if (player.hasPermission("zentrispecials.fixall.mvp")) {
            return plugin.getConfig().getLong("fix-commands.fix-all.cooldowns.mvp", 120);
        } else if (player.hasPermission("zentrispecials.fixall.vip")) {
            return plugin.getConfig().getLong("fix-commands.fix-all.cooldowns.vip", 300);
        } else {
            return plugin.getConfig().getLong("fix-commands.fix-all.cooldowns.default", 600);
        }
    }
}

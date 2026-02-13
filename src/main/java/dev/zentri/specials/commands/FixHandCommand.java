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

public class FixHandCommand implements CommandExecutor {
    
    private final ZentriSpecials plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public FixHandCommand(ZentriSpecials plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (!plugin.getConfig().getBoolean("fix-commands.fix-hand.enabled", true)) {
            player.sendMessage(MessageUtils.formatMessage("no-permission"));
            return true;
        }
        
        // Get cooldown based on permissions
        long cooldownSeconds = getCooldown(player);
        
        // Check cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) + (cooldownSeconds * 1000) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage(MessageUtils.formatMessage("fix-hand.cooldown", 
                    "time", MessageUtils.formatTime(timeLeft)));
                return true;
            }
        }
        
        // Get item in hand
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            player.sendMessage(MessageUtils.formatMessage("fix-hand.no-item"));
            return true;
        }
        
        if (!(item.getItemMeta() instanceof Damageable)) {
            player.sendMessage(MessageUtils.formatMessage("fix-hand.not-repairable"));
            return true;
        }
        
        Damageable meta = (Damageable) item.getItemMeta();
        if (meta.getDamage() == 0) {
            player.sendMessage(MessageUtils.formatMessage("fix-hand.not-repairable"));
            return true;
        }
        
        // Repair the item
        meta.setDamage(0);
        item.setItemMeta(meta);
        
        // Set cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        player.sendMessage(MessageUtils.formatMessage("fix-hand.success"));
        return true;
    }
    
    private long getCooldown(Player player) {
        if (player.hasPermission("zentrispecials.fixhand.mvp")) {
            return plugin.getConfig().getLong("fix-commands.fix-hand.cooldowns.mvp", 60);
        } else if (player.hasPermission("zentrispecials.fixhand.vip")) {
            return plugin.getConfig().getLong("fix-commands.fix-hand.cooldowns.vip", 180);
        } else {
            return plugin.getConfig().getLong("fix-commands.fix-hand.cooldowns.default", 300);
        }
    }
}

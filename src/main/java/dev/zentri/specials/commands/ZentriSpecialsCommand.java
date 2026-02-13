package dev.zentri.specials.commands;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Main plugin command for reload and other operations.
 */
public class ZentriSpecialsCommand implements CommandExecutor {
    
    private final ZentriSpecials plugin;
    
    public ZentriSpecialsCommand(ZentriSpecials plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageUtils.colorize("&8[&bZentri&8] &7ZentriSpecials v" + plugin.getDescription().getVersion()));
            sender.sendMessage(MessageUtils.colorize("&7Use &b/zentrispecials reload &7to reload configuration"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("zentrispecials.reload")) {
                String message = MessageUtils.getMessage(plugin.getConfig(), "fix.messages.no-permission");
                sender.sendMessage(MessageUtils.colorize(message));
                return true;
            }
            
            plugin.reloadConfiguration();
            String message = MessageUtils.getMessage(plugin.getConfig(), "reload.message");
            sender.sendMessage(MessageUtils.colorize(message));
            return true;
        }
        
        sender.sendMessage(MessageUtils.colorize("&cUnknown subcommand. Use &b/zentrispecials reload"));
        return true;
    }
}

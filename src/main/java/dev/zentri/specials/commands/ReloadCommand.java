package dev.zentri.specials.commands;

import dev.zentri.specials.ZentriSpecials;
import dev.zentri.specials.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    
    private final ZentriSpecials plugin;
    
    public ReloadCommand(ZentriSpecials plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(MessageUtils.colorize("&cUsage: /zentrispecials reload"));
            return true;
        }
        
        if (!sender.hasPermission("zentrispecials.reload")) {
            sender.sendMessage(MessageUtils.formatMessage("no-permission"));
            return true;
        }
        
        plugin.reloadConfig();
        plugin.reloadAllComponents();
        
        sender.sendMessage(MessageUtils.formatMessage("reload-success"));
        return true;
    }
}

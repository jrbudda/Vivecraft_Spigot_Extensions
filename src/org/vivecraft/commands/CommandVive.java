package org.vivecraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandVive implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Here we need to give items to our player

            if (args.length == 1) { // Check argument length
                if (args[0].equalsIgnoreCase("info")) { // Check first argument
                    player.sendMessage(ChatColor.GREEN + "This server its running ViveSpigot [1.10.2 r1] ");
                    
                    return true; // return, everything else doesn't matter
                                 // anymore
                }
            }
        }// here
        if (args.length == 1) { // Check argument length
            if (args[0].equalsIgnoreCase("creators")) { // Check first argument
                Player player = (Player) sender;
                player.sendMessage(ChatColor.GREEN + "jrbudda TechJar  ");
                
                return true; // return, everything else doesn't matter
                             // anymore
            }
        
        
        
        return false;
        

    }
        
        
        
        return false;
        
}
}

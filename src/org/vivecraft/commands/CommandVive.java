package org.vivecraft.commands;

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

                    return true; // return, everything else doesn't matter
                                 // anymore
                }

                return true;
            }

            return false;
        }

        if (args.length == 1) { // Check argument length
            if (args[0].equalsIgnoreCase("")) { // Check first argument
                
                return true; // return, everything else doesn't matter anymore
            }

        }

        return false;

    }
}

package org.vivecraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ViveInfoCommand extends JavaPlugin implements CommandExecutor{
    @Override 
    public void onEnable(){

        this.getLogger().info("The ViveInfo Command Have been Enabled!");

    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player || sender.hasPermission("SpigotVive.viveinfo")) {
            Player player = (Player) sender;
            player.sendMessage("This plugin its Version [1.10.2 r1] And the server its runnng :" + "["+ this.getServer().getBukkitVersion() + "]");

        }

        this.getLogger().info("Only players can run this command!");
        return true;
    }



}


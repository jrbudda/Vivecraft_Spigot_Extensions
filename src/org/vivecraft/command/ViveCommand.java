package org.vivecraft.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.vivecraft.VSE;

public class ViveCommand implements CommandExecutor{
	
	private VSE plugin;

	public ViveCommand(VSE vse) {
		plugin = vse;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player || sender.hasPermission("SpigotVive.vive")) {
			Player player = (Player) sender;
			if (args.length >= 1) {
				if(args[0].equals("vive-only")){
					if(args.length >= 2){
						if(args[1].equals("true")){
							plugin.getConfig().set("vive-only.enabled", true);
							player.sendMessage("[ViveCraft] Vive-Only has been enabled.");
						}else
						if(args[1].equals("false")){
							plugin.getConfig().set("vive-only.enabled", false);
							player.sendMessage("[ViveCraft] Vive-Only has been disabled.");
						}
					}else{
						player.sendMessage("[ViveCraft] Vive-Only: " + plugin.getConfig().get("vive-only.enabled"));
					}
					plugin.saveConfig();
				}else
				//
				if(args[0].equals("waittime")){
					if(args.length >= 2){
						try {
							plugin.getConfig().set("vive-only.waittime", Integer.parseInt(args[1]));
							player.sendMessage("[ViveCraft] waittime set to " + Integer.parseInt(args[1]));
						} catch (NumberFormatException e) {
							player.sendMessage("[ViveCraft] Must use numbers");
						}
					}else{
						player.sendMessage("[ViveCraft] waittime: " + plugin.getConfig().get("vive-only.waittime"));
					}
					plugin.saveConfig();
				}else{
					player.sendMessage("[ViveCraft] Unknown command");
				}
			}
		} else if (!(sender instanceof Player))
			plugin.getLogger().info("Only players can run this command!");
		return true;
	}

}

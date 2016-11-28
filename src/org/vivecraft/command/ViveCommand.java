package org.vivecraft.command;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.vivecraft.VSE;

public class ViveCommand implements CommandExecutor {

	private VSE plugin;
	private ArrayList<Cmd> commands = new ArrayList<Cmd>();

	public ViveCommand(VSE vse) {
		plugin = vse;
		commands.add(new Cmd("vive-only", "Set to true to only allow Vivecraft players to play. Default: false"));
		commands.add(new Cmd("waittime","Ticks to wait before kicking a player. The player's client must send a Vivecraft VERSION info in that time. Default: 60"));
		commands.add(new Cmd("version", "returns the version of the plugin."));
		commands.add(new Cmd("sendplayerdata", "set to false to disable sending player to data to clients. Default: true"));
		commands.add(new Cmd("creeperradius", "type false to disable or type a number to change the radius. Default: 1.75"));
		commands.add(new Cmd("bow", "Sets the multiplier for bow damage of vive users. Default: 2"));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player){
			Player player = (Player) sender;
			if (args.length >= 1) {
				String command = args[0].toLowerCase();
				//
				if (command.equals("vive-only") && sender.isOp()) {
					if (args.length >= 2) {
						if (args[1].toLowerCase().equals("true")) {
							plugin.getConfig().set("vive-only.enabled", true);
							sendMessage("Vive-Only has been enabled.", player);

						} else if (args[1].toLowerCase().equals("false")) {
							plugin.getConfig().set("vive-only.enabled", false);
							sendMessage("Vive-Only has been disabled.", player);
						}
					} else {
						sendMessage("Vive-Only: " + plugin.getConfig().get("vive-only.enabled"), player);
					}
					plugin.saveConfig();
				} else
			    //
				if(command.equals("sendplayerdata") && sender.isOp()){
					if(args.length >= 2){
						if(args[1].toLowerCase().equals("true")){
							plugin.getConfig().set("SendPlayerData.enabled", true);
							sendMessage("SendPlayerData has been enabled.", player);
						}else if (args[1].toLowerCase().equals("false")) {
							plugin.getConfig().set("SendPlayerData.enabled", false);
							sendMessage("SendPlayerData has been disabled.", player);
						}
					}else{
						sendMessage("SendPlayerData: " + plugin.getConfig().get("SendPlayerData.enabled"),player);
					}
				}else
				//
				if(command.equals("creeperradius") && sender.isOp()){
					if(args.length >= 2){
						if(args[1].toLowerCase().equals("true")){
							plugin.getConfig().set("CreeperRadius.enabled", true);
							sendMessage("Creeper Radius has been enabled.", player);
						}else if (args[1].toLowerCase().equals("false")) {
							plugin.getConfig().set("CreeperRadius.enabled", false);
							sendMessage("Creeper Radius has been disabled.", player);
						}else{
							try {
								plugin.getConfig().set("CreeperRadius.radius", Double.parseDouble(args[1]));
								sendMessage("Creeper Radius set to " + Double.parseDouble(args[1]), player);
							} catch (NumberFormatException e) {
								sendMessage("Must use numbers ex: 1.75", player);
							}
						}
					}else{
						sendMessage("Creeper Radius: " + plugin.getConfig().get("CreeperRadius.enabled") + " Radius set to: " + plugin.getConfig().getDouble("CreeperRadius.radius"),player);
					}
				}else
				//
				if (command.equals("waittime") && sender.isOp()) {
					if (args.length >= 2) {
						try {
							plugin.getConfig().set("vive-only.waittime", Integer.parseInt(args[1]));
							sendMessage("waittime set to " + Integer.parseInt(args[1]), player);
						} catch (NumberFormatException e) {
							sendMessage("Must use numbers", player);
						}
					} else {
						sendMessage("waittime: " + plugin.getConfig().get("vive-only.waittime"), player);
					}
					plugin.saveConfig();
				} else
				if (command.equals("bow") && sender.isOp()) {
					if (args.length >= 2) {
						try {
							plugin.getConfig().set("bow.multiplier", Integer.parseInt(args[1]));
							sendMessage("Multiplier set to " + Integer.parseInt(args[1]), player);
						} catch (NumberFormatException e) {
							sendMessage("Must use numbers", player);
						}
					} else {
						sendMessage("Multiplier: " + plugin.getConfig().get("bow.multiplier"), player);
					}
					plugin.saveConfig();
				} else
				//
				if (command.equals("version")) {
					PluginDescriptionFile pdf = plugin.getDescription();
					String version = pdf.getVersion();
					sendMessage("Version: " + version, player);
				} else
				//	
				if (command.equals("help")) {
					for (Cmd cm : commands) {
						sendMessage(cm.getCommand() + " - " + cm.getDescription(), player);
					}
				} else {
					if(!sender.isOp()){
						sendMessage("You must be OP to use these commands", player);
					}else
					sendMessage("Unknown command", player);
				}
			}else{
				sendMessage("Download Vivecraft at http://www.vivecraft.org/ type '/vive help' to list options",player);
			}
		} else if (!(sender instanceof Player))
			plugin.getLogger().info("Only players can run this command!");
		return true;
	}

	public static void sendMessage(String message, Player p) {
		p.sendMessage(ChatColor.BLUE + "[" + ChatColor.GRAY + "Vivecraft" + ChatColor.BLUE + "] " + ChatColor.WHITE + message);
	}

}

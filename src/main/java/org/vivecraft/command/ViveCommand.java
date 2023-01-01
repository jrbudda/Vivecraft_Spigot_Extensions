package org.vivecraft.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;


public class ViveCommand implements CommandExecutor {

	private VSE plugin;
	private static ArrayList<Cmd> commands = new ArrayList<Cmd>();

	public ViveCommand(VSE vse) {
		plugin = vse;
		commands.add(new Cmd("vive-only", "Set to true to only allow Vivecraft players to play. Default: false","Example: /vse vive-only true"));
		commands.add(new Cmd("waittime","Ticks to wait before kicking a player. The player's client must send a Vivecraft VERSION info in that time. Default: 60","Example: /vse waittime 60"));
		commands.add(new Cmd("version", "returns the version of the plugin.","Example: /vse version"));
		commands.add(new Cmd("sendplayerdata", "set to false to disable sending player to data to clients. Default: true","Example: /vse sendplayerdata true"));
		commands.add(new Cmd("creeperradius", "type false to disable or type a number to change the radius. Default: 1.75","Example: /vse creeperradius 1.75"));
		commands.add(new Cmd("bow", "Sets the multiplier for bow damage of vive users. Default: 2","Example: /vse bow 2"));
		commands.add(new Cmd("checkforupdate", "Checked for an update every time an OP joins the server","Example: /vse checkforupdate true"));
		commands.add(new Cmd("set", "Allows Editing the plugin config ingame. May need to restart server to take effect.","Example: /vse set general.vive-only true"));
		commands.add(new Cmd("list", "Lists all the users using Vivecraft.","Example: /vse list"));
	}
	
	public static ArrayList<Cmd> getCommands(){
		return commands;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length == 0) {
			sendMessage("Download Vivecraft at http://www.vivecraft.org/ type '/vive help' to list options",sender);
			return true;
		}

		String command = args[0].toLowerCase();

		if (command.equals("vive-only") && sender.isOp()) {
			if (args.length >= 2) {
				if (args[1].toLowerCase().equals("true")) {
					plugin.getConfig().set("vive-only.enabled", true);
					sendMessage("Vive-Only has been enabled.", sender);

				} else if (args[1].toLowerCase().equals("false")) {
					plugin.getConfig().set("vive-only.enabled", false);
					sendMessage("Vive-Only has been disabled.", sender);
				}
			} else {
				sendMessage("Vive-Only: " + plugin.getConfig().get("vive-only.enabled"), sender);
			}
			plugin.saveConfig();
		} 
		else if(command.equals("sendplayerdata") && sender.isOp()){
			if(args.length >= 2){
				if(args[1].toLowerCase().equals("true")){
					plugin.getConfig().set("SendPlayerData.enabled", true);
					sendMessage("SendPlayerData has been enabled.", sender);
				}else if (args[1].toLowerCase().equals("false")) {
					plugin.getConfig().set("SendPlayerData.enabled", false);
					sendMessage("SendPlayerData has been disabled.", sender);
				}
			}else{
				sendMessage("SendPlayerData: " + plugin.getConfig().get("SendPlayerData.enabled"),sender);
			}
		}
		else if(command.equals("creeperradius") && sender.isOp()){
			if(args.length >= 2){
				if(args[1].toLowerCase().equals("true")){
					plugin.getConfig().set("CreeperRadius.enabled", true);
					sendMessage("Creeper Radius has been enabled.", sender);
				}else if (args[1].toLowerCase().equals("false")) {
					plugin.getConfig().set("CreeperRadius.enabled", false);
					sendMessage("Creeper Radius has been disabled.", sender);
				}else{
					try {
						plugin.getConfig().set("CreeperRadius.radius", Double.parseDouble(args[1]));
						sendMessage("Creeper Radius set to " + Double.parseDouble(args[1]), sender);
					} catch (NumberFormatException e) {
						sendMessage("Must use numbers ex: 1.75", sender);
					}
				}
			}else{
				sendMessage("Creeper Radius: " + plugin.getConfig().get("CreeperRadius.enabled") + " Radius set to: " + plugin.getConfig().getDouble("CreeperRadius.radius"),sender);
			}
		}
		else if (command.equals("waittime") && sender.isOp()) {
			if (args.length >= 2) {
				try {
					plugin.getConfig().set("vive-only.waittime", Integer.parseInt(args[1]));
					sendMessage("waittime set to " + Integer.parseInt(args[1]), sender);
				} catch (NumberFormatException e) {
					sendMessage("Must use numbers", sender);
				}
			} else {
				sendMessage("waittime: " + plugin.getConfig().get("vive-only.waittime"), sender);
			}
			plugin.saveConfig();
		} 
		else if (command.equals("bow") && sender.isOp()) {
			if (args.length >= 2) {
				try {
					plugin.getConfig().set("bow.multiplier", Integer.parseInt(args[1]));
					sendMessage("Multiplier set to " + Integer.parseInt(args[1]), sender);
				} catch (NumberFormatException e) {
					sendMessage("Must use numbers", sender);
				}
			} else {
				sendMessage("Multiplier: " + plugin.getConfig().get("bow.multiplier"), sender);
			}
			plugin.saveConfig();
		} 
		else if(command.equals("list")){
			Iterator it = VSE.vivePlayers.entrySet().iterator();
			int size = VSE.vivePlayers.size();
			int total = plugin.getServer().getOnlinePlayers().size();		
			sendMessage("There are " + total +" players online", sender);

			if(size > 1){
				sendMessage("There are " + size + " Vivecraft Players",sender);
			}else if(size ==1){
				sendMessage("There is 1 Vivecraft Player",sender);
			}else{
				sendMessage("There are no Vivecraft players",sender);
			}

			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				VivePlayer vp = (VivePlayer)pair.getValue();
				sendMessage(vp.player.getDisplayName() + ": " + (vp.isVR() ? "VR " + (vp.isSeated() ? "SEATED" : "STANDING") : "NONVR") ,sender);
			}

		}
		else if(command.equals("set") && sender.isOp()){
			if (args.length >= 3) {
				String config = args[1];
				if(plugin.getConfig().get(config) != null){
					if(isBoolean(args[2])){
						plugin.getConfig().set(config, Boolean.parseBoolean(args[2]));
					}else
						if(isDouble(args[2])){
							plugin.getConfig().set(config, Double.parseDouble(args[2]));
						}else{
							String test = "";
							if(args.length > 3){
								for(int i = 2; i < args.length; i++){
									test = test + args[i];
									if(i < args.length-1){
										test = test + " ";
									}
								}
							}
							plugin.getConfig().set(config, test);
						}
					sendMessage(config + " has been set to: " + plugin.getConfig().get(config),sender);
				}else{
					sendMessage("That config option does not exist.",sender);
				}
			}else{
				sendMessage("Missing arguments!",sender);
			}
		}
		else if (command.equals("version")) {
			PluginDescriptionFile pdf = plugin.getDescription();
			String version = pdf.getVersion();
			sendMessage("Version: " + version, sender);
		} 
		else if(command.equals("checkforupdate")){
			if(args.length >= 2){
				if(args[1].toLowerCase().equals("true")){
					plugin.getConfig().set("checkforupdate.enabled", true);
					sendMessage("Update checker has been enabled.", sender);
				}else if (args[1].toLowerCase().equals("false")) {
					plugin.getConfig().set("checkforupdate.enabled", false);
					sendMessage("Update checker has been disabled.", sender);
				}
			}else{
				sendMessage("Check for update: " + plugin.getConfig().get("checkforupdate.enabled"),sender);
			}
		}
		else if (command.equals("help")) {
			sender.sendMessage(ChatColor.BLUE + "-------------- " + ChatColor.GRAY + "VSE Commands" + ChatColor.BLUE + " --------------");
			for (Cmd cm : commands) {

			//	TextComponent tc = new TextComponent();
			//	tc.setText(ChatColor.BLUE + cm.getCommand() + ": " + ChatColor.WHITE + cm.getDescription());
			//	tc.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(cm.getHoverText()).color(ChatColor.BLUE).create()));
				sender.sendMessage(ChatColor.BLUE + cm.getCommand() + ": " + ChatColor.WHITE + cm.getDescription());
			}
		} else {
			if(!sender.isOp()){
				sendMessage("You must be OP to use these commands", sender);
			}else
				sendMessage("Unknown command", sender);
		}


		if(sender.isOp()) plugin.saveConfig();

		return true;
	}
	
	public boolean isBoolean(String str){
		return (str.toLowerCase().equals("true") || str.toLowerCase().equals("false"));
	}
	
	boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
	
	public static void sendMessage(String message, CommandSender p) {
		p.sendMessage(ChatColor.BLUE + "[" + ChatColor.GRAY + "Vivecraft" + ChatColor.BLUE + "] " + ChatColor.WHITE + message);
	}

}

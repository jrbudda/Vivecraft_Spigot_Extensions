package org.vivecraft.command;

import java.util.ArrayList;
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
		commands.add(new Cmd("vive-only", "Set to true to only allow Vivecraft players to play."));
		commands.add(new Cmd("waittime",
				"Ticks to wait before kicking a player. The player's client must send a Vivecraft VERSION info in that time."));
		commands.add(new Cmd("version", "returns the version of the plugin."));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player){// || sender.hasPermission("SpigotVive.vive")) {
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
					sendMessage("Unknown command", player);
				}
			}else{
				sendMessage("Download Vivecraft at http://www.vivecraft.org/",player);
			}
		} else if (!(sender instanceof Player))
			plugin.getLogger().info("Only players can run this command!");
		return true;
	}

	public static void sendMessage(String message, Player p) {
		p.sendMessage("[Vivecraft] " + message);
	}

}

package org.vivecraft.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ViveCommand extends JavaPlugin  implements CommandExecutor{
	@Override 
	public void onEnable(){

		this.getLogger().info("The Vive Commands Have been Enabled!");

	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player || sender.hasPermission("SpigotVive.vive")) {
			Player player = (Player) sender;
			player.sendMessage(ChatColor.DARK_PURPLE + "This command its still in make!");

		}

		this.getLogger().info("Only players can run this command!");
		return true;
	}

}

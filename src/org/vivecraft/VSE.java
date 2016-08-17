package org.vivecraft;

import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import org.vivecraft.listeners.VivecraftNetworkListener;


public class VSE extends JavaPlugin {

	
@Override
public void onEnable() {
	super.onEnable();
	
getServer().getMessenger().registerIncomingPluginChannel(this, "Vivecraft", new VivecraftNetworkListener());
getServer().getMessenger().registerOutgoingPluginChannel(this, "Vivecraft");

	
SpigotConfig.movedWronglyThreshold = 10;
SpigotConfig.movedTooQuicklyMultiplier = 64;


}
	
}

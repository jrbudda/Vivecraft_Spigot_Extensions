package org.vivecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import org.vivecraft.listeners.VivecraftNetworkListener;


public class VSE extends JavaPlugin {

public final String CHANNEL = "Vivecraft";
	
public Map<UUID, VivePlayer> vivePlayers = new HashMap<UUID, VivePlayer>(); 

@Override
public void onEnable() {
	super.onEnable();

	getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new VivecraftNetworkListener());
	getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);


	SpigotConfig.movedWronglyThreshold = 10;
	SpigotConfig.movedTooQuicklyMultiplier = 64;

	
	
}
	


}

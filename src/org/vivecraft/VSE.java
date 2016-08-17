package org.vivecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import org.vivecraft.listeners.VivecraftNetworkListener;


public class VSE extends JavaPlugin implements Listener{

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

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		vivePlayers.remove(event.getPlayer().getUniqueId());
	}



}

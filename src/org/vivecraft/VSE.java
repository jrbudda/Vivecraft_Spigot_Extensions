package org.vivecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import org.vivecraft.command.ViveCommand;
import org.vivecraft.listeners.VivecraftCombatListener;
import org.vivecraft.listeners.VivecraftNetworkListener;

public class VSE extends JavaPlugin implements Listener {
	FileConfiguration config = getConfig();

	public final String CHANNEL = "Vivecraft";

	public Map<UUID, VivePlayer> vivePlayers = new HashMap<UUID, VivePlayer>();

	int task = 0;

	@Override
	public void onEnable() {
		super.onEnable();

		// Config Part
		config.options().copyDefaults(true);
		saveDefaultConfig();
		saveConfig();
		// end Config part

		this.getCommand("vive").setExecutor(new ViveCommand(this));
		getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new VivecraftNetworkListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
		
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new VivecraftCombatListener(this), this);

		try {
			SpigotConfig.movedWronglyThreshold = 10;
			SpigotConfig.movedTooQuicklyMultiplier = 64;
		} catch (NoSuchFieldError e) {
			getLogger().warning("You are running this on an older version of spigot/bukkit Please set the value of movedWronglyThreshold to 10 and set the value of movedTooQuicklyMultiplier to 64");
			//.printStackTrace();
		}

		task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				sendPosData();
			}
		}, 20, 1);

	}

	public void sendPosData() {

		for (VivePlayer sendTo : vivePlayers.values()) {

			if (sendTo == null || sendTo.player == null || !sendTo.player.isOnline())
				continue; // dunno y but just in case.

			for (VivePlayer v : vivePlayers.values()) {
			
					if (v == sendTo || v == null || v.player == null || !v.player.isOnline() || v.hmdData == null || v.controller0data == null || v.controller1data == null)
						continue;
					
					double d = sendTo.player.getLocation().distanceSquared(v.player.getLocation());
	
					if (d < 256 * 256) {
						// TODO: optional distance value?
						sendTo.player.sendPluginMessage(this, CHANNEL, v.getUberPacket());
				}
			}
		}
	}
	
	@Override
	public void onDisable() {
		saveConfig();
		getServer().getScheduler().cancelTask(task);
		super.onDisable();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		vivePlayers.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent event) {
		final Player p = event.getPlayer();

		if (getConfig().getBoolean("debug.enabled")) {
			getLogger().info(p.getName() + " Has joined the server");
		}
		if (getConfig().getBoolean("vive-only.enabled")) {
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					if (VSE.this.getConfig().getBoolean("debug.enabled")) {
						VSE.this.getLogger().info("Checking player for ViveCraft");
					}
					if ((p.isOnline()) && (!isVive(p))) {
						VSE.this.getLogger().info(p.getName() + " " + "got kicked for not using the Vive Mod");
						p.kickPlayer(VSE.this.getConfig().getString("vive-only.kickmessage"));
					}
				}
			}, getConfig().getInt("vive-only.waittime"));
		}
	}
	
	public boolean isVive(Player p){
		if(p == null) return false;
		return vivePlayers.containsKey(p.getUniqueId());
	}

	public void setPermissionsGroup(Player p) {

		Map<String, Boolean> groups = new HashMap<String, Boolean>();

		boolean isvive = isVive(p);

		String g_vive = getConfig().getString("permissions.vivegroup");
		String g_classic = getConfig().getString("permissions.non-vivegroup");
		if (g_vive != null)
			groups.put(g_vive, isvive);
		if (g_classic != null)
			groups.put(g_classic, !isvive);

		if (isvive) {
			String g_freemove = getConfig().getString("permissions.freemovegroup");
			if (g_freemove != null)
				groups.put(g_freemove, !vivePlayers.get(p.getUniqueId()).isTeleportMode);
		}

		updatePlayerPermissionGroup(p, groups);

	}

	public void updatePlayerPermissionGroup(Player p, Map<String, Boolean> groups) {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		Permission perm = rsp.getProvider();
		if (perm != null) {
			for (Map.Entry<String, Boolean> entry : groups.entrySet()) {
				if (entry.getValue()) {
					if (!perm.playerInGroup(p, entry.getKey()))
						perm.playerAddGroup(p, entry.getKey());
				} else {
					if (perm.playerInGroup(p, entry.getKey()))
						perm.playerRemoveGroup(p, entry.getKey());
				}
			}
		}
	}
}

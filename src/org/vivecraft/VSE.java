package org.vivecraft;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftCreeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_11_R1.EntityCreeper;
import net.minecraft.server.v1_11_R1.PathfinderGoalSelector;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import org.vivecraft.command.ViveCommand;
import org.vivecraft.entities.CustomGoalSwell;
import org.vivecraft.listeners.VivecraftCombatListener;
import org.vivecraft.listeners.VivecraftItemListener;
import org.vivecraft.listeners.VivecraftNetworkListener;

public class VSE extends JavaPlugin implements Listener {
	FileConfiguration config = getConfig();

	public final String CHANNEL = "Vivecraft";

	public static Map<UUID, VivePlayer> vivePlayers = new HashMap<UUID, VivePlayer>();

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
		this.getCommand("vse").setExecutor(new ViveCommand(this));
		getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new VivecraftNetworkListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
		
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new VivecraftCombatListener(this), this);
		getServer().getPluginManager().registerEvents(new VivecraftItemListener(), this);
		
		SpigotConfig.movedWronglyThreshold = 10;
		SpigotConfig.movedTooQuicklyMultiplier = 64;

		task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				sendPosData();
			}
		}, 20, 1);
		
		CheckForCreeper();

	}
		
	public static Object getPrivateField(String fieldName, Class<PathfinderGoalSelector> clazz, Object object)
	{
	Field field;
	Object o = null;
	try
	{
		field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		o = field.get(object);
	}
	catch(NoSuchFieldException e)
	{
		e.printStackTrace();
	}
	catch(IllegalAccessException e)
	{
		e.printStackTrace();
	}
	return o;
	}
	  
	@EventHandler
    public void onEvent(CreatureSpawnEvent event) {
		EditCreeper(event.getEntity());
	}
	
	public void CheckForCreeper(){
		List<World> wrl = this.getServer().getWorlds();
		for(World world: wrl){
			for(Entity e: world.getLivingEntities()){
				if(e.getType() == EntityType.CREEPER){
					EditCreeper(e);
				}
			}
		}
	}
	
	public void EditCreeper(Entity entity){
		if(entity.getType() == EntityType.CREEPER){	
			EntityCreeper e = ((CraftCreeper) entity).getHandle();
			@SuppressWarnings("rawtypes")
			LinkedHashSet goalB = (LinkedHashSet )getPrivateField("b", PathfinderGoalSelector.class, e.goalSelector);
			int x = 0;
			for(Object b: goalB){
				if(x==1){
					goalB.remove(b);
					break;
				}
				x+=1;
			}
		    e.goalSelector.a(2, new CustomGoalSwell(e));
		}
	}

	public void sendPosData() {

		for (VivePlayer sendTo : vivePlayers.values()) {

			if (sendTo == null || sendTo.player == null || !sendTo.player.isOnline())
				continue; // dunno y but just in case.

			for (VivePlayer v : vivePlayers.values()) {
			
					if (v == sendTo || v == null || v.player == null || !v.player.isOnline() || v.hmdData == null || v.controller0data == null || v.controller1data == null){
						continue;
					}
					
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
	
	public static boolean isVive(Player p){
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

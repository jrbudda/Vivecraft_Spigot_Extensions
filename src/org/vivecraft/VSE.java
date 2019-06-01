package org.vivecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEnderman;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;
import org.spigotmc.SpigotConfig;
import org.vivecraft.command.ConstructTabCompleter;
import org.vivecraft.command.ViveCommand;
import org.vivecraft.entities.CustomGoalSwell;
import org.vivecraft.entities.CustomPathFinderGoalPlayerWhoLookedAtTarget;
import org.vivecraft.listeners.VivecraftCombatListener;
import org.vivecraft.listeners.VivecraftItemListener;
import org.vivecraft.listeners.VivecraftNetworkListener;
import org.vivecraft.utils.Headshot;

import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_13_R2.EntityCreeper;
import net.minecraft.server.v1_13_R2.EntityEnderman;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.PathfinderGoalSelector;

public class VSE extends JavaPlugin implements Listener {
	FileConfiguration config = getConfig();

	public final String CHANNEL = "vivecraft:data";

	public static Map<UUID, VivePlayer> vivePlayers = new HashMap<UUID, VivePlayer>();
	public static VSE me;
	
	int task = 0;
	private String readurl = "https://raw.githubusercontent.com/jrbudda/Vivecraft_Spigot_Extensions/1.13/version.txt";
	
	public List<String> blocklist = new ArrayList<>();
	
	public boolean debug = false;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		super.onEnable();
		
		me = this;
		
		ItemStack is = new ItemStack(Material.LEATHER_BOOTS);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("Jump Boots");
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is.setItemMeta(meta);
		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "jump_boots"),is);
		recipe.shape("B", "S");
		recipe.setIngredient('B', Material.LEATHER_BOOTS);
		recipe.setIngredient('S', Material.SLIME_BLOCK);
		Bukkit.addRecipe(recipe);
		
		ItemStack is2 = new ItemStack(Material.SHEARS);
		ItemMeta meta2 = is2.getItemMeta();
		meta2.setDisplayName("Climb Claws");
		meta2.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is2.setItemMeta(meta2);
		ShapedRecipe recipe2 = new ShapedRecipe( new NamespacedKey(this, "climb_claws"), is2);
		recipe2.shape("E E", "S S");
		recipe2.setIngredient('E', Material.SPIDER_EYE);
		recipe2.setIngredient('S', Material.SHEARS);
		Bukkit.addRecipe(recipe2);
		
		try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        // Failed to submit the stats :-(
	    }
		
		// Config Part
		config.options().copyDefaults(true);
		saveDefaultConfig();
		saveConfig();
		saveResource("config-instructions.yml", true);
		ConfigurationSection sec = config.getConfigurationSection("climbey");
		
		if(sec!=null){
			List<String> temp = sec.getStringList("blocklist");
			//make an attempt to validate these on the server for debugging.
			if(temp != null){
				for (String string : temp) {
					if (IRegistry.BLOCK.get(new MinecraftKey(string)) == null) {
						getLogger().warning("Unknown climbey block name: " + string);
						continue;
					}

					blocklist.add(string);
				}
			}
		}
					
		// end Config part

		getCommand("vse").setExecutor(new ViveCommand(this));
		getCommand("vse").setTabCompleter(new ConstructTabCompleter());

		getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new VivecraftNetworkListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
		
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new VivecraftCombatListener(this), this);
		getServer().getPluginManager().registerEvents(new VivecraftItemListener(this), this);

        Headshot.init(this);
        
        if(getConfig().getBoolean("setSpigotConfig.enabled")){
        	SpigotConfig.movedWronglyThreshold = getConfig().getDouble("setSpigotConfig.movedWronglyThreshold");
			SpigotConfig.movedTooQuicklyMultiplier = getConfig().getDouble("setSpigotConfig.movedTooQuickly");
        }
        
		debug = (getConfig().getBoolean("general.debug", false));

        
		task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				sendPosData();
			}
		}, 20, 1);
		
		//check for any creepers and modify the fuse radius
		CheckAllEntities();
		
		vault = true;
		if(getServer().getPluginManager().getPlugin("Vault") == null || getServer().getPluginManager().getPlugin("Vault").isEnabled() == false) {
			getLogger().severe("Vault not found, permissions groups will not be set");
			vault = false;
		}	
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new BukkitRunnable() {
			@Override
			public void run() {
				startUpdateCheck();
			}
		}, 1);
		
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method getPrivateMethod(String methodName, Class clazz, Class param)
	{
		Method m = null;
		try
		{
			if(param == null) {
				m = clazz.getDeclaredMethod(methodName);
			} else {
				m = clazz.getDeclaredMethod(methodName, param);
			}
			m.setAccessible(true);
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		return m;
	}
	
	public static Object invoke(Method m, Object object, Object param) {
		try {
			if(param == null) 
				return  m.invoke(object);
			else
				return  m.invoke(object, param);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(CreatureSpawnEvent event) {
		if(!event.isCancelled()){
			EditEntity(event.getEntity());
		}
	}
	
	public void CheckAllEntities(){
		List<World> wrl = this.getServer().getWorlds();
		for(World world: wrl){
			for(Entity e: world.getLivingEntities()){
					EditEntity(e);
			}
		}
	}
	
	public void EditEntity(Entity entity){
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
		else if(entity.getType() == EntityType.ENDERMAN && ((CraftEntity)entity).getHandle() instanceof EntityEnderman){			
			EntityEnderman e = ((CraftEnderman) entity).getHandle();
			@SuppressWarnings("rawtypes")
			LinkedHashSet goalB = (LinkedHashSet )getPrivateField("b", PathfinderGoalSelector.class, e.targetSelector);
			int x = 0;
			for(Object b: goalB){
				if(x==0){
					goalB.remove(b);
					break;
				}
				x+=1;
			}
			e.targetSelector.a(1, new CustomPathFinderGoalPlayerWhoLookedAtTarget(e));
		}
	}

	public void sendPosData() {

		for (VivePlayer sendTo : vivePlayers.values()) {

			if (sendTo == null || sendTo.player == null || !sendTo.player.isOnline())
				continue; // dunno y but just in case.

			for (VivePlayer v : vivePlayers.values()) {
			
					if (v == sendTo || v == null || v.player == null || !v.player.isOnline() || v.player.getWorld() != sendTo.player.getWorld() || v.hmdData == null || v.controller0data == null || v.controller1data == null){
						continue;
					}
					
					double d = sendTo.player.getLocation().distanceSquared(v.player.getLocation());
	
					if (d < 256 * 256) {
						sendTo.player.sendPluginMessage(this, CHANNEL, v.getUberPacket());
				}
			}
		}
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(task);
		super.onDisable();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		vivePlayers.remove(event.getPlayer().getUniqueId());
		
		if(getConfig().getBoolean("welcomemsg.enabled"))
			broadcastConfigString("welcomemsg.leaveMessage", event.getPlayer().getDisplayName());
	}

	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent event) {
		final Player p = event.getPlayer();

		if (debug) getLogger().info(p.getName() + " Has joined the server");
			
		int t = getConfig().getInt("general.vive-only-kickwaittime",100);
		if(t < 100) t = 100;
		if(t > 1000) t = 1000;
		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
				if (debug) 
					VSE.this.getLogger().info("Checking player for ViveCraft");
				
				
				if (getConfig().getBoolean("general.vive-only")) {
					if ((p.isOnline()) && (!isVive(p))) {
						VSE.this.getLogger().info(p.getName() + " " + "got kicked for not using Vivecraft");
						p.kickPlayer(VSE.this.getConfig().getString("general.vive-only-kickmessage"));
					}		
				}
				
				if (p.isOnline()) {
					sendWelcomeMessage(p);
					setPermissionsGroup(p);
				}	 else {
					if (debug) 
						VSE.this.getLogger().info(p.getName() + " no longer online! ");
				}		
			}
		}, t);
	}
		
	public void startUpdateCheck() {
		PluginDescriptionFile pdf = getDescription();
		String version = pdf.getVersion();
		getLogger().info("Version: " + version);
		if (getConfig().getBoolean("general.checkforupdate", true)) {
			try {
				getLogger().info("Checking for update...");
				URL url = new URL(readurl);
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				String str;
				String updatemsg = null;
				while ((str = br.readLine()) != null) {
					String line = str;
					String[] bits = line.split(":");
					if(bits[0].trim().equalsIgnoreCase(version)){
						updatemsg = bits[1].trim();
						getLogger().info(updatemsg);
						//ViveCommand.sendMessage(updatemsg, p);
						break;
					}
				}
				br.close();
				if(updatemsg == null){
					getLogger().info("Version not found. Are you from the future?");
				}
			} catch (IOException e) {
				getLogger().severe("Error retrieving version list: " + e.getMessage());
			}
		}
	}
	
	public static boolean isVive(Player p){
		if(p == null) return false;
			if(vivePlayers.containsKey(p.getUniqueId())){
				return vivePlayers.get(p.getUniqueId()).isVR();
			}
		return false;
	}

	public void setPermissionsGroup(Player p) {
		if(!vault) return;	
		if(!getConfig().getBoolean("permissions.enabled")) return;
		
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

	public boolean vault;
	
	public void updatePlayerPermissionGroup(Player p, Map<String, Boolean> groups) {
		try {	
				
			RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
			Permission perm = rsp.getProvider();
			if (perm != null) {
				for (Map.Entry<String, Boolean> entry : groups.entrySet()) {
					if (entry.getValue()) {
						if (!perm.playerInGroup(p, entry.getKey())) {
							if (debug) 
								VSE.this.getLogger().info("Adding " + p.getName() + " to " + entry.getKey());
							perm.playerAddGroup(p, entry.getKey());
						}
					} else {
						if (perm.playerInGroup(p, entry.getKey())) {
							if (debug) 
								VSE.this.getLogger().info("Removing " + p.getName() + " from " + entry.getKey());
							perm.playerRemoveGroup(p, entry.getKey());
						}
					}
				}
			} else {
				VSE.this.getLogger().info("Permissions error: Registered permissions provider is null!");
			}
			
		} catch (Exception e) {
			getLogger().severe("Could not set player permission group: " + e.getMessage());
		}
	}
	
	boolean tryParseInt(String value) {  
	     try {  
	         Integer.parseInt(value);  
	         return true;  
	      } catch (NumberFormatException e) {  
	         return false;  
	      }  
	}
	
	public void broadcastConfigString(String node, String playername){
		String message = this.getConfig().getString(node);
		if(message == null || message.isEmpty()) return;
		String format = message.replace("&player", playername);
		for(Player p : Bukkit.getOnlinePlayers()){
			ViveCommand.sendMessage(format,p);
		}
	}

	
	public void sendWelcomeMessage(Player p){
		if(!getConfig().getBoolean("welcomemsg.enabled"))return;

		VivePlayer vp = VSE.vivePlayers.get(p.getUniqueId());
		
		if(vp==null){
			broadcastConfigString("welcomemsg.welcomeVanilla", p.getDisplayName());
		} else {
			if(vp.isSeated())
				broadcastConfigString("welcomemsg.welcomeSeated", p.getDisplayName());
			else if (!vp.isVR())
				broadcastConfigString("welcomemsg.welcomenonVR", p.getDisplayName());
			else
				broadcastConfigString("welcomemsg.welcomeVR", p.getDisplayName());
		}
	}
	
}
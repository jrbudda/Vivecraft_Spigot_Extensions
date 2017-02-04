package org.vivecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEnderman;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import net.milkbowl.vault.item.Items;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_11_R1.CreativeModeTab;
import net.minecraft.server.v1_11_R1.EntityCreeper;
import net.minecraft.server.v1_11_R1.EntityEnderman;
import net.minecraft.server.v1_11_R1.EnumItemSlot;
import net.minecraft.server.v1_11_R1.Item;
import net.minecraft.server.v1_11_R1.ItemArmor;
import net.minecraft.server.v1_11_R1.ItemArmor.EnumArmorMaterial;
import net.minecraft.server.v1_11_R1.ItemShears;
import net.minecraft.server.v1_11_R1.MinecraftKey;
import net.minecraft.server.v1_11_R1.PathfinderGoalSelector;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
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
import org.vivecraft.utils.ItemVivecraft;

public class VSE extends JavaPlugin implements Listener {
	FileConfiguration config = getConfig();

	public final String CHANNEL = "Vivecraft";

	public static Map<UUID, VivePlayer> vivePlayers = new HashMap<UUID, VivePlayer>();

	int task = 0;
	private String readurl = "https://raw.githubusercontent.com/jaron780/Vivecraft_Spigot_Extensions/master/version.txt";
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		ItemStack is = new ItemStack(Material.LEATHER_BOOTS);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("Jump Boots");
		meta.setUnbreakable(true);
		is.setItemMeta(meta);
		ShapedRecipe recipe = new ShapedRecipe(is);
		recipe.shape("   ", " B ", " S ");
		recipe.setIngredient('B', Material.LEATHER_BOOTS);
		recipe.setIngredient('S', Material.SLIME_BLOCK);
		Bukkit.addRecipe(recipe);
		
		ItemStack is2 = new ItemStack(Material.SHEARS);
		ItemMeta meta2 = is2.getItemMeta();
		meta2.setDisplayName("Climb Claws");
		meta2.setUnbreakable(true);
		is2.setItemMeta(meta2);
		ShapedRecipe recipe2 = new ShapedRecipe(is2);
		recipe2.shape("   ", "E E", "S S");
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
		// end Config part

		getCommand("vive").setExecutor(new ViveCommand(this));
		getCommand("vse").setExecutor(new ViveCommand(this));
		getCommand("vive").setTabCompleter(new ConstructTabCompleter());
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
        
		task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				sendPosData();
			}
		}, 20, 1);
		
		//check for any creepers and modify the fuse radius
		CheckAllEntities();
		
		//Easter egg
		if(getConfig().getBoolean("general.printmoney")){
			getLogger().warning("\r\n||====================================================================||\r\n||//$\\\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\//$\\\\||\r\n||(100)==================| FEDERAL RESERVE NOTE |================(100)||\r\n||\\\\$//        ~         '------========--------'                \\\\$//||\r\n||<< /        /$\\              // ____ \\\\                         \\ >>||\r\n||>>|  12    //L\\\\            // ///..) \\\\         L38036133B   12 |<<||\r\n||<<|        \\\\ //           || <||  >\\  ||                        |>>||\r\n||>>|         \\$/            ||  $$ --/  ||        One Hundred     |<<||\r\n||<<|      L38036133B        *\\\\  |\\_/  //* series                 |>>||\r\n||>>|  12                     *\\\\/___\\_//*   1989                  |<<||\r\n||<<\\      Treasurer     ______/Franklin\\________     Secretary 12 />>||\r\n||//$\\                 ~|UNITED STATES OF AMERICA|~               /$\\\\||\r\n||(100)===================  ONE HUNDRED DOLLARS =================(100)||\r\n||\\\\$//\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\\\$//||\r\n||====================================================================||");
		}
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
	public static Method getPrivateMethod(String methodName, Class clazz)
	{
		Method m = null;
		try
		{
			m = clazz.getDeclaredMethod(methodName);
			m.setAccessible(true);
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		return m;
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
		saveConfig();
		getServer().getScheduler().cancelTask(task);
		super.onDisable();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		vivePlayers.remove(event.getPlayer().getUniqueId());
		if(getConfig().getBoolean("welcomemsg.enabled")){
			String message = getConfig().getString("welcomemsg.leaveMessage");
			String format = message.replace("&player", event.getPlayer().getDisplayName());
			for(Player p : Bukkit.getOnlinePlayers()){
				ViveCommand.sendMessage(format,p);
			}
		}
	}

	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		
		if (getConfig().getBoolean("general.debug")) {
			getLogger().info(p.getName() + " Has joined the server");
		}
		if (getConfig().getBoolean("general.vive-only")) {
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					if (VSE.this.getConfig().getBoolean("general.debug")) {
						VSE.this.getLogger().info("Checking player for ViveCraft");
					}
					if ((p.isOnline()) && (!isVive(p))) {
						VSE.this.getLogger().info(p.getName() + " " + "got kicked for not using Vivecraft");
						p.kickPlayer(VSE.this.getConfig().getString("general.vive-only-kickmessage"));
					}
				}
			}, getConfig().getInt("general.vive-only-kickwaittime"));
		}
		if(p.isOp())
		startUpdateCheck(p);
	}
	
	public void startUpdateCheck(Player p) {
		PluginDescriptionFile pdf = getDescription();
		String version = pdf.getVersion();
		System.out.println("Version: " + version);
		if (getConfig().getBoolean("general.checkforupdate", true)) {
			try {
				getLogger().info("Checking for a update...");
				URL url = new URL(readurl);
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				String str;
				while ((str = br.readLine()) != null) {
					String line = str;
//					if (line.charAt(0) == 'R' && line.charAt(1) == '2') {
					if(line.toLowerCase().startsWith(version.toLowerCase())){
						String updatemsg = line.substring(version.length() + 2);
						getLogger().info(updatemsg);
						ViveCommand.sendMessage(updatemsg, p);
					}
				}
				br.close();
			} catch (IOException e) {
			getLogger().severe("The update URL is invalid! Please message Jaron780 on discord!");
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
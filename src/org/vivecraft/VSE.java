package org.vivecraft;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChannelEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import org.vivecraft.command.Vive;
import org.vivecraft.listeners.VivecraftNetworkListener;


public class VSE extends JavaPlugin implements Listener{

	public final String CHANNEL = "Vivecraft";

	public Map<UUID, VivePlayer> vivePlayers = new HashMap<UUID, VivePlayer>(); 

	@Override
	public void onEnable() {
		super.onEnable();
		
		createConfig();
		this.getCommand("vive").setExecutor(new Vive(this));
		getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new VivecraftNetworkListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
		

		SpigotConfig.movedWronglyThreshold = 10;
		SpigotConfig.movedTooQuicklyMultiplier = 64;
		
	}

	public class PlayerRegisterChannelEvent extends PlayerChannelEvent {

	    public PlayerRegisterChannelEvent(final Player player, final String channel) {
	    	super(player, channel);
	        getLogger().info("VIveCraft: "+ channel);
	    }
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		vivePlayers.remove(event.getPlayer().getUniqueId());
	}

    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent event) {
    	final Player p = event.getPlayer();

    	if(getConfig().getBoolean("vive-only.enabled")){
    		getLogger().info(p.getName() +" : Starting Runnable");
    		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
    			@Override
    			public void run() {
    				if (p.isOnline() && !vivePlayers.containsKey(p.getUniqueId())) {
    					p.kickPlayer(getConfig().getString("vive-only.kickmessage"));
    					getLogger().info(p.getName() + " " + "got kicked for not using the Vive Mod");
    				}
    			}
    		}, getConfig().getInt("vive-only.waittime"));
    	}
    }

    
    public void setPermissionsGroup(Player p){
    	
        Map<String, Boolean> groups = new HashMap<String, Boolean>();

        boolean isvive = vivePlayers.containsKey(p.getUniqueId());
        
        String g_vive = getConfig().getString("permissions.vivegroup");
        String g_classic = getConfig().getString("permissions.non-vivegroup");
        if (g_vive != null)
            groups.put(g_vive,isvive);
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
    
    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("ViveCraft Config not found, creating!");
                saveDefaultConfig();
                this.getConfig().set("vive-only.enabled", true);
                this.getConfig().set("vive-only.kickmessage", "You need to have the Vive Mod installed to join this server!");
                this.getConfig().set("vive-only.waittime", 20);
                this.getConfig().set("permissions.vivegroup", "vive.vivegroup");
                this.getConfig().set("permissions.non-vivegroup", "vive.non-vivegroup");
                this.getConfig().set("permissions.freemovegroup", "vive.freemovegroup");
                saveConfig();
            } else {
                getLogger().info("ViveCraft Config found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
     

}

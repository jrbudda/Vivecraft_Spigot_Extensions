package org.vivecraft.listeners;

import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.utils.Headshot;

import net.minecraft.server.v1_14_R1.Vec3D;


public class VivecraftCombatListener implements Listener{

	private VSE vse;

	public VivecraftCombatListener(VSE plugin){
		this.vse = plugin;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		//position all projectiles correctly.

		Projectile proj = event.getEntity();
		
		if (!(proj.getShooter() instanceof Player) || !VSE.isVive((Player) proj.getShooter()))
			return;

		Player pl = (Player)proj.getShooter();
		final VivePlayer vp = (VivePlayer)VSE.vivePlayers.get(pl.getUniqueId());

		final boolean arrow = proj instanceof AbstractArrow && !(proj instanceof Trident);

		if ((vp == null) && (this.vse.getConfig().getBoolean("general.debug"))) {
			vse.getLogger().warning(" Error on projectile launch!");
		}

		//this only works if the incoming speed is at max (based! on draw time)
		//TODO: properly scale in all cases.

		if(arrow && vp.getDraw() != 0) {
			proj.setVelocity(proj.getVelocity().multiply(vp.getDraw()));  
		}

		int hand = arrow ? 1 : 0;

		Vec3D aim = vp.getControllerDir(hand);

		if(arrow){
			aim = vp.getControllerDir(0);
			if(!vp.isSeated() && vp.getDraw() !=0){ //standing
				Vector m = (vp.getControllerPos(1).subtract(vp.getControllerPos(0))).toVector();
				m = m.normalize();
				aim = new Vec3D(m.getX(),  m.getY(), m.getZ());
			} else { //seated or roomscale off
				hand = 0;
			}
		}               

		Location pos = vp.getControllerPos(hand);
		Location loc = new Location(proj.getWorld(), pos.getX() + aim.x*0.6f, pos.getY()+aim.y*0.6f, pos.getZ()+aim.z*0.6f);
	
	//	loc.setPitch(-(float)Math.toDegrees(Math.asin(aim.y/aim.b())));
	//	loc.setYaw((float)Math.toDegrees(Math.atan2(aim.x, aim.z)));
		
		double velo = proj.getVelocity().length();	

		proj.setVelocity(new Vector(aim.x*velo, aim.y*velo, aim.z*velo));
		
		proj.teleport(loc);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onProjectileHit(EntityDamageByEntityEvent event) {
		
		if(event.getDamager() instanceof Trident) return;
		
		if (event.getDamager() instanceof Arrow && event.getEntity() instanceof LivingEntity) {
			final Arrow arrow = (Arrow) event.getDamager();
			LivingEntity target = (LivingEntity) event.getEntity();
			boolean headshot = Headshot.isHeadshot(target, arrow);

			if (!(arrow.getShooter() instanceof Player) || !VSE.isVive((Player) arrow.getShooter()))
				return;
			Player pl = (Player)arrow.getShooter();
			VivePlayer vp = (VivePlayer)VSE.vivePlayers.get(pl.getUniqueId());

			if(!vp.isSeated()){
				if(headshot){
					event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.standingheadshotmultiplier"));
				}else{
					event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.standingmultiplier"));
				}
			}else{
				if(headshot){
					event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.seatedheadshotmultiplier"));
				}else{
					event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.seatedmultiplier"));
				}
			}

		}
	}

	public boolean usingVR(Player player){
		if(VSE.vivePlayers.containsKey(player.getUniqueId())){
			if(!VSE.vivePlayers.get(player.getUniqueId()).isVR()){
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean isSeated(Player player){
		if(VSE.vivePlayers.containsKey(player.getUniqueId())){
			return VSE.vivePlayers.get(player.getUniqueId()).isSeated();
		}
		return false;
	}

	public boolean isStanding(Player player){
		if(VSE.vivePlayers.containsKey(player.getUniqueId())){
			if(!VSE.vivePlayers.get(player.getUniqueId()).isSeated() && VSE.vivePlayers.get(player.getUniqueId()).isVR()) return true;
		}
		return false;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onDamage(EntityDamageByEntityEvent e) {
		final Entity damager = e.getDamager();
		final Entity damaged = e.getEntity();

		if (damager instanceof Player) {
			if (damaged instanceof Player) {
				Player attacker = (Player) damager;
				Player victim = (Player) damager;

				if (!vse.getConfig().getBoolean("pvp.VRvsVR",true)) {
					if (usingVR(attacker) && usingVR(victim)) {
						if (isStanding(attacker) && isStanding(victim)) {
							e.setCancelled(true);
						}
					}
				}

				if (!vse.getConfig().getBoolean("pvp.VRvsNONVR", true)) {
					if ((usingVR(attacker) && !usingVR(victim)) || (usingVR(victim) && !usingVR(attacker))) {
						e.setCancelled(true);
					}
				}

				if (!vse.getConfig().getBoolean("pvp.SEATEDVRvsNONVR", true)) {
					if(((usingVR(attacker) && isSeated(attacker)) && !usingVR(victim)) || ((usingVR(victim) && isSeated(victim)) && !usingVR(attacker))){
						e.setCancelled(true);
					}
				}

				if (!vse.getConfig().getBoolean("pvp.VRvsSEATEDVR", true)) {
					if (usingVR(attacker) && usingVR(victim)) {
						if ((isSeated(attacker) && isStanding(victim)) || (isSeated(victim) && isStanding(attacker))) {
							e.setCancelled(true);
						}
					}
				}

			}
		}
	}

}
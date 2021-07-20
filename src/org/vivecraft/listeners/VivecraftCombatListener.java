package org.vivecraft.listeners;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;


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
		
		ServerPlayer nsme = ((CraftPlayer)pl).getHandle();
	
		Location pos = vp.getControllerPos(vp.activeHand);
		Vec3 aim = vp.getControllerDir(vp.activeHand);

		//this only works if the incoming speed is at max (based! on draw time)
		//TODO: properly scale in all cases.

		if(arrow && vp.getDraw() != 0) {
			proj.setVelocity(proj.getVelocity().multiply(vp.getDraw()));  	
			if(!vp.isSeated()){ //standing
		    	pos = vp.getControllerPos(0);
				Vector m = (vp.getControllerPos(1).subtract(vp.getControllerPos(0))).toVector();
				m = m.normalize();
				aim = new Vec3(m.getX(),  m.getY(), m.getZ());
			}
		}

		Location loc = new Location(proj.getWorld(), pos.getX() + aim.x*0.6f, pos.getY()+aim.y*0.6f, pos.getZ()+aim.z*0.6f);
		double velo = proj.getVelocity().length();		
		proj.teleport(loc); //paper sets velocity to 0 on teleport.
		proj.setVelocity(new Vector(aim.x*velo, aim.y*velo, aim.z*velo));
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

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onDamage(EntityDamageByEntityEvent e) {
		final Entity damager = e.getDamager();
		final Entity damaged = e.getEntity();

		if (damager instanceof Player) {
			if (damaged instanceof Player) {
				Player attacker = (Player) damager;
				Player victim = (Player) damager;

				if (!vse.getConfig().getBoolean("pvp.VRvsVR",true)) {
					if (VSE.isVive(attacker) && VSE.isVive(victim)) {
						if (VSE.isStanding(attacker) && VSE.isStanding(victim)) {
							e.setCancelled(true);
						}
					}
				}

				if (!vse.getConfig().getBoolean("pvp.VRvsNONVR", true)) {
					if ((VSE.isVive(attacker) && !VSE.isVive(victim)) || (VSE.isVive(victim) && !VSE.isVive(attacker))) {
						e.setCancelled(true);
					}
				}

				if (!vse.getConfig().getBoolean("pvp.SEATEDVRvsNONVR", true)) {
					if(((VSE.isVive(attacker) && VSE.isSeated(attacker)) && !VSE.isVive(victim)) || ((VSE.isVive(victim) && VSE.isSeated(victim)) && !VSE.isVive(attacker))){
						e.setCancelled(true);
					}
				}

				if (!vse.getConfig().getBoolean("pvp.VRvsSEATEDVR", true)) {
					if (VSE.isVive(attacker) && VSE.isVive(victim)) {
						if ((VSE.isSeated(attacker) && VSE.isStanding(victim)) || (VSE.isSeated(victim) && VSE.isStanding(attacker))) {
							e.setCancelled(true);
						}
					}
				}

			} 
			else if(damaged instanceof Fireball) {
				VivePlayer vp = (VivePlayer)VSE.vivePlayers.get(damager.getUniqueId());
				if(vp!=null && vp.isVR()) {
					Vec3 dir = vp.getHMDDir();
					//Interesting experiment. 
					//We know the player's look is read immediately after this event returns.
					//Override it here. It should be set back to normal next tick.
					//And ideally nothing weird happens because of it.

					((CraftEntity) damager).getHandle().setXRot((float) Math.toDegrees(Math.asin(dir.y/dir.length()))); 
					((CraftEntity) damager).getHandle().setYRot((float) Math.toDegrees(Math.atan2(-dir.x, dir.z))); 
				}
			}
		}
	}

}
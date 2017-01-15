package org.vivecraft.listeners;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.utils.Headshot;


public class VivecraftCombatListener implements Listener{

	private VSE vse;
	
	public VivecraftCombatListener(VSE plugin){
		this.vse = plugin;
	}
	
	   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	   public void onProjectileLaunch(ProjectileLaunchEvent event) {
		   //position all projectiles correctly.
		   
		   final Projectile proj = event.getEntity();
		   if (!(proj.getShooter() instanceof Player) || !VSE.isVive((Player) proj.getShooter()))
			   return;

		    Player pl = (Player)proj.getShooter();
		    VivePlayer vp = (VivePlayer)VSE.vivePlayers.get(pl.getUniqueId());
		   
		   int hand = 0;
		   if (proj instanceof CraftArrow) hand = 1;
		   //TODO: check for seated mode.
		   
		   if ((vp == null) && (this.vse.getConfig().getBoolean("debug.enabled"))) {
			   vse.getLogger().warning(" Error on projectile launch!");
		   }
		   
		   //this only works if the incoming speed is at max (based! on draw time)
		   //TODO: properly scale in all cases.
		   
		   proj.teleport(vp.getControllerPos(hand));
		   if(proj.getType() == EntityType.ARROW && vp.getDraw() != 0) {
			   proj.setVelocity(proj.getVelocity().multiply(vp.getDraw()));  
		   }
		   
	   }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
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
}
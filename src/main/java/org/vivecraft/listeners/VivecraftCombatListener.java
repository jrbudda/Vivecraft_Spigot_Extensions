package org.vivecraft.listeners;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.utils.Headshot;

public class VivecraftCombatListener implements Listener {

    private final VSE vse;

    public VivecraftCombatListener(VSE plugin) {
        this.vse = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        //position all projectiles correctly.

        Projectile proj = event.getEntity();

        if (!(proj.getShooter() instanceof Player) || !VSE.isVive((Player) proj.getShooter()))
            return;

        Player pl = (Player) proj.getShooter();
        final VivePlayer vp = VSE.vivePlayers.get(pl.getUniqueId());

        final boolean arrow = proj instanceof AbstractArrow && !(proj instanceof Trident);

        if ((vp == null) && (this.vse.getConfig().getBoolean("general.debug"))) {
            vse.getLogger().warning(" Error on projectile launch!");
        }

        Location pos = vp.getControllerPos(vp.activeHand);
        Vector aim = vp.getControllerDir(vp.activeHand);

        //this only works if the incoming speed is at max (based! on draw time)
        //TODO: properly scale in all cases.

        if (arrow && vp.getDraw() != 0) {
            proj.setVelocity(proj.getVelocity().multiply(vp.getDraw()));
            if (!vp.isSeated()) { //standing
                pos = vp.getControllerPos(0);
                aim = (vp.getControllerPos(1).subtract(vp.getControllerPos(0))).toVector();
                aim.normalize();
            }
        }

        Location loc = new Location(proj.getWorld(), pos.getX() + aim.getX() * 0.6f, pos.getY() + aim.getY() * 0.6f, pos.getZ() + aim.getZ() * 0.6f);
        double velo = proj.getVelocity().length();
        proj.teleport(loc); //paper sets velocity to 0 on teleport.
        proj.setVelocity(new Vector(aim.getX() * velo, aim.getY() * velo, aim.getZ() * velo));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onProjectileHit(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Trident) return;

        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof LivingEntity) {
            final Arrow arrow = (Arrow) event.getDamager();
            LivingEntity target = (LivingEntity) event.getEntity();
            boolean headshot = Headshot.isHeadshot(target, arrow);

            if (!(arrow.getShooter() instanceof Player) || !VSE.isVive((Player) arrow.getShooter()))
                return;
            Player pl = (Player) arrow.getShooter();
            VivePlayer vp = VSE.vivePlayers.get(pl.getUniqueId());

            if (!vp.isSeated()) {
                if (headshot) {
                    event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.standingheadshotmultiplier"));
                } else {
                    event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.standingmultiplier"));
                }
            } else {
                if (headshot) {
                    event.setDamage(event.getDamage() * vse.getConfig().getDouble("bow.seatedheadshotmultiplier"));
                } else {
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

                if (!vse.getConfig().getBoolean("pvp.VRvsVR", true)) {
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
                    if (((VSE.isVive(attacker) && VSE.isSeated(attacker)) && !VSE.isVive(victim)) || ((VSE.isVive(victim) && VSE.isSeated(victim)) && !VSE.isVive(attacker))) {
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

            } else if (damaged instanceof Fireball) {
                VivePlayer vp = VSE.vivePlayers.get(damager.getUniqueId());
                if (vp != null && vp.isVR()) {
                    Vector dir = vp.getHMDDir();
                    //Interesting experiment.
                    //We know the player's look is read immediately after this event returns.
                    //Override it here. It should be set back to normal next tick.
                    //And ideally nothing weird happens because of it.

                    damager.setRotation((float) Math.toDegrees(Math.asin(dir.getY() / dir.length())), (float) Math.toDegrees(Math.atan2(-dir.getX(), dir.getY())));
                }
            }
        }
    }

}
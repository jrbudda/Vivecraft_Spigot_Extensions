package org.vivecraft.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VivecraftItemListener implements Listener{
	VSE vse = null;
	public VivecraftItemListener(VSE vse){
		this.vse = vse;
	}
	
	 @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	 public void onPlayerDropItem(PlayerDropItemEvent event) {
		 final Player player = event.getPlayer();
		 if (!VSE.isVive(player))
		 return;
		 
		 VivePlayer vp = (VivePlayer)VSE.vivePlayers.get(player.getUniqueId());
		 
		 if(vp == null)return;
		 
		 float f2 = 0.3F;
		 
		 if(event.getItemDrop().getType() == EntityType.DROPPED_ITEM){
		 	 Vector v = new Vector();
			 float yaw = player.getLocation().getYaw();
			 float pitch = -player.getLocation().getPitch();
			 v.setX((double)(-Mth.sin(yaw * 0.017453292F) * Mth.cos(player.getLocation().getPitch() * 0.017453292F) * f2));
			 v.setZ((double)(Mth.cos(yaw * 0.017453292F) * Mth.cos(player.getLocation().getPitch() * 0.017453292F) * f2));
			 v.setY((double)(Mth.sin(pitch * 0.017453292F) * f2 + 0.1F));
			 
             Vec3 aim = vp.getControllerDir(0);
			 event.getItemDrop().teleport(vp.getControllerPos(0).add(0.2f*aim.x,0.25f*aim.y - 0.2f, 0.2f*aim.z));
			 event.getItemDrop().setVelocity(v);
		 }
	 }
}

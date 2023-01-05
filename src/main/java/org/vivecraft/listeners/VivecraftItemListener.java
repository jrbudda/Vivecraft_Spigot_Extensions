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

public class VivecraftItemListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (!VSE.isVive(player))
            return;

        VivePlayer vp = VSE.vivePlayers.get(player.getUniqueId());

        if (vp == null) return;

        float f2 = 0.3F;

        if (event.getItemDrop().getType() == EntityType.DROPPED_ITEM) {
            Vector v = new Vector();
            float yaw = player.getLocation().getYaw();
            float pitch = -player.getLocation().getPitch();
            v.setX(-Math.sin(yaw * 0.017453292F) * Math.cos(player.getLocation().getPitch() * 0.017453292F) * f2);
            v.setZ(Math.cos(yaw * 0.017453292F) * Math.cos(player.getLocation().getPitch() * 0.017453292F) * f2);
            v.setY(Math.sin(pitch * 0.017453292F) * f2 + 0.1F);

            Vector aim = vp.getControllerDir(0);
            event.getItemDrop().teleport(vp.getControllerPos(0).add(0.2f * aim.getX(), 0.25f * aim.getY() - 0.2f, 0.2f * aim.getZ()));
            event.getItemDrop().setVelocity(v);
        }
    }
}

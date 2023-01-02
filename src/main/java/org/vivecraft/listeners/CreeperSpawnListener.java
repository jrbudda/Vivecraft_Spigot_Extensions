package org.vivecraft.listeners;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.vivecraft.VSE;

public class CreeperSpawnListener implements Listener {

    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.CREEPER)
            return;

        if (!VSE.me.getConfig().getBoolean("CreeperRadius.enabled"))
            return;

        int newRadius = VSE.me.getConfig().getInt("CreeperRadius.radius", 3);
        Creeper creeper = (Creeper) event.getEntity();
        creeper.setExplosionRadius(newRadius);
    }
}

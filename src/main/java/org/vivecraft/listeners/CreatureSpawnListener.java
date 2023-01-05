package org.vivecraft.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.vivecraft.VSE;
import org.vivecraft.compatibility.CompatibilityAPI;

public class CreatureSpawnListener implements Listener {

    public CreatureSpawnListener() {
        // inject any creatures that already exist
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                onSpawn(new CreatureSpawnEvent(entity, CreatureSpawnEvent.SpawnReason.CUSTOM));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {

        // We need to modify ALL endermen to make sure that VR players make
        // them angry properly
        if (event.getEntityType() == EntityType.ENDERMAN) {
            CompatibilityAPI.getCompatibility().injectEnderman((Enderman) event.getEntity());
            return;
        }

        // Creepers should only be modified if config says to.
        if (event.getEntityType() != EntityType.CREEPER)
            return;

        if (!VSE.me.getConfig().getBoolean("CreeperRadius.enabled"))
            return;

        double newRadius = VSE.me.getConfig().getDouble("CreeperRadius.radius", 3.0);
        CompatibilityAPI.getCompatibility().injectCreeper((Creeper) event.getEntity(), newRadius);
    }
}

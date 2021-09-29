package org.vivecraft.entities;

import java.util.EnumSet;

import org.vivecraft.VSE;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;

public class CustomGoalSwell extends Goal {

    private final Creeper creeper;
    private LivingEntity target;

	public CustomGoalSwell(Creeper var0) {
        this.creeper = var0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	public double creeperBlowyUppyRadius = 3.0f; //VIVE default is 3
	
    @Override
	public boolean canUse(){
		VSE vse = (VSE.getPlugin(VSE.class));
        LivingEntity livingentity = this.creeper.getTarget();
		if(vse.getConfig().getBoolean("CreeperRadius.enabled") == true){
			if(livingentity != null && VSE.vivePlayers.containsKey(livingentity.getBukkitEntity().getUniqueId()) && VSE.isVive(VSE.vivePlayers.get(livingentity.getBukkitEntity().getUniqueId()).player))
				creeperBlowyUppyRadius = vse.getConfig().getDouble("CreeperRadius.radius");
		}
	    return this.creeper.getSwellDir() > 0 || livingentity != null && this.creeper.distanceToSqr(livingentity) < creeperBlowyUppyRadius*creeperBlowyUppyRadius;
	}
	
    @Override
    public void start()
    {
        this.creeper.getNavigation().stop();
        this.target = this.creeper.getTarget();
    }
    
    @Override
    public void stop()
    {
        this.target = null;
    }
    
    @Override
    public void tick()
    {
        if (this.target == null)
        {
            this.creeper.setSwellDir(-1);
        }
        else if (this.creeper.distanceToSqr(this.target) > 49.0D)
        {
            this.creeper.setSwellDir(-1);
        }
        else if (!this.creeper.getSensing().hasLineOfSight(this.target))
        {
            this.creeper.setSwellDir(-1);
        }
        else
        {
            this.creeper.setSwellDir(1);
        }
    }
}

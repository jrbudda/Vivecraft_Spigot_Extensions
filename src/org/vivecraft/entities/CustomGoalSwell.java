package org.vivecraft.entities;

import java.util.EnumSet;

import org.vivecraft.VSE;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityCreeper;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.PathfinderGoal;

public class CustomGoalSwell extends PathfinderGoal {

	EntityCreeper a;
	EntityLiving b;

	public CustomGoalSwell(EntityCreeper var0) {
		this.a = var0;
		this.a(EnumSet.of(Type.MOVE));
	}

	public double creeperBlowyUppyRadius = 3.0f; //VIVE default is 3

	public boolean a(){
		VSE vse = (VSE.getPlugin(VSE.class));
		EntityLiving localEntityLiving = this.a.getGoalTarget();
		if(vse.getConfig().getBoolean("CreeperRadius.enabled") == true){
			if(localEntityLiving != null && VSE.vivePlayers.containsKey(localEntityLiving.getUniqueID()) && VSE.isVive(VSE.vivePlayers.get(localEntityLiving.getUniqueID()).player))
				creeperBlowyUppyRadius = vse.getConfig().getDouble("CreeperRadius.radius");
		}
        if (this.a.dW() > 0) return true;
        if (localEntityLiving == null) return false;
        if (!(this.a.h((Entity)localEntityLiving) < creeperBlowyUppyRadius*creeperBlowyUppyRadius)) return false;
        return true;
	}

	public void c()
	{
		this.a.getNavigation().o();
		this.b = this.a.getGoalTarget();
	}

	public void d()
	{
		this.b = null;
	}

	public void e() {
		if (this.b == null) {
			this.a.a(-1);
		} else if (this.a.h(this.b) > 49.0D) {
			this.a.a(-1);
		} else if (!this.a.getEntitySenses().a(this.b)) {
			this.a.a(-1);
		} else {
			this.a.a(1);
		}
	}

}

package org.vivecraft.entities;

import java.util.EnumSet;

import org.vivecraft.VSE;

import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityCreeper;
import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.PathfinderGoal;

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
		EntityLiving var0 = this.a.getGoalTarget();
		if(vse.getConfig().getBoolean("CreeperRadius.enabled") == true){
			if(var0 != null && VSE.vivePlayers.containsKey(var0.getUniqueID()) && VSE.isVive(VSE.vivePlayers.get(var0.getUniqueID()).player))
				creeperBlowyUppyRadius = vse.getConfig().getDouble("CreeperRadius.radius");
		}
	    return this.a.eK() > 0 || var0 != null && this.a.h(var0) < creeperBlowyUppyRadius*creeperBlowyUppyRadius;
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

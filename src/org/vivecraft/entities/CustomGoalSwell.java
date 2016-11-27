package org.vivecraft.entities;

import org.vivecraft.VSE;

import net.minecraft.server.v1_11_R1.EntityCreeper;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.PathfinderGoal;

public class CustomGoalSwell extends PathfinderGoal {

	EntityCreeper a;
	EntityLiving b;
	
	public CustomGoalSwell(EntityCreeper arg0) 
	{
		a = arg0;
		a(1);
	}
	
	public double creeperBlowyUppyRadius = 3.0f; //VIVE default is 3
	
	
	public boolean a(){
		VSE vse = (VSE.getPlugin(VSE.class));
		EntityLiving localEntityLiving = this.a.getGoalTarget();
		 if(vse.getConfig().getBoolean("CreeperRadius.enabled") == true){
			 if((VSE.vivePlayers != null && localEntityLiving != null) && VSE.vivePlayers.containsKey(localEntityLiving.getUniqueID()))
			 creeperBlowyUppyRadius = vse.getConfig().getDouble("CreeperRadius.radius");
		 }
		return (this.a.di() > 0) || ((localEntityLiving != null) && (this.a.h(localEntityLiving) < (creeperBlowyUppyRadius*creeperBlowyUppyRadius)));
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
	  
	  public void e()
	  {
	    if (this.b == null)
	    {
	      this.a.a(-1);
	      return;
	    }
	    if (this.a.h(this.b) > 49.0D)
	    {
	      this.a.a(-1);
	      return;
	    }
	    if (!this.a.getEntitySenses().a(this.b))
	    {
	      this.a.a(-1);
	      return;
	    }
	    this.a.a(1);
	  }
}

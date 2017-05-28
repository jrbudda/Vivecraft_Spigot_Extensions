package org.vivecraft.entities;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.vivecraft.VSE;

import com.google.common.base.Predicate;

import net.minecraft.server.v1_10_R1.Blocks;
import net.minecraft.server.v1_10_R1.EntityEnderman;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.Item;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_10_R1.Vec3D;

public class CustomPathFinderGoalPlayerWhoLookedAtTarget 
 extends PathfinderGoalNearestAttackableTarget<EntityHuman>
{
	private final EntityEnderman i;
	private EntityHuman j;
	private int k;
	private int l;

	private Method df;
	
	public CustomPathFinderGoalPlayerWhoLookedAtTarget(EntityEnderman entityenderman)
	{
		super(entityenderman, EntityHuman.class, false);
		this.i = entityenderman;
		this.df = VSE.getPrivateMethod("df", EntityEnderman.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean a()
	{
		double d0 = i();

		this.j = this.i.world.a(this.i.locX, this.i.locY, this.i.locZ, d0, d0, null, new Predicate()
		{
			public boolean a(@Nullable EntityHuman entityhuman)
			{
				return (entityhuman != null) && isLookingAtMe((entityhuman));
			}

			public boolean apply(@Nullable Object object)
			{
				return a((EntityHuman)object);
			}
		});
		return this.j != null;
	}

	 private boolean isLookingAtMe(EntityHuman entityhuman)
	  {
	    ItemStack itemstack = (ItemStack)entityhuman.inventory.armor[3];
	    if (itemstack !=null && itemstack.getItem() == Item.getItemOf(Blocks.PUMPKIN)) {
	      return false;
	    }
	    
	    //VSE MODIFICATION
	    Vec3D vec3d;
	    if(VSE.isVive((Player) entityhuman.getBukkitEntity())){
	        vec3d = VSE.vivePlayers.get(entityhuman.getBukkitEntity().getUniqueId()).getHMDDir();
	    } else {
	        vec3d = entityhuman.f(1.0F).a();
	    }
	    ////

	    Vec3D vec3d1 = new Vec3D(i.locX - entityhuman.locX, i.getBoundingBox().b + i.getHeadHeight() - (entityhuman.locY + entityhuman.getHeadHeight()), i.locZ - entityhuman.locZ);
	    double d0 = vec3d1.b();
	    
	    vec3d1 = vec3d1.a();
	    double d1 = vec3d.b(vec3d1);
	    
	    return d1 > 1.0D - 0.025D / d0 ? entityhuman.hasLineOfSight(i) : false;
	  }
	
	
	public void c()
	{
		this.k = 5;
		this.l = 0;
	}

	public void d()
	{
		this.j = null;
		super.d();
	}

	public boolean b()
	{
		if (this.j != null)
		{
			if (!isLookingAtMe(this.j)) {
				return false;
			}
			this.i.a(this.j, 10.0F, 10.0F);
			return true;
		}
		return (this.d != null) && (((EntityHuman)this.d).isAlive()) ? true : super.b();
	}

	public void e()
	{
		if (this.j != null)
		{
			if (--this.k <= 0)
			{
				this.d = this.j;
				this.j = null;
				super.c();
			}
		}
		else
		{
			if (this.d != null) {
				if (isLookingAtMe(this.d))
				{
					if (((EntityHuman)this.d).h(this.i) < 16.0D) {
						try {
							df.invoke(this.i, (Object[]) null);
						} catch (Exception e1) {
						}
					}
					this.l = 0;
				}
				else if ((((EntityHuman)this.d).h(this.i) > 256.0D) && (this.l++ >= 30) && (this.i.a(this.d)))
				{
					this.l = 0;
				}
			}
			super.e();
		}
	}
}


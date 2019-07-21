package org.vivecraft.entities;

import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityEnderman;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.Vec3D;

public class CustomPathFinderGoalPlayerWhoLookedAtTarget 
extends PathfinderGoalNearestAttackableTarget<EntityHuman> {
	   private final EntityEnderman i;
	   private EntityHuman j;
	   private int k;
	   private int l;
	   private final PathfinderTargetCondition m;
	   private final PathfinderTargetCondition n = (new PathfinderTargetCondition()).c();
	   private Method dV;
	   private Method a_entity;
		
	   public CustomPathFinderGoalPlayerWhoLookedAtTarget(EntityEnderman entityenderman) {
	      super(entityenderman, EntityHuman.class, false);
	      this.i = entityenderman;
	      this.dV = VSE.getPrivateMethod("dV", EntityEnderman.class, null);
	      this.a_entity = VSE.getPrivateMethod("a", EntityEnderman.class, Entity.class);
	      this.m = (new PathfinderTargetCondition()).a(this.k()).a((entityliving) -> {
	         return isLookingAtMe((EntityHuman)entityliving);
	      });
	   }

	   public boolean a() {
	      this.j = this.i.world.a(this.m, this.i);
	      return this.j != null;
	   }

	   public void c() {
	      this.k = 5;
	      this.l = 0;
	   }

	   public void d() {
	      this.j = null;
	      super.d();
	   }
	   
	   private boolean isLookingAtMe(EntityHuman entityhuman) {
		   ItemStack itemstack = (ItemStack)entityhuman.inventory.armor.get(3);
		   if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.getItem()) {
			   return false;
		   } else {
			   Vec3D vec3d = entityhuman.f(1.0F).d();
			   Vec3D vec3d1 = new Vec3D(i.locX - entityhuman.locX, i.getBoundingBox().minY + (double)i.getHeadHeight() - (entityhuman.locY + (double)entityhuman.getHeadHeight()), i.locZ - entityhuman.locZ);
			   //VSE MODIFICATION
			   boolean vr = VSE.isVive((Player)entityhuman.getBukkitEntity());
			   VivePlayer vp = null;
			   Vec3D hmdpos = null;
			   if(vr){
				   vp = VSE.vivePlayers.get(entityhuman.getBukkitEntity().getUniqueId());
				   vec3d = vp.getHMDDir();
				   Location h = vp.getHMDPos();
				   hmdpos = new Vec3D(h.getX(), h.getY(), h.getZ());
				   vec3d1 = new Vec3D(i.locX - hmdpos.getX(), i.getBoundingBox().minY + (double)i.getHeadHeight() - hmdpos.getY(), i.locZ - hmdpos.getZ());
			   }
			   ////
			   double d0 = vec3d1.f();
			   vec3d1 = vec3d1.d();
			   double d1 = vec3d.b(vec3d1);
			   return d1 > 1.0D - 0.025D / d0 ? (vr ? hasLineOfSight(hmdpos, new Vec3D(i.locX, i.locY + (double)i.getHeadHeight(), i.locZ)) : entityhuman.hasLineOfSight(i)) : false;
		   }
	   }

	    private boolean hasLineOfSight(Vec3D source, Vec3D target) {
	        if (i.world.rayTrace(new RayTrace(source, target , RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, (Entity)i)).getType() != MovingObjectPosition.EnumMovingObjectType.MISS) return false;
	        return true;
	    }

	   public boolean b() {
	      if (this.j != null) {
	         if (!isLookingAtMe(this.j)) {
	            return false;
	         } else {
	            this.i.a(this.j, 10.0F, 10.0F);
	            return true;
	         }
	      } else {
	         return this.c != null && this.n.a(this.i, this.c) ? true : super.b();
	      }
	   }

	   public void e() {
	      if (this.j != null) {
	         if (--this.k <= 0) {
	            this.c = this.j;
	            this.j = null;
	            super.c();
	         }
	      } else {
	         if (this.c != null && !this.i.isPassenger()) {
	            if (isLookingAtMe((EntityHuman)this.c)) {
	               if (this.c.h(this.i) < 16.0D) {
	            	   VSE.invoke(this.dV, this.i, null);
	               }

	               this.l = 0;
	            } else if (this.c.h(this.i) > 256.0D && this.l++ >= 30 && (Boolean) VSE.invoke(this.a_entity, i, this.c)) {
	               this.l = 0;
	            }
	         }

	         super.e();
	      }

	   }

}


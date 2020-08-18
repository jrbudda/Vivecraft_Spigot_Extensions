package org.vivecraft.entities;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityEnderman;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.MovingObjectPosition;
import net.minecraft.server.v1_16_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R2.PathfinderTargetCondition;
import net.minecraft.server.v1_16_R2.RayTrace;
import net.minecraft.server.v1_16_R2.Vec3D;

public class CustomPathFinderGoalPlayerWhoLookedAtTarget extends PathfinderGoalNearestAttackableTarget<EntityHuman> {
	   private final EntityEnderman i;
	   private EntityHuman j;
	   private int k;
	   private int l;
	   private final PathfinderTargetCondition m;
	   private final PathfinderTargetCondition n = (new PathfinderTargetCondition()).c();
	   private Method eL;
	   private Method a_entity;
		
	   public CustomPathFinderGoalPlayerWhoLookedAtTarget(EntityEnderman entityenderman) {
	      super(entityenderman, EntityHuman.class, 10, false, false, entityenderman::a_);
	      this.i = entityenderman;
	      this.eL = VSE.getPrivateMethod("eL", EntityEnderman.class, null);
	      this.a_entity = VSE.getPrivateMethod("a", EntityEnderman.class, Entity.class);
	      this.m = (new PathfinderTargetCondition()).a(this.k()).a((entityliving) -> {
	         return isLookingAtMe((EntityHuman)entityliving);
	      });
	   }

	   @Override
	   public boolean a() {
	      this.j = this.i.world.a(this.m, (EntityLiving)this.i);
	      return this.j != null;
	   }
	   
	   @Override
	   public void c() {
	      this.k = 5;
	      this.l = 0;
	      this.i.eP();
	   }
	   
	   @Override
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
			   Vec3D vec3d1 = new Vec3D(i.locX() - entityhuman.locX(), i.getHeadY() - entityhuman.getHeadY(), i.locZ() - entityhuman.locZ());
			   //VSE MODIFICATION
			   boolean vr = entityhuman instanceof EntityPlayer && VSE.isVive((Player)entityhuman.getBukkitEntity());
			   VivePlayer vp = null;
			   Vec3D hmdpos = null;
			   if(vr){
				   vp = VSE.vivePlayers.get(entityhuman.getBukkitEntity().getUniqueId());
				   vec3d = vp.getHMDDir();
				   Location h = vp.getHMDPos();
				   hmdpos = new Vec3D(h.getX(), h.getY(), h.getZ());
				   vec3d1 = new Vec3D(i.locX() - hmdpos.getX(), i.getHeadY() - hmdpos.getY(), i.locZ() - hmdpos.getZ());
			   }
			   ////
			   double d0 = vec3d1.f();
			   vec3d1 = vec3d1.d();
			   double d1 = vec3d.b(vec3d1);
			   if (!(d1 > 1.0 - 0.025 / d0)) return false;
			   if (vr)
				   return hasLineOfSight(hmdpos, new Vec3D(i.locX(), i.getHeadY(), i.locZ()));
			   else
				   return entityhuman.hasLineOfSight((Entity)i);
		   }
	   }

	    private boolean hasLineOfSight(Vec3D source, Vec3D target) {
	        if (i.world.rayTrace(new RayTrace(source, target , RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, (Entity)i)).getType() != MovingObjectPosition.EnumMovingObjectType.MISS) return false;
	        return true;
	    }
	    
	   @Override
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
	   
	   @Override
	   public void e() {
	        if (this.i.getGoalTarget() == null) {
	            super.a((EntityHuman)null);
	        }
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
	            	   VSE.invoke(this.eL, this.i, null);
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


package org.vivecraft.entities;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.server.v1_16_R1.Blocks;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityEnderman;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.MovingObjectPosition;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import net.minecraft.server.v1_16_R1.RayTrace;
import net.minecraft.server.v1_16_R1.Vec3D;

public class CustomGoalStare extends PathfinderGoal {
	private final EntityEnderman i;
	private EntityLiving b;

	public CustomGoalStare(EntityEnderman entityenderman) {
		this.i = entityenderman;
		this.a(EnumSet.of(PathfinderGoal.Type.JUMP, PathfinderGoal.Type.MOVE));
	}

	public boolean a() {
		this.b = this.i.getGoalTarget();
		if (!(this.b instanceof EntityHuman)) {
			return false;
		}
		double d0 = this.b.h((Entity)this.i);
		if (d0 > 256.0) {
			return false;
		}
		boolean bl = isLookingAtMe((EntityHuman)((EntityHuman)this.b));
		return bl;
	}

	public void c() {
		this.i.getNavigation().o();
	}

	public void e() {
		this.i.getControllerLook().a(this.b.locX(), this.b.getHeadY(), this.b.locZ());
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
    
}

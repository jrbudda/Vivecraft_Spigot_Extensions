package org.vivecraft.entities;

import java.util.EnumSet;

import org.bukkit.Location;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CustomGoalStare extends Goal {
    private final Entity enderman;
    private Entity target;

    public CustomGoalStare(EnderMan p_32550_)
    {
        this.enderman = p_32550_;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    public boolean canUse()
    {
        this.target = ((Mob)this.enderman).getTarget();

        if (!(this.target instanceof Player))
        {
            return false;
        }
        else
        {
            double d0 = this.target.distanceToSqr(this.enderman);
            return d0 > 256.0D ? false : isLookingAtMe((Player)this.target);
        }
    }

    public void start()
    {
        ((Mob) this.enderman).getNavigation().stop();
    }

    public void tick()
    {
        ((Mob) this.enderman).getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
    }

	//Vivecraft copy and modify from EnderMan
	private boolean isLookingAtMe(Player pPlayer)
	{
		ItemStack itemstack = pPlayer.getInventory().armor.get(3);

		if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem()))
		{
			return false;
		}
		else
		{
			Vec3 vec3 = pPlayer.getViewVector(1.0F).normalize();
			Vec3 vec31 = new Vec3(enderman.getX() - pPlayer.getX(), enderman.getEyeY() - pPlayer.getEyeY(), enderman.getZ() - pPlayer.getZ());
			//VSE MODIFICATION
			boolean vr = pPlayer instanceof Player && VSE.isVive((org.bukkit.entity.Player) pPlayer.getBukkitEntity());
			VivePlayer vp = null;
			Vec3 hmdpos = null;
			if(vr){
				vp = VSE.vivePlayers.get(pPlayer.getBukkitEntity().getUniqueId());
				vec3 = vp.getHMDDir();
				Location h = vp.getHMDPos();
				hmdpos = new Vec3(h.getX(), h.getY(), h.getZ());
				vec31= new Vec3(enderman.getX() - hmdpos.x, enderman.getEyeY() - hmdpos.y, enderman.getZ() - hmdpos.z);
			}
			////
			double d0 = vec31.length();
			vec31 = vec31.normalize();
			double d1 = vec3.dot(vec31);
			//VSE MODIFICATION
			if(! (d1 > 1.0D - 0.025D / d0)) return false; 			
			if(vr)
				return hasLineOfSight(hmdpos, enderman);
			else
				return pPlayer.hasLineOfSight(enderman);
			//
		}
	}

	//Vivecraft copy and modify from LivingEntity
    public boolean hasLineOfSight(Vec3 source, Entity entity)
    {
     	Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

    	if (vec31.distanceTo(source) > 128.0D)
    	{
    		return false;
    	}
    	else
    	{
    		return entity.level.clip(new ClipContext(source, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    	}
    }
    
}

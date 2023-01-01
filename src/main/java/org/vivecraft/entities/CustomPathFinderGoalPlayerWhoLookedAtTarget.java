package org.vivecraft.entities;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CustomPathFinderGoalPlayerWhoLookedAtTarget extends NearestAttackableTargetGoal<Player> {
	private final EnderMan enderman;
	private Player pendingTarget;
	private int aggroTime;
	private int teleportTime;
	private final TargetingConditions startAggroTargetConditions;
	private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();

	public CustomPathFinderGoalPlayerWhoLookedAtTarget(EnderMan entityenderman, Predicate<LivingEntity> p) {
		super(entityenderman, Player.class, 10, false, false, p);
		this.enderman = entityenderman;
		this.startAggroTargetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector((player) ->
		{
			return isLookingAtMe((Player)player);
		});
	}

	@Override
	public boolean canUse()
	{
		this.pendingTarget = this.enderman.level.getNearestPlayer(this.startAggroTargetConditions, this.enderman);
		return this.pendingTarget != null;
	}

	@Override
	public void start()
	{
		this.aggroTime = 5;
		this.teleportTime = 0;
		this.enderman.setBeingStaredAt();
	}

	@Override
	public void stop()
	{
		this.pendingTarget = null;
		super.stop();
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
	
	
	@Override
	public boolean canContinueToUse()
	{
		if (this.pendingTarget != null)
		{
			if (!isLookingAtMe(this.pendingTarget))
			{
				return false;
			}
			else
			{
				this.enderman.lookAt(this.pendingTarget, 10.0F, 10.0F);
				return true;
			}
		}
		else
		{
			return this.target != null && this.continueAggroTargetConditions.test((EnderMan)this.enderman, this.target) ? true : super.canContinueToUse();
		}
	}

	@Override
	public void tick()
	{
		if (this.enderman.getTarget() == null)
		{
			super.setTarget((LivingEntity)null);
		}

		if (this.pendingTarget != null)
		{
			if (--this.aggroTime <= 0)
			{
				this.target = (LivingEntity) this.pendingTarget;
				this.pendingTarget = null;
				super.start();
			}
		}
		else
		{
			if (this.target != null && !this.enderman.isPassenger())
			{
				if (isLookingAtMe((Player)this.target))
				{
					if (this.target.distanceToSqr(this.enderman) < 16.0D)
					{
						Reflector.invoke(Reflector.Entity_teleport, enderman);
					}

					this.teleportTime = 0;
				}
				else if (this.target.distanceToSqr(this.enderman) > 256.0D && this.teleportTime++ >= 30 && (boolean)Reflector.invoke(Reflector.Entity_teleportTowards, enderman, pendingTarget));
				{
					this.teleportTime = 0;
				}
			}

			super.tick();
		}
	}
}



package org.vivecraft.compatibility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEnderman;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import java.util.AbstractCollection;
import java.util.EnumSet;
import java.util.function.Predicate;

public class Vivecraft_1_19_R1 implements VivecraftCompatibility {

    @Override
    public void editCreeper(Creeper creeper, double radius) {
        net.minecraft.world.entity.monster.Creeper e = ((CraftCreeper) creeper).getHandle();
        AbstractCollection<WrappedGoal> goalB = (AbstractCollection<WrappedGoal>) Reflector.getFieldValue(Reflector.availableGoals, ((Mob) e).goalSelector);
        for (WrappedGoal b : goalB) {
            if (b.getGoal() instanceof net.minecraft.world.entity.ai.goal.SwellGoal) {//replace swell goal.
                goalB.remove(b);
                break;
            }
        }
        e.goalSelector.addGoal(2, new CustomGoalSwell(e, radius));
    }

    @Override
    public void editEnderman(Enderman enderman) {
        EnderMan e = ((CraftEnderman) enderman).getHandle();
        AbstractCollection<WrappedGoal> targets = (AbstractCollection<WrappedGoal>) Reflector.getFieldValue(Reflector.availableGoals, ((Mob) e).targetSelector);
        for (WrappedGoal b : targets) {
            if (b.getPriority() == 1) { //replace PlayerWhoLookedAt target. Class is private cant use instanceof, check priority on all new versions.
                targets.remove(b);
                break;
            }
        }
        e.targetSelector.addGoal(1, new CustomPathFinderGoalPlayerWhoLookedAtTarget(e, e::isAngryAt));

        AbstractCollection<WrappedGoal> goals = (AbstractCollection<WrappedGoal>) Reflector.getFieldValue(Reflector.availableGoals, ((Mob) e).goalSelector);
        for (WrappedGoal b : goals) {
            if (b.getPriority() == 1) {//replace EndermanFreezeWhenLookedAt goal. Verify priority on new version.
                goals.remove(b);
                break;
            }
        }
        e.goalSelector.addGoal(1, new CustomGoalStare(e));
    }

    public static class CustomGoalSwell extends SwellGoal {

        private final net.minecraft.world.entity.monster.Creeper creeper;
        public double radiusSqr;

        public CustomGoalSwell(net.minecraft.world.entity.monster.Creeper var0, double radius) {
            super(var0);

            this.creeper = var0;
            this.radiusSqr = radius * radius;
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.creeper.getTarget();

            // usually you want VR players to have a smaller radius since it is
            // harder to play in VR.
            double distance = 9.0;
            if (livingEntity != null && livingEntity.getBukkitEntity() instanceof Player player && VSE.isVive(player)) {
                distance = radiusSqr;
            }

            return this.creeper.getSwellDir() > 0 || livingEntity != null && this.creeper.distanceToSqr(livingEntity) < 9.0D;
        }
    }

    public static class CustomPathFinderGoalPlayerWhoLookedAtTarget extends NearestAttackableTargetGoal<net.minecraft.world.entity.player.Player> {

        private final EnderMan enderman;
        private final TargetingConditions startAggroTargetConditions;
        private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();
        private net.minecraft.world.entity.player.Player pendingTarget;
        private int aggroTime;
        private int teleportTime;

        public CustomPathFinderGoalPlayerWhoLookedAtTarget(EnderMan entityenderman, Predicate<LivingEntity> p) {
            super(entityenderman, net.minecraft.world.entity.player.Player.class, 10, false, false, p);
            this.enderman = entityenderman;
            this.startAggroTargetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector((player) ->
            {
                return isLookingAtMe((net.minecraft.world.entity.player.Player) player);
            });
        }

        @Override
        public boolean canUse() {
            this.pendingTarget = this.enderman.level.getNearestPlayer(this.startAggroTargetConditions, this.enderman);
            return this.pendingTarget != null;
        }

        @Override
        public void start() {
            this.aggroTime = 5;
            this.teleportTime = 0;
            this.enderman.setBeingStaredAt();
        }

        @Override
        public void stop() {
            this.pendingTarget = null;
            super.stop();
        }

        //Vivecraft copy and modify from EnderMan
        private boolean isLookingAtMe(net.minecraft.world.entity.player.Player pPlayer) {
            ItemStack itemstack = pPlayer.getInventory().armor.get(3);

            if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                return false;
            } else {
                Vec3 vec3 = pPlayer.getViewVector(1.0F).normalize();
                Vec3 vec31 = new Vec3(enderman.getX() - pPlayer.getX(), enderman.getEyeY() - pPlayer.getEyeY(), enderman.getZ() - pPlayer.getZ());
                //VSE MODIFICATION
                boolean vr = pPlayer instanceof Player && VSE.isVive((org.bukkit.entity.Player) pPlayer.getBukkitEntity());
                VivePlayer vp = null;
                Vec3 hmdpos = null;
                if (vr) {
                    vp = VSE.vivePlayers.get(pPlayer.getBukkitEntity().getUniqueId());
                    Vector temp = vp.getHMDDir();
                    vec3 = new Vec3(temp.getX(), temp.getY(), temp.getZ());
                    Location h = vp.getHMDPos();
                    hmdpos = new Vec3(h.getX(), h.getY(), h.getZ());
                    vec31 = new Vec3(enderman.getX() - hmdpos.x, enderman.getEyeY() - hmdpos.y, enderman.getZ() - hmdpos.z);
                }
                ////
                double d0 = vec31.length();
                vec31 = vec31.normalize();
                double d1 = vec3.dot(vec31);
                //VSE MODIFICATION
                if (!(d1 > 1.0D - 0.025D / d0)) return false;
                if (vr)
                    return hasLineOfSight(hmdpos, enderman);
                else
                    return pPlayer.hasLineOfSight(enderman);
                //
            }
        }

        //Vivecraft copy and modify from LivingEntity
        public boolean hasLineOfSight(Vec3 source, Entity entity) {
            Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

            if (vec31.distanceTo(source) > 128.0D) {
                return false;
            } else {
                return entity.level.clip(new ClipContext(source, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
            }
        }


        @Override
        public boolean canContinueToUse() {
            if (this.pendingTarget != null) {
                if (!isLookingAtMe(this.pendingTarget)) {
                    return false;
                } else {
                    this.enderman.lookAt(this.pendingTarget, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return this.target != null && this.continueAggroTargetConditions.test((EnderMan) this.enderman, this.target) ? true : super.canContinueToUse();
            }
        }

        @Override
        public void tick() {
            if (this.enderman.getTarget() == null) {
                super.setTarget((LivingEntity) null);
            }

            if (this.pendingTarget != null) {
                if (--this.aggroTime <= 0) {
                    this.target = (LivingEntity) this.pendingTarget;
                    this.pendingTarget = null;
                    super.start();
                }
            } else {
                if (this.target != null && !this.enderman.isPassenger()) {
                    if (isLookingAtMe((net.minecraft.world.entity.player.Player) this.target)) {
                        if (this.target.distanceToSqr(this.enderman) < 16.0D) {
                            Reflector.invoke(Reflector.Entity_teleport, enderman);
                        }

                        this.teleportTime = 0;
                    } else if (this.target.distanceToSqr(this.enderman) > 256.0D && this.teleportTime++ >= 30 && (boolean) Reflector.invoke(Reflector.Entity_teleportTowards, enderman, pendingTarget))
                        ;
                    {
                        this.teleportTime = 0;
                    }
                }

                super.tick();
            }
        }
    }

    /**
     * Based off of the EndermanFreezeWhenLookedAt, but it is private so we
     * cannot extend it.
     */
    public static class CustomGoalStare extends Goal {
        private final Entity enderman;
        private Entity target;

        public CustomGoalStare(EnderMan p_32550_) {
            this.enderman = p_32550_;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            this.target = ((Mob) this.enderman).getTarget();

            if (!(this.target instanceof net.minecraft.world.entity.player.Player)) {
                return false;
            } else {
                double d0 = this.target.distanceToSqr(this.enderman);
                return !(d0 > 256.0D) && isLookingAtMe((net.minecraft.world.entity.player.Player) this.target);
            }
        }

        public void start() {
            ((Mob) this.enderman).getNavigation().stop();
        }

        public void tick() {
            ((Mob) this.enderman).getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }

        //Vivecraft copy and modify from EnderMan
        private boolean isLookingAtMe(net.minecraft.world.entity.player.Player pPlayer) {
            ItemStack itemstack = pPlayer.getInventory().armor.get(3);

            if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                return false;
            } else {
                Vec3 vec3 = pPlayer.getViewVector(1.0F).normalize();
                Vec3 vec31 = new Vec3(enderman.getX() - pPlayer.getX(), enderman.getEyeY() - pPlayer.getEyeY(), enderman.getZ() - pPlayer.getZ());
                //VSE MODIFICATION
                boolean vr = pPlayer instanceof Player && VSE.isVive((org.bukkit.entity.Player) pPlayer.getBukkitEntity());
                VivePlayer vp = null;
                Vec3 hmdpos = null;
                if (vr) {
                    vp = VSE.vivePlayers.get(pPlayer.getBukkitEntity().getUniqueId());
                    Vector temp = vp.getHMDDir();
                    vec3 = new Vec3(temp.getX(), temp.getY(), temp.getZ());
                    Location h = vp.getHMDPos();
                    hmdpos = new Vec3(h.getX(), h.getY(), h.getZ());
                    vec31 = new Vec3(enderman.getX() - hmdpos.x, enderman.getEyeY() - hmdpos.y, enderman.getZ() - hmdpos.z);
                }
                ////
                double d0 = vec31.length();
                vec31 = vec31.normalize();
                double d1 = vec3.dot(vec31);
                //VSE MODIFICATION
                if (!(d1 > 1.0D - 0.025D / d0)) return false;
                if (vr)
                    return hasLineOfSight(hmdpos, enderman);
                else
                    return pPlayer.hasLineOfSight(enderman);
                //
            }
        }

        //Vivecraft copy and modify from LivingEntity
        public boolean hasLineOfSight(Vec3 source, Entity entity) {
            Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

            if (vec31.distanceTo(source) > 128.0D) {
                return false;
            } else {
                return entity.level.clip(new ClipContext(source, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
            }
        }

    }
}

package org.vivecraft.compatibility;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
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
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.utils.AimFixHandler;
import org.vivecraft.utils.Vector3;

import java.util.AbstractCollection;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

public class Vivecraft_1_19_R1 implements VivecraftCompatibility {

    @Override
    public void injectCreeper(Creeper creeper, double radius) {
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
    public void injectEnderman(Enderman enderman) {
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

    public void injectPlayer(Player player) {
        EntityDataAccessor<Pose> poseObj = (EntityDataAccessor<Pose>) Reflector.getFieldValue(Reflector.Entity_Data_Pose, player);
        SynchedEntityData dataWatcher = ((CraftPlayer) player).getHandle().getEntityData();
        Int2ObjectOpenHashMap<SynchedEntityData.DataItem<?>> entries = (Int2ObjectOpenHashMap<SynchedEntityData.DataItem<?>>) Reflector.getFieldValue(Reflector.SynchedEntityData_itemsById, dataWatcher);
        InjectedDataWatcherItem item = new InjectedDataWatcherItem(poseObj, Pose.STANDING, player);
        entries.put(poseObj.getId(), item);

        Connection netManager = ((CraftPlayer) player).getHandle().connection.connection;
        netManager.channel.pipeline().addBefore("packet_handler", "vr_aim_fix", new AimFixHandler(netManager));
    }


    public class AimFixHandler extends ChannelInboundHandlerAdapter {
        private final Connection netManager;

        public AimFixHandler(Connection netManager) {
            this.netManager = netManager;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            net.minecraft.world.entity.player.Player player = ((ServerGamePacketListenerImpl) netManager.getPacketListener()).player;
            boolean isCapturedPacket = msg instanceof ServerboundUseItemPacket || msg instanceof ServerboundUseItemOnPacket || msg instanceof ServerboundPlayerActionPacket;
            UUID uuid = player.getGameProfile().getId();
            if (!VSE.vivePlayers.containsKey(uuid) || !VSE.vivePlayers.get(uuid).isVR() || !isCapturedPacket || player.getServer() == null) {
                // we don't need to handle this packet, just defer to the next handler in the pipeline
                ctx.fireChannelRead(msg);
                return;
            }

            player.getServer().submit(() -> {
                // Save all the current orientation data
                Vec3 oldPos = player.position();
                Vec3 oldPrevPos = new Vec3(player.xo, player.yo, player.zo);
                float oldPitch = player.getXRot();
                float oldYaw = player.getYRot();
                float oldYawHead = player.yHeadRot; // field_70759_as
                float oldPrevPitch = player.xRotO;
                float oldPrevYaw = player.yRotO;
                float oldPrevYawHead = player.yHeadRotO; // field_70758_at
                float oldEyeHeight = player.getEyeHeight();

                VivePlayer data = null;
                if (VSE.vivePlayers.containsKey(uuid) && VSE.vivePlayers.get(uuid).isVR()) { // Check again in case of race condition
                    data = VSE.vivePlayers.get(uuid);
                    Location pos = data.getControllerPos(0);
                    Vector aim = data.getControllerDir(0);

                    // Inject our custom orientation data
                    player.setPosRaw(pos.getX(), pos.getY(), pos.getZ());
                    player.xo = pos.getX();
                    player.yo = pos.getY();
                    player.zo = pos.getZ();
                    player.setXRot((float) Math.toDegrees(Math.asin(-aim.getY())));
                    player.setYRot((float) Math.toDegrees(Math.atan2(-aim.getX(), aim.getZ())));
                    player.xRotO = player.getXRot();
                    player.yRotO = player.yHeadRotO = player.yHeadRot = player.getYRot();
                    Reflector.setFieldValue(Reflector.Entity_eyeHeight, player, 0);

                    // Set up offset to fix relative positions
                    // P.S. Spigot mappings are stupid
                    data.offset = oldPos.add(-pos.getX(), -pos.getY(), -pos.getZ());
                }

                // Call the packet handler directly
                // This is several implementation details that we have to replicate
                try {
                    if (netManager.isConnected()) {
                        try {
                            ((Packet) msg).handle(this.netManager.getPacketListener());
                        } catch (RunningOnDifferentThreadException runningondifferentthreadexception) {
                        }
                    }
                } finally {
                    // Vanilla uses SimpleInboundChannelHandler, which automatically releases
                    // by default, so we're expected to release the packet once we're done.
                    ReferenceCountUtil.release(msg);
                }

                // Restore the original orientation data
                player.setPosRaw(oldPos.x, oldPos.y, oldPos.z);
                player.xo = oldPrevPos.x;
                player.yo = oldPrevPos.y;
                player.zo = oldPrevPos.z;
                player.setXRot(oldPitch);
                player.setYRot(oldYaw);
                player.yHeadRot = oldYawHead;
                player.xRotO = oldPrevPitch;
                player.yRotO = oldPrevYaw;
                player.yHeadRotO = oldPrevYawHead;
                Reflector.setFieldValue(Reflector.Entity_eyeHeight, player, oldEyeHeight);

                // Reset offset
                if (data != null)
                    data.offset = new Vector3(0, 0, 0);
            });
        }
    }


    public static class InjectedDataWatcherItem extends SynchedEntityData.DataItem<Pose> {
        protected final Player player;

        public InjectedDataWatcherItem(EntityDataAccessor<Pose> datawatcherobject, Pose t0, Player player) {
            super(datawatcherobject, t0);
            this.player = player;
        }

        @Override
        public void setValue(Pose pose) {
            VivePlayer vp = VSE.vivePlayers.get(player.getUniqueId());
            if (vp != null && vp.isVR() && vp.crawling)
                super.setValue(Pose.SWIMMING);
            else
                super.setValue(pose);
        }
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

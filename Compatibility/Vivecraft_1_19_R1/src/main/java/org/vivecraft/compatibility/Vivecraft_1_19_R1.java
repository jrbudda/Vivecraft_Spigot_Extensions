package org.vivecraft.compatibility;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;
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
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import java.util.*;
import java.util.function.Predicate;

public class Vivecraft_1_19_R1 implements VivecraftCompatibility {

    private static Class<?> classEndermanFreezeWhenLookedAt;
    private static Class<?> classEndermanLookForPlayerGoal;
    private static Reflector.FieldAccessor poseAccessor;
    private static Reflector.FieldAccessor itemsByIdAccessor;
    private static Reflector.FieldAccessor eyeHeightAccessor;
    private static Reflector.FieldAccessor fallFlyTicksAccessor;
    private static Reflector.MethodAccessor teleportAccessor;
    private static Reflector.MethodAccessor teleportTowardsAccessor;


    public Vivecraft_1_19_R1() {
        classEndermanFreezeWhenLookedAt = Reflector.getNMSClass("net.minecraft.world.entity.monster", "EntityEnderman$a");
        classEndermanLookForPlayerGoal = Reflector.getNMSClass("net.minecraft.world.entity.monster", "EntityEnderman$PathfinderGoalPlayerWhoLookedAtTarget");
        poseAccessor = Reflector.getField(Entity.class, EntityDataAccessor.class, 5);
        itemsByIdAccessor = Reflector.getField(SynchedEntityData.class, Int2ObjectMap.class, 0);
        eyeHeightAccessor = Reflector.getField(Entity.class, "ba");  // https://nms.screamingsandals.org/1.19.3/net/minecraft/world/entity/Entity.html
        fallFlyTicksAccessor = Reflector.getField(LivingEntity.class,  "bB");  // https://nms.screamingsandals.org/1.19.3/net/minecraft/world/entity/LivingEntity.html
        teleportAccessor = Reflector.getMethod(EnderMan.class, "t");  // https://nms.screamingsandals.org/1.19.3/net/minecraft/world/entity/monster/EnderMan.html
        teleportTowardsAccessor = Reflector.getMethod(Enderman.class, "a", Entity.class);
    }

    @Override
    public void injectCreeper(Creeper creeper, double radius) {
        net.minecraft.world.entity.monster.Creeper e = ((CraftCreeper) creeper).getHandle();
        Set<WrappedGoal> goals = e.goalSelector.getAvailableGoals();
        goals.removeIf(SwellGoal.class::isInstance);
        e.goalSelector.addGoal(2, new CustomGoalSwell(e, radius));
    }

    @Override
    public void injectEnderman(Enderman enderman) {
        EnderMan e = ((CraftEnderman) enderman).getHandle();
        Collection<WrappedGoal> targets = e.targetSelector.getAvailableGoals();
        targets.removeIf(classEndermanLookForPlayerGoal::isInstance);
        e.targetSelector.addGoal(1, new CustomPathFinderGoalPlayerWhoLookedAtTarget(e, e::isAngryAt));

        Collection<WrappedGoal> goals = e.goalSelector.getAvailableGoals();
        goals.removeIf(classEndermanFreezeWhenLookedAt::isInstance);
        e.goalSelector.addGoal(1, new CustomGoalStare(e));
    }

    @Override
    public void injectPlayer(Player bukkit) {
        ServerPlayer player = ((CraftPlayer) bukkit).getHandle();
        player.networkManager.channel.pipeline().addBefore("packet_handler", "vr_aim_fix", new AimFixHandler(player.networkManager));
    }

    @Override
    public void injectPoseOverrider(Player bukkit) {
        ServerPlayer player = ((CraftPlayer) bukkit).getHandle();
        EntityDataAccessor<Pose> poseObj = (EntityDataAccessor<Pose>) poseAccessor.get(player);
        Int2ObjectOpenHashMap<SynchedEntityData.DataItem<?>> entries = (Int2ObjectOpenHashMap<SynchedEntityData.DataItem<?>>) itemsByIdAccessor.get(player.getEntityData());
        InjectedDataWatcherItem item = new InjectedDataWatcherItem(poseObj, Pose.STANDING, bukkit);
        entries.put(poseObj.getId(), item);
    }

    @Override
    public void resetFall(Player bukkit) {
        net.minecraft.world.entity.player.Player player = ((CraftPlayer) bukkit).getHandle();
        player.fallDistance = 0f;
        fallFlyTicksAccessor.set(player, 0);
    }

    @Override
    public org.bukkit.inventory.ItemStack setLocalizedName(org.bukkit.inventory.ItemStack item, String key) {
        var nmsStack = CraftItemStack.asNMSCopy(item);
        nmsStack.setHoverName(Component.translatable(key));
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Override
    public void setSwimming(Player player) {
        ((CraftPlayer) player).getHandle().setPose(Pose.SWIMMING);
    }

    @Override
    public void absMoveTo(Player bukkit, double x, double y, double z) {
        ServerPlayer player = ((CraftPlayer) bukkit).getHandle();
        player.absMoveTo(x, y, z);
    }

    public static class AimFixHandler extends ChannelInboundHandlerAdapter {
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
                    eyeHeightAccessor.set(player, 0f);

                    // Set up offset to fix relative positions
                    // P.S. Spigot mappings are stupid
                    Vec3 nms =  oldPos.add(-pos.getX(), -pos.getY(), -pos.getZ());
                    data.offset = new Vector(nms.x, nms.y, nms.z);
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
                eyeHeightAccessor.set(player, 0f);

                // Reset offset
                if (data != null)
                    data.offset = new Vector(0, 0, 0);
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

            return this.creeper.getSwellDir() > 0 || livingEntity != null && this.creeper.distanceToSqr(livingEntity) < distance;
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
                            teleportAccessor.invoke(enderman);
                        }

                        this.teleportTime = 0;
                    } else if (this.target.distanceToSqr(this.enderman) > 256.0D && this.teleportTime++ >= 30 && (boolean) teleportTowardsAccessor.invoke(enderman, pendingTarget)) {
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

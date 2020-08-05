package org.vivecraft.utils;

import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.server.v1_16_R1.CancelledPacketHandleException;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.NetworkManager;
import net.minecraft.server.v1_16_R1.Packet;
import net.minecraft.server.v1_16_R1.PacketListener;
import net.minecraft.server.v1_16_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R1.PacketPlayInBlockPlace;
import net.minecraft.server.v1_16_R1.PacketPlayInUseItem;
import net.minecraft.server.v1_16_R1.PlayerConnection;
import net.minecraft.server.v1_16_R1.Vec3D;
import org.bukkit.Location;

public class AimFixHandler extends ChannelInboundHandlerAdapter {
	private final NetworkManager netManager;

	public AimFixHandler(NetworkManager netManager) {
		this.netManager = netManager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		EntityPlayer player = ((PlayerConnection)netManager.i()).player;
		boolean isCapturedPacket = msg instanceof PacketPlayInBlockPlace || msg instanceof PacketPlayInUseItem || msg instanceof PacketPlayInBlockDig;

		if (!VSE.vivePlayers.containsKey(player.getProfile().getId()) || !VSE.vivePlayers.get(player.getProfile().getId()).isVR() || !isCapturedPacket || player.getMinecraftServer() == null) {
			// we don't need to handle this packet, just defer to the next handler in the pipeline
			ctx.fireChannelRead(msg);
			return;
		}

		player.getMinecraftServer().f(() -> {
			// Save all the current orientation data
			Vec3D oldPos = player.getPositionVector();
			Vec3D oldPrevPos = new Vec3D(player.lastX, player.lastY, player.lastZ);
			float oldPitch = player.pitch;
			float oldYaw = player.yaw;
			float oldYawHead = player.aJ; // field_70759_as
			float oldPrevPitch = player.lastPitch;
			float oldPrevYaw = player.lastYaw;
			float oldPrevYawHead = player.aK; // field_70758_at
			float oldEyeHeight = player.getHeadHeight();

			// Check again in case of race condition
			if (VSE.vivePlayers.containsKey(player.getProfile().getId()) && VSE.vivePlayers.get(player.getProfile().getId()).isVR()) {
				VivePlayer data = VSE.vivePlayers.get(player.getProfile().getId());
				Location pos = data.getControllerPos(0);
				Vec3D aim = data.getControllerDir(0);

				// Inject our custom orientation data
				player.setPositionRaw(pos.getX(), pos.getY(), pos.getZ());
				player.lastX = pos.getX();
				player.lastY = pos.getY();
				player.lastZ = pos.getZ();
				player.pitch = (float)Math.toDegrees(Math.asin(-aim.y));
				player.yaw = (float)Math.toDegrees(Math.atan2(-aim.x, aim.z));
				player.lastPitch = player.pitch;
				player.lastYaw = player.aJ = player.aK = player.yaw;
				VSE.setPrivateField("headHeight", Entity.class, player, 0);
			}

			// Call the packet handler directly
			// This is several implementation details that we have to replicate
			try {
				if (netManager.isConnected()) {
					try {
						((Packet<PacketListener>)msg).a(netManager.i());
					} catch (CancelledPacketHandleException e) { // Apparently might get thrown and can be ignored
					}
				}
			} finally {
				// Vanilla uses SimpleInboundChannelHandler, which automatically releases
				// by default, so we're expected to release the packet once we're done.
				ReferenceCountUtil.release(msg);
			}

			// Restore the original orientation data
			player.setPositionRaw(oldPos.x, oldPos.y, oldPos.z);
			player.lastX = oldPrevPos.x;
			player.lastY = oldPrevPos.y;
			player.lastZ = oldPrevPos.z;
			player.pitch = oldPitch;
			player.yaw = oldYaw;
			player.aJ = oldYawHead;
			player.lastPitch = oldPrevPitch;
			player.lastYaw = oldPrevYaw;
			player.aK = oldPrevYawHead;
			VSE.setPrivateField("headHeight", Entity.class, player, oldEyeHeight);
		});
	}
}

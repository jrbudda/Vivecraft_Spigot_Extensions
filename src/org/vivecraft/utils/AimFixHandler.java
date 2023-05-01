package org.vivecraft.utils;

import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.server.v1_16_R3.CancelledPacketHandleException;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketListener;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_16_R3.PacketPlayInUseItem;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;

public class AimFixHandler extends ChannelInboundHandlerAdapter {
	private final NetworkManager netManager;

	public AimFixHandler(NetworkManager netManager) {
		this.netManager = netManager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		EntityPlayer player = ((PlayerConnection)netManager.j()).player;
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
			float oldYawHead = player.aC; // field_70759_as
			float oldPrevPitch = player.lastPitch;
			float oldPrevYaw = player.lastYaw;
			float oldPrevYawHead = player.aD; // field_70758_at
			float oldEyeHeight = player.getHeadHeight();

			VivePlayer data = null;
			if (VSE.vivePlayers.containsKey(player.getProfile().getId()) && VSE.vivePlayers.get(player.getProfile().getId()).isVR()) { // Check again in case of race condition
				data = VSE.vivePlayers.get(player.getProfile().getId());
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
				player.lastYaw = player.aC = player.aD = player.yaw;
				VSE.setPrivateField("headHeight", Entity.class, player, 0);

				// Set up offset to fix relative positions
				// P.S. Spigot mappings are stupid
				data.offset = oldPos.add(-pos.getX(), -pos.getY(), -pos.getZ());
			}

			// Call the packet handler directly
			// This is several implementation details that we have to replicate
			try {
				if (netManager.isConnected()) {
					try {
						((Packet<PacketListener>)msg).a(netManager.j());
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
			player.aC = oldYawHead;
			player.lastPitch = oldPrevPitch;
			player.lastYaw = oldPrevYaw;
			player.aD = oldPrevYawHead;
			VSE.setPrivateField("headHeight", Entity.class, player, oldEyeHeight);

			// Reset offset
			if (data != null)
				data.offset = new Vec3D(0, 0, 0);
		});
	}
}

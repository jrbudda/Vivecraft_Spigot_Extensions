package org.vivecraft.utils;

import java.util.UUID;

import org.bukkit.Location;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimFixHandler extends ChannelInboundHandlerAdapter {
	private final Connection netManager;

	public AimFixHandler(Connection netManager) {
		this.netManager = netManager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Player player = ((ServerGamePacketListenerImpl)netManager.getPacketListener()).player;
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
				Vec3 aim = data.getControllerDir(0);

				// Inject our custom orientation data
				player.setPosRaw(pos.getX(), pos.getY(), pos.getZ());
				player.xo = pos.getX();
				player.yo = pos.getY();
				player.zo = pos.getZ();
				player.setXRot((float)Math.toDegrees(Math.asin(-aim.y)));
				player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x, aim.z)));
				player.xRotO = player.getXRot();
				player.yRotO = player.yHeadRotO = player.yHeadRot = player.getYRot();
				Reflector.setFieldValue(Reflector.Entity_eyesHeight, player, 0);

				// Set up offset to fix relative positions
				// P.S. Spigot mappings are stupid
				data.offset = oldPos.add(-pos.getX(), -pos.getY(), -pos.getZ());
			}

			// Call the packet handler directly
			// This is several implementation details that we have to replicate
			try {
				if (netManager.isConnected()) {
					try {
						((Packet)msg).handle(this.netManager.getPacketListener());
					} 
					catch (RunningOnDifferentThreadException runningondifferentthreadexception)
					{
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
			Reflector.setFieldValue(Reflector.Entity_eyesHeight, player, oldEyeHeight);

			// Reset offset
			if (data != null)
				data.offset = new Vec3(0, 0, 0);
		});
	}
}

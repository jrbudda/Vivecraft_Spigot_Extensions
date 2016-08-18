package org.vivecraft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.listeners.VivecraftNetworkListener;

public class VivePlayer {

	public byte[] hmdData;
	public byte[] controller0data;
	public byte[] controller1data;
	boolean isTeleportMode;
	boolean isReverseHands;
	boolean isSeated;
	boolean worldScale;

	public Player player;
	
	public VivePlayer(Player player){
		this.player = player;
	}
	
	// TODO: implement
	public Vector getHmdPos() {
		Vector out = new Vector();
		// TODO : use nms Vec3D?
		return out;
	}

	// etc
	
	public byte[] getUberPacket(){
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			output.write((byte) VivecraftNetworkListener.PacketDiscriminators.UBERPACKET.ordinal()); 		
			output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getMostSignificantBits()).array());
			output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getLeastSignificantBits()).array());
			output.write(hmdData);
			output.write(controller0data);
			output.write(controller1data);
		} catch (IOException e) {

		}
		
		return output.toByteArray();
	
	}
	
}
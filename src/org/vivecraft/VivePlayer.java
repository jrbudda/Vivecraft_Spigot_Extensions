package org.vivecraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.vivecraft.listeners.VivecraftNetworkListener;

public class VivePlayer {

	public byte[] hmdData;
	public byte[] controller0data;
	public byte[] controller1data;
	public byte[] draw;
	boolean isTeleportMode;
	boolean isReverseHands;
	boolean isSeated;
	boolean worldScale;

	public Player player;

	public VivePlayer(Player player) {
		this.player = player;
	}

	public float getDraw(){
		try {
		
			ByteArrayInputStream byin = new ByteArrayInputStream(draw);
			DataInputStream da = new DataInputStream(byin);
	
			float draw= da.readFloat();
			
			da.close(); //needed?
			return draw;
				
		} catch (IOException e) {

		}
	 
		return 0;
	}
	
	// TODO: implement
	public Location getControllerPos(int c) {
		try {
			
			ByteArrayInputStream byin = new ByteArrayInputStream(c==0?controller0data:controller1data);
			DataInputStream da = new DataInputStream(byin);
	
			boolean rev = da.readBoolean();
			float x = da.readFloat();
			float y = da.readFloat();
			float z = da.readFloat();
			
			da.close(); //needed?
			return new Location(player.getWorld(), x, y, z);
				
		} catch (IOException e) {

		}
	 
		return player.getLocation(); //why

	}
	
	
	public boolean isSeated(){
		try {
			if(hmdData.length <29) return false;//old client.
			
			ByteArrayInputStream byin = new ByteArrayInputStream(hmdData);
			DataInputStream da = new DataInputStream(byin);
	
			boolean seated= da.readBoolean();
			
			da.close(); //needed?
			return seated;
				
		} catch (IOException e) {

		}
	 
		return false;
	}

	// etc

	public byte[] getUberPacket() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			output.write((byte) VivecraftNetworkListener.PacketDiscriminators.UBERPACKET.ordinal());
			output.write(
					java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getMostSignificantBits()).array());
			output.write(
					java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getLeastSignificantBits()).array());
			output.write(hmdData);
			output.write(controller0data);
			output.write(controller1data);
		} catch (IOException e) {

		}

		return output.toByteArray();

	}

}
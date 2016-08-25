package org.vivecraft.listeners;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.command.ViveCommand;

import com.google.common.base.Charsets;

public class VivecraftNetworkListener implements PluginMessageListener {
	public VSE vse;
		
	public VivecraftNetworkListener(VSE vse){
		this.vse = vse;
	}
	
	public enum PacketDiscriminators {
		VERSION,
		REQUESTDATA,
		HEADDATA,
		CONTROLLER0DATA,
		CONTROLLER1DATA,
		WORLDSCALE,
		DRAW,
		MOVEMODE,
		UBERPACKET
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player sender, byte[] payload) {
		if(!channel.equalsIgnoreCase(vse.CHANNEL)) return;
		if(payload.length==0) return;
		
		VivePlayer vp = VSE.vivePlayers.get(sender.getUniqueId());
		
		PacketDiscriminators disc = PacketDiscriminators.values()[payload[0]];
		if(vp == null && disc.ordinal() > 0) {
			//how?
			return;
		}
		
		byte[] data = Arrays.copyOfRange(payload, 1, payload.length);
		switch (disc){
		case CONTROLLER0DATA:
			vp.controller0data = data;
			break;
		case CONTROLLER1DATA:
			vp.controller1data = data;
			break;
		case DRAW:
			vp.draw = data;
			break;
		case HEADDATA:
			vp.hmdData = data;
			break;
		case MOVEMODE:
			break;
		case REQUESTDATA:
			//only we can use that word.
			break;
		case VERSION:
			VSE.vivePlayers.put(sender.getUniqueId(), new VivePlayer(sender));
			ViveCommand.sendMessage("Welcome Vive user!",sender);
			sender.sendPluginMessage(vse, vse.CHANNEL, StringToPayload(PacketDiscriminators.VERSION, vse.getDescription().getFullName()));
			if(vse.getConfig().getBoolean("SendPlayerData.enabled") == true)
			sender.sendPluginMessage(vse, vse.CHANNEL, new byte[]{(byte) PacketDiscriminators.REQUESTDATA.ordinal()});
			break;
		case WORLDSCALE:
			break;
		default:
			break;
		}
	}

	public static byte[] StringToPayload(PacketDiscriminators version, String input){
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] bytes = input.getBytes(Charsets.UTF_8);
		int len = bytes.length;
		if( len > 255) return output.toByteArray();
		try {
			output.write((byte)version.ordinal());
			output.write((byte) len);
			//TODO: check endianness.
			output.write(bytes);
		} catch (IOException e) {
		}

		return output.toByteArray();
		
	}
	
	

	
}

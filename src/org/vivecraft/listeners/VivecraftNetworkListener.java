package org.vivecraft.listeners;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.command.ViveCommand;

import com.google.common.base.Charsets;

import net.minecraft.server.v1_11_R1.EntityPlayer;

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
		UBERPACKET,
		TELEPORT,
		CLIMBING
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
			ByteArrayInputStream byin = new ByteArrayInputStream(data);
			DataInputStream da = new DataInputStream(byin);
			InputStreamReader is = new InputStreamReader(da);
			BufferedReader br = new BufferedReader(is);
			VSE.vivePlayers.put(sender.getUniqueId(), new VivePlayer(sender));

			sender.sendPluginMessage(vse, vse.CHANNEL, StringToPayload(PacketDiscriminators.VERSION, vse.getDescription().getFullName()));
			
			VivePlayer vivepl = VSE.vivePlayers.get(sender.getUniqueId());
			try {
				String version = br.readLine();
				
				if(vivepl.isSeated()){
					vivepl.setVR(true);
					if(vse.getConfig().getBoolean("welcomemsg.enabled")){
						String message = vse.getConfig().getString("welcomemsg.welcomeSeated");
						String format = message.replace("&player", sender.getDisplayName());
						for(Player p : Bukkit.getOnlinePlayers()){
							ViveCommand.sendMessage(format,p);
						}
					}
				}else
				if(version.contains("NONVR")){
					vivepl.setVR(false);
					
					if(vse.getConfig().getBoolean("welcomemsg.enabled")){
						String message = vse.getConfig().getString("welcomemsg.welcomenonVR");
						String format = message.replace("&player", sender.getDisplayName());
						for(Player p : Bukkit.getOnlinePlayers()){
							ViveCommand.sendMessage(format,p);
						}
					}
				}else{
					vivepl.setVR(true);
					
					if(vse.getConfig().getBoolean("welcomemsg.enabled")){
						String message = vse.getConfig().getString("welcomemsg.welcomeVR");
						String format = message.replace("&player", sender.getDisplayName());
						for(Player p : Bukkit.getOnlinePlayers()){
							ViveCommand.sendMessage(format,p);
						}
					}
					
					if(vse.getConfig().getBoolean("SendPlayerData.enabled") == true)
						sender.sendPluginMessage(vse, vse.CHANNEL, new byte[]{(byte) PacketDiscriminators.REQUESTDATA.ordinal()});
					
					if(vse.getConfig().getBoolean("climbey.enabled") == true)
						sender.sendPluginMessage(vse, vse.CHANNEL, new byte[]{(byte) PacketDiscriminators.CLIMBING.ordinal()});
					
					sender.sendPluginMessage(vse, vse.CHANNEL, new byte[]{(byte) PacketDiscriminators.TELEPORT.ordinal()});

					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case WORLDSCALE:
			break;
		case TELEPORT:
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			DataInputStream d = new DataInputStream(in);
			try {
				float x = d.readFloat();
				float y = d.readFloat();
				float z = d.readFloat();
				EntityPlayer nms = 	((CraftPlayer)sender).getHandle();
				nms.setLocation(x, y, z, nms.pitch, nms.yaw);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			break;
		case CLIMBING:
			EntityPlayer nms = 	((CraftPlayer)sender).getHandle();
			nms.fallDistance = 0;
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

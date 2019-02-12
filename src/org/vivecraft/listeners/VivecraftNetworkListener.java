package org.vivecraft.listeners;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import com.google.common.base.Charsets;

import net.minecraft.server.v1_13_R2.EntityPlayer;

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
		if(vp == null && disc != PacketDiscriminators.VERSION) {
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
			vp = new VivePlayer(sender);
			ByteArrayInputStream byin = new ByteArrayInputStream(data);
			DataInputStream da = new DataInputStream(byin);
			InputStreamReader is = new InputStreamReader(da);
			BufferedReader br = new BufferedReader(is);
			VSE.vivePlayers.put(sender.getUniqueId(), vp);

			sender.sendPluginMessage(vse, vse.CHANNEL, StringToPayload(PacketDiscriminators.VERSION, vse.getDescription().getFullName()));

			try {
				String version = br.readLine();

				if(version.contains("NONVR")){
					vse.getLogger().info("NONVR" + sender.getDisplayName());
					vp.setVR(false);
				}
				else{
					vse.getLogger().info("VR" + sender.getDisplayName());
					vp.setVR(true);
				}

				if(vse.getConfig().getBoolean("SendPlayerData.enabled") == true)
					sender.sendPluginMessage(vse, vse.CHANNEL, new byte[]{(byte) PacketDiscriminators.REQUESTDATA.ordinal()});

				if(vse.getConfig().getBoolean("climbey.enabled") == true){

					final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

					byteArrayOutputStream.write(PacketDiscriminators.CLIMBING.ordinal());

					final ObjectOutputStream objectOutputStream =
							new ObjectOutputStream(byteArrayOutputStream);
					String mode = vse.getConfig().getString("climbey.blockmode","none");
					byte m = 0;
					if(!sender.hasPermission(vse.getConfig().getString("permissions.climbperm"))){
						if(mode.trim().equalsIgnoreCase("include"))
							m = 1;
						else if(mode.trim().equalsIgnoreCase("exclude"))
							m = 2;
					} else {
					}
					objectOutputStream.writeByte(m);
					objectOutputStream.writeObject(vse.blocklist);
					objectOutputStream.flush();

					final byte[] p = byteArrayOutputStream.toByteArray();

					sender.sendPluginMessage(vse, vse.CHANNEL, p);

					objectOutputStream.close();

				}

				sender.sendPluginMessage(vse, vse.CHANNEL, new byte[]{(byte) PacketDiscriminators.TELEPORT.ordinal()});

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
		try {
			output.write((byte)version.ordinal());
			if(!writeVarInt(output, len, 2)) {
				output.reset();
				return output.toByteArray();
			}
			//TODO: check endianness.
			output.write(bytes);
		} catch (IOException e) {
		}

		return output.toByteArray();
		
	}
	
    public static int varIntByteCount(int toCount)
    {
        return (toCount & 0xFFFFFF80) == 0 ? 1 : ((toCount & 0xFFFFC000) == 0 ? 2 : ((toCount & 0xFFE00000) == 0 ? 3 : ((toCount & 0xF0000000) == 0 ? 4 : 5)));
    }
	
    public static boolean writeVarInt(ByteArrayOutputStream to, int toWrite, int maxSize)
    {
        if (varIntByteCount(toWrite) > maxSize) return false;
        while ((toWrite & -128) != 0)
        {
            to.write(toWrite & 127 | 128);
            toWrite >>>= 7;
        }

        to.write(toWrite);
        return true;
    }
}

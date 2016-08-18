package org.vivecraft;

import org.bukkit.util.Vector;

public class VivePlayer {

	
	public byte[] hmdData;
	public byte[] controller0data;
	public byte[] controller1data;
	boolean isTeleportMode;
	boolean isReverseHands;
	boolean isSeated;
	boolean worldScale;
	
	//TODO: implement
	public Vector getHmdPos(){
		Vector out = new Vector();
		//TODO : use nms Vec3D?
		return out;
	}
	
	//etc
	
}

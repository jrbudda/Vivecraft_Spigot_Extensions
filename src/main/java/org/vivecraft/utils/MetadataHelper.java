package org.vivecraft.utils;

import java.util.concurrent.Callable;

import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import net.minecraft.world.phys.Vec3;

public class MetadataHelper {
	public static void updateMetdata(final VivePlayer data) {
		addOrInvalidateKey(data.player, "head.pos", () -> getPos(data.getHMDPos(), data.getHMDDir()));
		addOrInvalidateKey(data.player, "head.aim", data::getHMDDir); // God forbid someone is using this...
		addOrInvalidateKey(data.player, "head.dir", () -> getAim(data.getHMDDir()));
		addOrInvalidateKey(data.player, "head.rot", () -> {
			Quaternion quat = data.getHMDRot();
			return new float[]{quat.w, quat.x, quat.y, quat.z};
		});

		addOrInvalidateKey(data.player, "righthand.pos", () -> getPos(data.getControllerPos(0), data.getControllerDir(0)));
		addOrInvalidateKey(data.player, "righthand.aim", () -> data.getControllerDir(0)); // Really seriously don't use this one.
		addOrInvalidateKey(data.player, "righthand.dir", () -> getAim(data.getControllerDir(0)));
		addOrInvalidateKey(data.player, "righthand.rot", () -> {
			Quaternion quat = data.getControllerRot(0);
			return new float[]{quat.w, quat.x, quat.y, quat.z};
		});

		addOrInvalidateKey(data.player, "lefthand.pos", () -> getPos(data.getControllerPos(1), data.getControllerDir(1)));
		addOrInvalidateKey(data.player, "lefthand.aim", () -> data.getControllerDir(1)); // It's an nms class, don't use it, use the other one.
		addOrInvalidateKey(data.player, "lefthand.dir", () -> getAim(data.getControllerDir(1)));
		addOrInvalidateKey(data.player, "lefthand.rot", () -> {
			Quaternion quat = data.getControllerRot(1);
			return new float[]{quat.w, quat.x, quat.y, quat.z};
		});

		addOrInvalidateKey(data.player, "seated", data::isSeated);
		addOrInvalidateKey(data.player, "height", () -> data.heightScale);
		addOrInvalidateKey(data.player, "activehand", () -> data.activeHand == 0 ? "right" : "left");
	}

	public static void cleanupMetadata(Player player) {
		player.removeMetadata("head.pos", VSE.me);
		player.removeMetadata("head.aim", VSE.me);
		player.removeMetadata("head.dir", VSE.me);
		player.removeMetadata("head.rot", VSE.me);
		player.removeMetadata("righthand.pos", VSE.me);
		player.removeMetadata("righthand.aim", VSE.me);
		player.removeMetadata("righthand.dir", VSE.me);
		player.removeMetadata("righthand.rot", VSE.me);
		player.removeMetadata("lefthand.pos", VSE.me);
		player.removeMetadata("lefthand.aim", VSE.me);
		player.removeMetadata("lefthand.dir", VSE.me);
		player.removeMetadata("lefthand.rot", VSE.me);
		player.removeMetadata("seated", VSE.me);
		player.removeMetadata("height", VSE.me);
		player.removeMetadata("activehand", VSE.me);
	}

	private static void addOrInvalidateKey(Player player, String key, Callable<Object> lazyValue) {
		if (!player.hasMetadata(key)) {
			player.setMetadata(key, new LazyMetadataValue(VSE.me, lazyValue));
		} else {
			MetadataValue value = player.getMetadata(key).stream().filter((v) -> v.getOwningPlugin() == VSE.me).findFirst().orElseThrow(() -> new RuntimeException("someone messed with our metadata"));
			value.invalidate();
		}
	}
	
	private static Location getPos(Location pos, Vec3 dir) {
		return pos.setDirection(getAim(dir));
	}
	
	private static Vector getAim(Vec3 dir) {
		return new Vector(dir.x, dir.y, dir.z);
	}
}

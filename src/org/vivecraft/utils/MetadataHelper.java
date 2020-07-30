package org.vivecraft.utils;

import java.util.concurrent.Callable;

import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class MetadataHelper {
	public static void updateMetdata(final VivePlayer data) {
		addOrInvalidateKey(data.player, "head.pos", data::getHMDPos);
		addOrInvalidateKey(data.player, "head.aim", data::getHMDDir);
		addOrInvalidateKey(data.player, "head.rot", () -> {
			Quaternion quat = data.getHMDRot();
			return new float[]{quat.w, quat.x, quat.y, quat.z};
		});

		addOrInvalidateKey(data.player, "righthand.pos", () -> data.getControllerPos(0));
		addOrInvalidateKey(data.player, "righthand.aim", () -> data.getControllerDir(0));
		addOrInvalidateKey(data.player, "righthand.rot", () -> {
			Quaternion quat = data.getControllerRot(0);
			return new float[]{quat.w, quat.x, quat.y, quat.z};
		});

		addOrInvalidateKey(data.player, "lefthand.pos", () -> data.getControllerPos(1));
		addOrInvalidateKey(data.player, "lefthand.aim", () -> data.getControllerDir(1));
		addOrInvalidateKey(data.player, "lefthand.rot", () -> {
			Quaternion quat = data.getControllerRot(1);
			return new float[]{quat.w, quat.x, quat.y, quat.z};
		});

		addOrInvalidateKey(data.player, "seated", data::isSeated);
		addOrInvalidateKey(data.player, "height", () -> data.heightScale);
		addOrInvalidateKey(data.player, "activehand", () -> data.activeHand == 0 ? "right" : "left");
	}

	private static void addOrInvalidateKey(Player player, String key, Callable<Object> lazyValue) {
		if (!player.hasMetadata(key)) {
			player.setMetadata(key, new LazyMetadataValue(VSE.me, lazyValue));
		} else {
			MetadataValue value = player.getMetadata(key).stream().filter((v) -> v.getOwningPlugin() == VSE.me).findFirst().orElseThrow(() -> new RuntimeException("someone messed with our metadata"));
			value.invalidate();
		}
	}
}

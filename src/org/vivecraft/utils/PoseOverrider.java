package org.vivecraft.utils;

import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.server.v1_16_R1.DataWatcher;
import net.minecraft.server.v1_16_R1.DataWatcherObject;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityPose;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PoseOverrider {
	@SuppressWarnings("unchecked")
	public static void injectPlayer(Player player) {
		DataWatcherObject<EntityPose> poseObj = (DataWatcherObject<EntityPose>)VSE.getPrivateField("POSE", Entity.class, null);
		DataWatcher dataWatcher = (DataWatcher)VSE.getPrivateField("datawatcher", Entity.class, ((CraftPlayer)player).getHandle());
		Int2ObjectOpenHashMap<DataWatcher.Item<?>> entries = (Int2ObjectOpenHashMap<DataWatcher.Item<?>>)VSE.getPrivateField("entries", DataWatcher.class, dataWatcher);

		InjectedDataWatcherItem item = new InjectedDataWatcherItem(poseObj, EntityPose.STANDING, player);
		entries.put(poseObj.a(), item);
	}

	public static class InjectedDataWatcherItem extends DataWatcher.Item<EntityPose> {
		protected final Player player;

		public InjectedDataWatcherItem(DataWatcherObject<EntityPose> datawatcherobject, EntityPose t0, Player player) {
			super(datawatcherobject, t0);
			this.player = player;
		}

		@Override
		public void a(EntityPose pose) {
			VivePlayer vp = VSE.vivePlayers.get(player.getUniqueId());
			if (vp != null && vp.isVR() && vp.crawling)
				super.a(EntityPose.SWIMMING);
			else
				super.a(pose);
		}
	}
}

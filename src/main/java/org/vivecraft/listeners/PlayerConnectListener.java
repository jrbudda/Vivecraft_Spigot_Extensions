package org.vivecraft.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.utils.AimFixHandler;
import org.vivecraft.utils.MetadataHelper;

public class PlayerConnectListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        VSE plugin = VSE.me;
        VSE.vivePlayers.remove(event.getPlayer().getUniqueId());
        MetadataHelper.cleanupMetadata(event.getPlayer());

        if (plugin.getConfig().getBoolean("welcomemsg.enabled"))
            plugin.broadcastConfigString("welcomemsg.leaveMessage", event.getPlayer().getDisplayName());
    }

    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent event) {
        VSE plugin = VSE.me;
        final Player p = event.getPlayer();

        if (plugin.debug)
            plugin.getLogger().info(p.getName() + " Has joined the server");

        int t = plugin.getConfig().getInt("general.vive-only-kickwaittime", 200);
        if (t < 100) t = 100;
        if (t > 1000) t = 1000;

        if (plugin.debug)
            plugin.getLogger().info("Checking " + event.getPlayer().getName() + " for Vivecraft");

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (p.isOnline()) {
                boolean kick = false;

                if (VSE.vivePlayers.containsKey(p.getUniqueId())) {
                    VivePlayer vp = VSE.vivePlayers.get(p.getUniqueId());
                    if (plugin.debug)
                        plugin.getLogger().info(p.getName() + " using: " + vp.version + " " + (vp.isVR() ? "VR" : "NONVR") + " " + (vp.isSeated() ? "SEATED" : ""));
                    if (!vp.isVR()) kick = true;
                } else {
                    kick = true;
                    if (plugin.debug)
                        plugin.getLogger().info(p.getName() + " Vivecraft not detected");
                }

                if (kick) {
                    if (plugin.getConfig().getBoolean("general.vive-only")) {
                        if (!plugin.getConfig().getBoolean("general.allow-op") || !p.isOp()) {
                            plugin.getLogger().info(p.getName() + " " + "got kicked for not using Vivecraft");
                            p.kickPlayer(plugin.getConfig().getString("general.vive-only-kickmessage"));
                        }
                        return;
                    }
                }

                plugin.sendWelcomeMessage(p);
                plugin.setPermissionsGroup(p);
            } else {
                if (plugin.debug)
                    plugin.getLogger().info(p.getName() + " no longer online! ");
            }
        }, t);


    }
}

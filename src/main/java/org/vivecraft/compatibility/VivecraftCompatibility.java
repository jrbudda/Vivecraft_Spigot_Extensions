package org.vivecraft.compatibility;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

public interface VivecraftCompatibility {

    void injectCreeper(Creeper creeper, double radius);

    void injectEnderman(Enderman enderman);

    void injectPlayer(Player player);
}

package org.vivecraft.compatibility;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;

public interface VivecraftCompatibility {

    void editCreeper(Creeper creeper, int radius);

    void editEnderman(Enderman enderman);
}

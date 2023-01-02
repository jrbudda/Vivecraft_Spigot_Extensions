package org.vivecraft.compatibility;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;

public interface VivecraftCompatibility {

    void editCreeper(Creeper creeper, double radius);

    void editEnderman(Enderman enderman);
}

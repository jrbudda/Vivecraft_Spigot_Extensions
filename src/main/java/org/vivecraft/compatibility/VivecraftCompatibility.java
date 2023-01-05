package org.vivecraft.compatibility;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VivecraftCompatibility {

    void injectCreeper(Creeper creeper, double radius);

    void injectEnderman(Enderman enderman);

    void injectPlayer(Player player);

    void injectPoseOverrider(Player player);

    void resetFall(Player player);

    ItemStack setLocalizedName(ItemStack item, String key);

    void setSwimming(Player player);

    void absMoveTo(Player player, double x, double y, double z);
}

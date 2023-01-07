package org.vivecraft.compatibility;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This compatibility class is used when the Minecraft Version is not supported.
 * In its current state (no code at all), this effectively disables the plugin.
 * In the future, we should use reflection to enable partial support.
 *
 * TODO use reflection.
 */
public class UnknownVivecraftCompatibility implements VivecraftCompatibility {

    @Override
    public void injectCreeper(Creeper creeper, double radius) {
    }

    @Override
    public void injectEnderman(Enderman enderman) {
    }

    @Override
    public void injectPlayer(Player player) {
    }

    @Override
    public void injectPoseOverrider(Player player) {
    }

    @Override
    public void resetFall(Player player) {
    }

    @Override
    public ItemStack setLocalizedName(ItemStack item, String key) {
        return item;
    }

    @Override
    public void setSwimming(Player player) {
    }

    @Override
    public void absMoveTo(Player player, double x, double y, double z) {
    }
}

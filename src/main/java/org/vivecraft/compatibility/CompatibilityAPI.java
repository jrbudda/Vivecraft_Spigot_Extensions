package org.vivecraft.compatibility;

import org.bukkit.Bukkit;
import org.vivecraft.VSE;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CompatibilityAPI {

    private static VivecraftCompatibility compatibility;

    public static VivecraftCompatibility getCompatibility() {

        // When compatibility has not yet been setup
        if (compatibility == null) {

            // Get the version string like '1_19_R2' for 1.19.3
            String version = Bukkit.getServer().getClass().getPackageName().replace(".", ",").split(",")[3];

            try {

                // If a class exists for this minecraft protocol version, then
                // we should cache and instance of it to be used as the compatibility
                Class<?> clazz = Class.forName("org.vivecraft.compatibility.Vivecraft_" + version);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                compatibility = (VivecraftCompatibility) instance;

            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                Logger log = VSE.me.getLogger();
                log.log(Level.WARNING, "Your version '" + version + "' is not fully supported");
                log.log(Level.WARNING, "Check Spigot for a list of supported Minecraft versions");
                log.log(Level.WARNING, "If you just updated your server to the newest version of Minecraft, make sure you update Vivecraft_Spigot_Extensions as well!");
                log.log(Level.WARNING, "The following features will now be disabled: ");
                log.log(Level.WARNING, "  - CreeperRadius");
                log.log(Level.WARNING, "  - CreeperRadius");
                log.log(Level.WARNING, "  - CreeperRadius");
                log.log(Level.WARNING, "  - CreeperRadius");

                // When there is no class for this version of minecraft, we
                // should use the 'Unknown' version. This allows *MOST* of the
                // plugin to run, but the version dependent features will not
                // work.
                compatibility = new UnknownVivecraftCompatibility();
            }
        }

        return compatibility;
    }
}

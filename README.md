<div align="center">

![Image](https://i0.wp.com/www.vivecraft.org/wp-content/uploads/2016/07/wesYwME.png?w=650&ssl=1)

[![Spigot](https://img.shields.io/badge/-Spigot-orange?logo=data%3Aimage%2Fx-icon%3Bbase64%2CAAABAAEAEBAQAAAAAAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAgAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAND%2FAOhGOgA%2F6OIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAiAAAAAAAAACIAAAAAAAAAIgAAAAAAAAAAAAAAAAAAABEAAAAzMQABEQAAARMzEBERARERETMxERAAAAARMzEAAAAAAAETMwAAAAAAABEwAAAAAAAAERAAAAAAAAABAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAD%2F%2BQAA%2F%2FkAAP%2F5AAD%2F8AAA%2BDAAAPAgAAAAAAAAAAEAAAADAADwDwAA%2FB8AAPwfAAD8HwAA%2Fj8AAP4%2FAADwBwAA)](https://www.spigotmc.org/resources/33166/)
[![Download](https://img.shields.io/github/downloads/CJCrafter/Vivecraft_Spigot_Extensions/total?color=green)](https://github.com/CJCrafter/Vivecraft_Spigot_Extensions/releases/latest)
[![Version](https://img.shields.io/github/v/release/CJCrafter/Vivecraft_Spigot_Extensions?include_prereleases&label=version)](https://github.com/CJCrafter/Vivecraft_Spigot_Extensions/releases/latest)
</div>

# Vivecraft Spigot Extensions

VSE is a companion plugin for [Vivecraft](http://www.vivecraft.org), the VR mod for Java Minecraft.  
VSE is for [Spigot](https://www.spigotmc.org/) servers and adds several enhancements for VR players.

# Disclaimer
This is NOT the original version of VSE. This is a modified version of the original
to fix various issues, add a working API, and *attempts* to *partially* fix some of the worst 
Spigot code I have ever seen. I intended for my code to merged into the original
project, but the developers have not responded to my multiple requests in 6 months.
So I created this project to add VR support to [WeaponMechanics](https://www.spigotmc.org/resources/99913/). 
Any other developers are free to use this plugin to add their own VR support.

I do **NOT**:
 * provide support for this plugin
 * have any plans to add any new features
 * have any associations with the original plugin

Please consider downloading the [original version](https://www.spigotmc.org/resources/33166/) instead.

# Developer Information

Due to the Metadata system not working when multiple worlds are involved (such as Multiverse), 
it is recommended to depend on the jar directly. Use `VSE.vivePlayers.get(UUID)`

Here is an example from WeaponMechanics which gets the location of the VR hand (and direction):
```java
        if (Bukkit.getPluginManager().getPlugin("Vivecraft-Spigot-Extensions") != null
                && livingEntity.getType() == EntityType.PLAYER) {
            // Vivecraft support for VR players

            VivePlayer vive = VSE.vivePlayers.get(livingEntity.getUniqueId());
            if (vive != null && vive.isVR()) {
                // Now we know it's actually VR player

                // Get the position and direction from player metadata
                Location location = vive.getControllerPos(mainhand ? 0 : 1);
                location.setDirection(vive.getControllerDir(mainhand ? 0 : 1));
                return location;
            }

            // Not VR player, let pass to these normal location finders
        }
```

## Metadata

The following information is from the legacy API and original version. Expect it to not work randomly.


VSE provides Spigot metadata on `Player` objects so other plugins can provide special support for handed interactions or
somesuch. If you aren’t sure what metadata is, check
the [Spigot documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/metadata/Metadatable.html). The API
supports multiple plugins using the same metadata key, so make sure you filter to our specific plugin
name (`Vivecraft-Spigot-Extensions`).

Every player has a head and two hands (obviously), each of which have a 6DOF position and rotation. There are also some
tertiary values so you can determine how to properly handle a particular player. The full set of available keys is as
follows:

| Key(s)                                      | Value                                                                                                                                                                                                                              |
|---------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `head.pos`, `righthand.pos`, `lefthand.pos` | `Location` representing the absolute position in the world of the VR object. Also includes the direction for convenience.                                                                                                          |
| `head.dir`, `righthand.dir`, `lefthand.dir` | `Vector` representing the forward direction of the VR object. This is gimbal locked; if you want up or right vectors, use the `rot` value below.                                                                                   |
| `head.rot`, `righthand.rot`, `lefthand.rot` | Array of 4 floats, representing a quaternion with the order `w,x,y,z`. You’ll need a `Quaternion` class to deal with this properly, but it’s much more flexible than the `dir` value. Feel free to use the one in this repository. |
| `seated`                                    | `Boolean` representing the player is in seated mode. This mode disables hand tracking and places the VR hands to the sides of the head, to allow for keyboard and mouse play in VR.                                                |
| `height`                                    | `Float` representing whether the player's calibrated height, which mainly affects how tall they appear to other players.                                                                                                           |
| `activehand`                                | `String` representing which hand (left or right) last performed some actions. Currently throwing projectiles such as snowballs.                                                                                                    |

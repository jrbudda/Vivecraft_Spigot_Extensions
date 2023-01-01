
<div align="center">

![Image](https://i0.wp.com/www.vivecraft.org/wp-content/uploads/2016/07/wesYwME.png?w=650&ssl=1)

  [![Spigot](https://img.shields.io/badge/-Spigot-orange?logo=data%3Aimage%2Fx-icon%3Bbase64%2CAAABAAEAEBAQAAAAAAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAgAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAND%2FAOhGOgA%2F6OIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAiAAAAAAAAACIAAAAAAAAAIgAAAAAAAAAAAAAAAAAAABEAAAAzMQABEQAAARMzEBERARERETMxERAAAAARMzEAAAAAAAETMwAAAAAAABEwAAAAAAAAERAAAAAAAAABAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAD%2F%2BQAA%2F%2FkAAP%2F5AAD%2F8AAA%2BDAAAPAgAAAAAAAAAAEAAAADAADwDwAA%2FB8AAPwfAAD8HwAA%2Fj8AAP4%2FAADwBwAA)](https://www.spigotmc.org/resources/33166/)
  [![Discord](https://img.shields.io/discord/178567952780623872.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/2x3QCk8qa9)
  [![Download](https://img.shields.io/github/downloads/jrbudda/Vivecraft_Spigot_Extensions/total?color=green)](https://github.com/jrbudda/Vivecraft_Spigot_Extensions/releases/latest)
  [![Version](https://img.shields.io/github/v/release/jrbudda/Vivecraft_Spigot_Extensions?include_prereleases&label=version)](https://github.com/jrbudda/Vivecraft_Spigot_Extensions/releases/latest)
</div>

# Vivecraft Spigot Extensions
VSE is a companion plugin for [Vivecraft](http://www.vivecraft.org), the VR mod for Java Minecraft.  
VSE is for [Spigot](https://www.spigotmc.org/) servers and adds several enhancements for VR players.

# Features
 - Vivecraft players will see other Vivecraft players head and arm movements.
 - Support for Vivecraft 2-handed archery.
 - Assign permission groups for VR players.
 - Fixes projectiles and dropped items from VR players.
 - Shrinks Creeper explosion radius for VR players from 3 to 1.75m (Configurable)
 - Option to limit server to Vivecraft players only.

See the config.yml for all available configuration options.

# Installation
Download from the [Releases](https://github.com/jrbudda/Vivecraft_Spigot_Extensions/releases) page. Please ensure you download the correct version of the plugin as they are not backwards compatible.

Install as you would any other Spigot/Bukkit plugin by placing the jar in the /plugins folder. 

# Developer Information
## Metadata
VSE provides Spigot metadata on `Player` objects so other plugins can provide special support for handed interactions or somesuch. If you aren’t sure what metadata is, check the [Spigot documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/metadata/Metadatable.html). The API supports multiple plugins using the same metadata key, so make sure you filter to our specific plugin name (`Vivecraft-Spigot-Extensions`).

Every player has a head and two hands (obviously), each of which have a 6DOF position and rotation. There are also some tertiary values so you can determine how to properly handle a particular player. The full set of available keys is as follows:

| Key(s)                                      | Value                                                                                                                                                                                                                              |
|---------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `head.pos`, `righthand.pos`, `lefthand.pos` | `Location` representing the absolute position in the world of the VR object. Also includes the direction for convenience.                                                                                                          |
| `head.dir`, `righthand.dir`, `lefthand.dir` | `Vector` representing the forward direction of the VR object. This is gimbal locked; if you want up or right vectors, use the `rot` value below.                                                                                   |
| `head.rot`, `righthand.rot`, `lefthand.rot` | Array of 4 floats, representing a quaternion with the order `w,x,y,z`. You’ll need a `Quaternion` class to deal with this properly, but it’s much more flexible than the `dir` value. Feel free to use the one in this repository. |
| `seated`                                    | `Boolean` representing the player is in seated mode. This mode disables hand tracking and places the VR hands to the sides of the head, to allow for keyboard and mouse play in VR.                                                |
| `height`                                    | `Float` representing whether the player's calibrated height, which mainly affects how tall they appear to other players.                                                                                                           |
| `activehand`                                | `String` representing which hand (left or right) last performed some actions. Currently throwing projectiles such as snowballs.                                                                                                    |

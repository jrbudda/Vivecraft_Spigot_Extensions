# Vivecraft_Spigot_Extensions
VSE is a companion plugin for [Vivecraft](http://www.vivecraft.org), the VR mod for Java Minecraft. 
VSE is for [Spigot](https://www.spigotmc.org/) servers and adds several enhancements for VR players.

## Features
 - Vivecraft players will see other Vivecraft players head and arm movements.
 - Support for Vivecraft 2-handed archery.
 - Assign permission groups for VR players.
 - Fixes projectiles and dropped items from VR players.
 - Shrinks Creeper explosion radius for VR players from 3 to 1.75m (Configurable)
 - Option to limit server to Vivecraft players only.

See the config.yml for all available configuration options.

## Installation
Download from the [Releases](https://github.com/jrbudda/Vivecraft_Spigot_Extensions/releases) page. Please ensure you download the correct version of the plugin as they are not backwards compatible.

Install as you would any other Spigot/Bukkit plugin by placing the jar in the /plugins folder. 

## Permissions

Permission                  | Default   | Description
----------------------------|-----------|----------------------------------------------
vive.use                    | true      | Whether or not to provide server integrations
vive.climbanywhere          | op        | Permission to override climb limitations.
vive.command.vive-only      | op        | Access to the /vse vive-only command
vive.command.sendplayerdata | op        | Access to the /vse sendplayerdata command
vive.command.creeperradius  | true      | Access to the /vse creeperradius command
vive.command.waittime       | op        | Access to the /vse waittime command
vive.command.bow            | op        | Access to the /vse bow command
vive.command.list           | true      | Access to the /vse list command
vive.command.set            | true      | Access to the /vse set command
vive.command.version        | true      | Access to the /vse version command
vive.command.checkforupdate | false     | Access to the /vse checkforupdate command
vive.command.help           | true      | Access to the /vse help command
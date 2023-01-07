// This plugin by Paper allows us to use the mojang-mapped "NMS" classes, and
// it automatically remaps them to spigot-mappings during the build phase.
// Works for 1.17+
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "Vivecraft_Spigot_Extensions"

// We have to include the subprojects so editors like IntelliJ recognize
// them as valid Java projects.
include(":BuildVivecraftSpigotExtensions")

include(":Vivecraft_1_19_R1")
include(":Vivecraft_1_19_R2")

project(":Vivecraft_1_19_R1").projectDir = file("Compatibility/Vivecraft_1_19_R1")
project(":Vivecraft_1_19_R2").projectDir = file("Compatibility/Vivecraft_1_19_R2")
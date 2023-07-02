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

// Include compatibility issues
listOf("19_R3", "20_R1").forEach {
    println("Including Vivecraft module 1_$it")
    include(":Vivecraft_1_$it")
    project(":Vivecraft_1_$it").projectDir = file("Compatibility/Vivecraft_1_$it")
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "org.vivecraft"
version = "2.0.0"

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

// See https://github.com/Minecrell/plugin-yml
bukkit {
    name = "Vivecraft-Spigot-Extensions"
    main = "org.vivecraft.VSE"
    apiVersion = "1.19"
    website = "https://www.vivecraft.org"
    authors = listOf("jrbudda", "jaron780")
    prefix = "Vivecraft"
    softDepend = listOf("Vault")

    commands {
        register("Vive") {
            description = "Vivecraft Spigot Extensions"
            usage = "/vive <command>"
            aliases = listOf("vse")
        }
    }
}

dependencies {
    implementation(project(":")) // base project
    implementation(project(":Vivecraft_1_19_R1", "reobf"))
    implementation(project(":Vivecraft_1_19_R2", "reobf"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(16)
    }
}

// The shadowJar task builds a "fat jar" (a jar with all dependencies built in).
tasks.named<ShadowJar>("shadowJar") {

    archiveFileName.set("Vivecraft_Spigot_Extensions-${project.version}.jar")
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    // This automatically "shades" (adds to jar) the bstats libs into the
    // org.vivecraft.bstats package.
    dependencies {
        include(project(":")) // base project
        include(project(":Vivecraft_1_19_R1"))
        include(project(":Vivecraft_1_19_R2"))

        relocate("org.bstats", "org.vivecraft.bstats") {
            include(dependency("org.bstats:"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}


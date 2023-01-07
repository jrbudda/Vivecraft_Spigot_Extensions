plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.9"
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    implementation(project(":"))
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17) // Override the BuildVivecraftSpigotExtension's java 16 since 1.19 requires java 17
    }
}